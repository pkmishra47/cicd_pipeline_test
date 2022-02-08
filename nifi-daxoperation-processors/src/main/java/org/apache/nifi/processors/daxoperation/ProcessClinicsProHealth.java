package org.apache.nifi.processors.daxoperation;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.DBProHealth;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "operations", "dataman", "clinicsProHealth", "oldsite"})
@CapabilityDescription("")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class ProcessClinicsProHealth extends AbstractProcessor {

    private static final String processorName = "ProcessClinicsProHealth";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private MongoClient mongoClient = null;
    protected MongoClientService mongoClientService;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private ProHealthDao proHealthDao = null;
    private NotificationDao notificationDao = null;
    private List<ClinicProhealth> clinicProhealthList = null;

    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public String getProcessorName() {
        return ProcessClinicsProHealth.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();

        return this.dateUtil;
    }

    public Map<String, SiteDetails> getSiteMasterMap() {
        return this.siteMasterMap;
    }

    public void setSiteMasterMap(Map<String, SiteDetails> siteMasterMap) {
        this.siteMasterMap = siteMasterMap;
    }

    public SiteDetails getSiteDetails() {
        return siteDetails;
    }

    public void setSiteDetails(SiteDetails siteDetails) {
        this.siteDetails = siteDetails;
    }

    public SiteStats getSiteStats() {
        return siteStats;
    }

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("marks process successful when processor achieves success condition.")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("marks process failure when processor achieves failure condition.")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE);
        this.relationships = Set.of(REL_SUCCESS, REL_FAILURE);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        setMongoClientService(context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class));
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        List<ClinicProhealth> failedClinicProhealthList = new ArrayList<>();

        try {
            logMetaData.put("processor_name", getProcessorName());

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            ffinput = gson.fromJson(ffContent, FFInput.class);

            MongoClient mongoClient = getMongoClientService().getMongoClient();
            setMongoClient(mongoClient);

            setSiteMasterMap(SiteMaster.loadSiteMasterMap(mongoClient, logMetaData, this.getLogUtil()));
            SiteDetails siteDetails = getSiteDetailsForSiteApiKey(ffinput.siteApiKey);
            this.setSiteDetails(siteDetails);
            if (siteDetails == null)
                throw new Exception("invalid siteDetails for " + ffinput.siteApiKey);

            this.clinicProhealthList = ffinput.clinicprohealth;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            totalProcessed = this.clinicProhealthList.size();
            while (!this.clinicProhealthList.isEmpty()) {
                ClinicProhealth dbPatient = this.clinicProhealthList.get(0);

                try {
                    String uhid = dbPatient.getMr_no();
                    logMetaData.put("uhid", uhid);
                    String siteId = getSiteKeyFromUhid(uhid, logMetaData);

                    if (siteId == null || siteId.trim().length() == 0) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ClinicsProHealth : SiteName %s Uhid %s is Null", siteDetails.getSiteName(), uhid), Level.ERROR, null);
                        this.clinicProhealthList.remove(dbPatient);
                        continue;
                    }

                    DBUser dbUser = identityDao.findByUhid(uhid, siteDetails.getSiteKey());
                    if (dbUser == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ClinicsProHealth: Unable to find UHID: %s in site: %s ", uhid, siteId), Level.INFO, null);
                        failedClinicProhealthList.add(dbPatient);
                        this.clinicProhealthList.remove(dbPatient);
                        continue;
                    }

                    Date testDate = null;
                    try {
                        testDate = dateformat.parse(getDateWithoutGMT(dbPatient.getCreatedOn()));
                    } catch (Exception e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ClinicsProHealth: Could not parse test date %s for UHID %s", dbPatient.getCreatedOn(), uhid), Level.ERROR, null);
                        this.clinicProhealthList.remove(dbPatient);
                        continue;
                    }

                    DBProHealth dbProHealth = dbUser.getProHealth();
                    if (dbProHealth == null) {
                        dbProHealth = new DBProHealth();
                        dbUser.setProHealth(dbProHealth);
                    } else {
                        if (dbProHealth.getTestDate().after(testDate)) {
                            this.clinicProhealthList.remove(dbPatient);
                            continue;
                        }
                    }

                    dbProHealth.setTestDate(testDate);

                    Object guestLocation = dbPatient.getCityName();
                    if (guestLocation != null)
                        dbProHealth.setGuestLocation(guestLocation.toString());
                    Object ahcno = dbPatient.getVisit_id();
                    if (ahcno != null)
                        dbProHealth.setAhcno(ahcno.toString());
                    Object packagename = dbPatient.getAct_description();
                    if (packagename != null)
                        dbProHealth.setPackageName(packagename.toString());
                    Object proHealthLocation = dbPatient.getHospitalName();
                    if (proHealthLocation != null)
                        dbProHealth.setProHealthLocation(proHealthLocation.toString());
                    // Object contactPreference = dbPatient.get(ClinicsProHealth.CONTACTPREFERENCE.name());  NOT CAPTURED IN ASKAPOLLO
                    // if(contactPreference != null)
                    // 	dbProHealth.setContactPreference(contactPreference.toString());
                    Object ahcphysician = dbPatient.getRef_doct_name();
                    if (ahcphysician != null)
                        dbProHealth.setAhcphysician(ahcphysician.toString());
                    Object doctorsSpeciality = dbPatient.getSpecialityName();
                    if (doctorsSpeciality != null)
                        dbProHealth.setDoctorsSpeciality(doctorsSpeciality.toString());

                    Object vitalsJSON = dbPatient.getMSCardiacScoreRequest();

                    if (vitalsJSON != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode actualObjs = mapper.valueToTree(vitalsJSON);
                        String value = mapper.writeValueAsString(actualObjs);
                        String act = value.substring(2, value.length() - 2);
                        act = act.replaceAll("\\\\", "");

                        JsonNode actualObj = mapper.readTree(act);

                        JsonNode weight = actualObj.get(ClinicsProHealth.Weight.name());
                        if (weight != null)
                            dbProHealth.setWeight(weight.asText());
                        JsonNode height = actualObj.get(ClinicsProHealth.Height.name());
                        if (height != null)
                            dbProHealth.setHeight(height.asText());
                        JsonNode bmi = actualObj.get(ClinicsProHealth.BMI.name());
                        if (bmi != null)
                            dbProHealth.setBmi(bmi.asText());
                        JsonNode diastolicBP = actualObj.get(ClinicsProHealth.BloodPressureDiastolic.name());
                        if (diastolicBP != null)
                            dbProHealth.setDiastolicBP(diastolicBP.asText());
                        JsonNode systolicBP = actualObj.get(ClinicsProHealth.BloodPressureSystolic.name());
                        if (systolicBP != null)
                            dbProHealth.setSystolicBP(systolicBP.asText());
                    }

                    Object hraJSON = dbPatient.getComprehensiveHRASubmitPayLoad();
                    if (hraJSON != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode actualObj = mapper.valueToTree(hraJSON);

                        String value = mapper.writeValueAsString(actualObj);
                        String act = value.substring(1, value.length() - 1);
                        act = act.replaceAll("\\\\", "");

                        JsonNode hraJSONnode = mapper.readTree(act);

                        JsonNode docReviewJSON = hraJSONnode.get(ClinicsProHealth.doctorReviewPHRA.name());

                        if (docReviewJSON != null) {
                            JsonNode dietChanges = docReviewJSON.get(ClinicsProHealth.DocRev_AdviceOnDiet.name());

                            if (dietChanges != null) {
                                String diet = "";
                                if (dietChanges.isArray()) {
                                    for (JsonNode jsonNode : dietChanges) {
                                        String stringValue = jsonNode.get(ClinicsProHealth.value.name()).asText();
                                        diet = stringValue + "," + diet;
                                    }
                                }
                                dbProHealth.setDietChanges(diet);
                            }

                            JsonNode activityChanges = docReviewJSON.get(ClinicsProHealth.DocRev_AdviceOnPhysicalActivity.name());
                            if (activityChanges != null) {
                                String activity = "";
                                if (activityChanges.isArray()) {
                                    for (JsonNode jsonNode : activityChanges) {
                                        String stringValue = jsonNode.get(ClinicsProHealth.value.name()).asText();
                                        activity = stringValue + "," + activity;
                                    }
                                }
                                dbProHealth.setActivityChanges(activity);
                            }

                            JsonNode otherLifeStyleChanges = docReviewJSON.get(ClinicsProHealth.OtherLifeStyleChangesAndRecommendations.name());
                            if (otherLifeStyleChanges != null)
                                dbProHealth.setOtherLifeStyleChanges(otherLifeStyleChanges.toString());


                            JsonNode followUpAndReviewPlans = docReviewJSON.get(ClinicsProHealth.FollowUpAndReviewPlans.name());
                            if (followUpAndReviewPlans != null)
                                dbProHealth.setFollowupPreview(followUpAndReviewPlans.toString());

                            JsonNode recommended_consultations = docReviewJSON.get(ClinicsProHealth.ReCommended_Consultations.name());
                            if (recommended_consultations != null)
                                dbProHealth.setConsultationDetail(recommended_consultations.toString());

                            JsonNode recommended_diagnostics = docReviewJSON.get(ClinicsProHealth.ReCommended_Diagnostics.name());
                            if (recommended_diagnostics != null)
                                dbProHealth.setInvestigationsDetail(recommended_diagnostics.toString());

                        }

                        Object allergy = dbPatient.getAllergies();
                        if (allergy != null)
                            dbProHealth.setAllergy(allergy.toString());
                    }

                    dbProHealth.setDbUser(dbUser);
                    dbUser.setUpdatedAt(new Date());
                    proHealthDao.save(dbProHealth);
                    userDao.save(dbUser);
                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ClinicsProhealth : Saved clinics pro Health Data for Uhid %s", uhid), Level.INFO, null);
                    this.clinicProhealthList.remove(dbPatient);
                    successCount++;
                    notificationDao.addNotification(dbUser, "UHID - " + uhid + " from " + siteDetails.getSiteName() + " - Imported ClinicsProHealth record", NotificationLevel.System, NotificationType.HealthCheck, siteId, null, uhid);
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedClinicProhealthList.add(dbPatient);
                    this.clinicProhealthList.remove(dbPatient);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedClinicProhealthList.add(dbPatient);
                    this.clinicProhealthList.remove(dbPatient);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedClinicProhealthList.isEmpty()) {
                ffinput.clinicprohealth = failedClinicProhealthList;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.clinicprohealth = this.clinicProhealthList;
        } catch (Exception ex) {
            setFlowFileDate(inputFF, session);
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }

        String flowFileContent = gson.toJson(ffinput);
        session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
        session.transfer(inputFF, REL_SUCCESS);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }

    private void log_site_processing_time(Long duration, int totalProcessed, int successCount, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("site_name", this.siteDetails.getSiteName());
        otherDetails.put("site_key", this.siteDetails.getSiteKey());
        otherDetails.put("total_processed", totalProcessed);
        otherDetails.put("success_count", successCount);
        otherDetails.put("failed_count", totalProcessed - successCount);
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, this.siteDetails.getSiteName() + " processed successfully.", Level.INFO, otherDetails);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }


    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, new InputStreamCallback() {
            @Override
            public void process(InputStream in) throws IOException {
                StreamUtils.fillBuffer(in, buffer);
            }
        });

        return new String(buffer, charset);
    }

    public String getSiteKeyFromUhid(String uhid, Map<String, Object> logMetaData) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Improper UHID: " + uhid + ", unable to split it.", Level.ERROR, null);
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                if (this.getSiteDetails().isDebug())
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not find the site for UHID: " + uhid, Level.ERROR, null);
                return null;
            }
            return uhidSite.getSiteKey();
        }

        return this.getSiteDetails().getSiteKey();
    }

    public SiteDetails getSiteBasedOnPrefix(String sitePrefix) {
        for (String siteKey : this.getSiteMasterMap().keySet()) {
            SiteDetails stDetails = this.getSiteMasterMap().get(siteKey);
            if (stDetails.getUhidPrefix() != null &&
                    stDetails.getUhidPrefix().equals(sitePrefix))
                return stDetails;
        }
        return null;
    }

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    public String getDateWithoutGMT(String dt) {
        int pos = dt.indexOf('T');
        if (pos == -1) {
            dt = dt.replace(' ', 'T');
        }

        pos = dt.indexOf('+');
        if (pos != -1)
            dt = dt.substring(0, dt.indexOf('+'));

        if (dt.endsWith(".0"))
            dt = dt.substring(0, dt.length() - 2);

        //remove milliseconds
        pos = dt.indexOf('.');
        if (pos != -1)
            dt = dt.substring(0, dt.indexOf('.'));

        return (dt);
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
    }

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.proHealthDao = new ProHealthDao(client);
        this.notificationDao = new NotificationDao(client);
    }
}