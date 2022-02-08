package org.apache.nifi.processors.daxoperation;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
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
import org.apache.nifi.processor.util.StandardValidators;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "operations", "dataman", "prohealth", "oldsite"})
@CapabilityDescription("")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class ProcessProHealth extends AbstractProcessor {

    private static final String processorName = "ProcessProHealth";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private boolean debugOn = false;
    private static Gson gson = null;
    private ServiceUtil serviceUtil = null;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private ProHealthDao proHealthDao = null;
    private NotificationDao notificationDao = null;

    private MongoClient mongoClient = null;
    protected MongoClientService mongoClientService;
    private String hMDashboardPushApi = "";
    private String hMDashboardApiToken = "";

    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat billDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String getProcessorName() {
        return ProcessProHealth.processorName;
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

    public Gson getGson() {
        if (ProcessProHealth.gson == null)
            ProcessProHealth.gson = GsonUtil.getGson();
        return ProcessProHealth.gson;
    }

    public String gethMDashboardPushApi() {
        return hMDashboardPushApi;
    }

    public void sethMDashboardPushApi(String hMDashboardPushApi) {
        this.hMDashboardPushApi = hMDashboardPushApi;
    }

    public String gethMDashboardApiToken() {
        return hMDashboardApiToken;
    }

    public void sethMDashboardApiToken(String hMDashboardApiToken) {
        this.hMDashboardApiToken = hMDashboardApiToken;
    }


    public ServiceUtil getServiceUtil() {
        if (this.serviceUtil == null)
            this.serviceUtil = new ServiceUtil();
        return this.serviceUtil;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor HMDASHBOARD_PUSH_API = new PropertyDescriptor
            .Builder()
            .name("HMDASHBOARD_PUSH_API")
            .displayName("HMDASHBOARD_PUSH_API")
            .description("provide HMDASHBOARD_PUSH_API.")
            .required(false)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor HMDASHBOARD_API_TOKEN = new PropertyDescriptor
            .Builder()
            .name("HMDASHBOARD_API_TOKEN")
            .displayName("HMDASHBOARD_API_TOKEN")
            .description("provide HMDASHBOARD_API_TOKEN.")
            .required(false)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEBUG = new PropertyDescriptor
            .Builder().name("DEBUG")
            .displayName("DEBUG")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .description("Default : false")
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, HMDASHBOARD_PUSH_API, HMDASHBOARD_API_TOKEN, DEBUG);
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

        if (context.getProperty(DEBUG).evaluateAttributeExpressions().getValue() != null)
            debugOn = Boolean.parseBoolean(context.getProperty(DEBUG).evaluateAttributeExpressions().getValue());

        if (context.getProperty(HMDASHBOARD_PUSH_API).evaluateAttributeExpressions().getValue() != null)
            hMDashboardPushApi = context.getProperty(HMDASHBOARD_PUSH_API).evaluateAttributeExpressions().getValue();

        if (context.getProperty(HMDASHBOARD_API_TOKEN).evaluateAttributeExpressions().getValue() != null)
            hMDashboardApiToken = context.getProperty(HMDASHBOARD_API_TOKEN).evaluateAttributeExpressions().getValue();
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", getProcessorName());
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        List<ProHealth> failedProHealthList = new ArrayList<>();

        try {

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            ffinput = this.getGson().fromJson(ffContent, FFInput.class);

            MongoClient mongoClient = getMongoClientService().getMongoClient();
            setMongoClient(mongoClient);

            setSiteMasterMap(SiteMaster.loadSiteMasterMap(mongoClient, logMetaData, this.getLogUtil()));
            SiteDetails siteDetails = getSiteDetailsForSiteApiKey(ffinput.siteApiKey);
            this.setSiteDetails(siteDetails);
            if (siteDetails == null)
                throw new Exception("invalid siteDetails for " + ffinput.siteApiKey);

            List<ProHealth> proHealthList = ffinput.prohealth;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            totalProcessed = proHealthList.size();
            while (!proHealthList.isEmpty()) {
                ProHealth proHealthObj = proHealthList.get(0);

                try {
                    String uhid = proHealthObj.getUHID();
                    String siteId = getSiteKeyFromUhid(uhid, logMetaData);
                    logMajorDetails(logMetaData, proHealthObj);

                    if (debugOn)
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "data : " + this.getGson().toJson(proHealthObj), Level.INFO, null);

                    if (siteId == null || siteId.trim().length() == 0) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ProHealth : SiteName %s Uhid %s is Null", siteDetails.getSiteName(), uhid), Level.WARN, null);
                        proHealthList.remove(proHealthObj);
                        continue;
                    }

                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);

                    if (dbUser == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("ProHealth: Unable to find UHID: %s in site: %s ", uhid, siteId), Level.INFO, null);
                        failedProHealthList.add(proHealthObj);
                        proHealthList.remove(proHealthObj);
                        continue;
                    }

                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("User found for Uhid %s", uhid), Level.INFO, null);
                    Date testDate = null;
                    if (proHealthObj.getCREATEDATEPOCH() != null && !(proHealthObj.getCREATEDATEPOCH()).isEmpty()) {
                        try {
                            testDate = dateformat.parse(getDateWithoutGMT(proHealthObj.getCREATEDATEPOCH()));
                        } catch (Exception ex) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Unable to parse CREATEDATEPOCH " + proHealthObj.getCREATEDATEPOCH() + " for " + uhid, Level.INFO, null);
                            proHealthList.remove(proHealthObj);
                            continue;
                        }
                    }

                    DBProHealth dbProHealth = dbUser.getProHealth();
                    if (dbProHealth == null) {
                        dbProHealth = new DBProHealth();
                        dbUser.setProHealth(dbProHealth);
                    } else {
                        if (testDate != null && dbProHealth.getTestDate() != null && dbProHealth.getTestDate().after(testDate)) {
                            proHealthList.remove(proHealthObj);
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "TestDate is smaller than already saved object test date." + proHealthObj.getCREATEDATEPOCH() + " for " + uhid, Level.INFO, null);
                            continue;
                        }
                    }

                    if (testDate != null)
                        dbProHealth.setTestDate(testDate);

                    String guestLocation = proHealthObj.getGUEST_LOCATION();
                    if (guestLocation != null)
                        dbProHealth.setGuestLocation(guestLocation);
                    String ahcno = proHealthObj.getAHCNO();
                    if (ahcno != null)
                        dbProHealth.setAhcno(ahcno);
                    String packagename = proHealthObj.getPACKAGENAME();
                    if (packagename != null && !packagename.isEmpty())
                        dbProHealth.setPackageName(packagename);
                    String proHealthLocation = proHealthObj.getPROHEALTHLOCATION();
                    if (proHealthLocation != null)
                        dbProHealth.setProHealthLocation(proHealthLocation);
                    String contactPreference = proHealthObj.getCONTACTPREFERENCE();
                    if (contactPreference != null)
                        dbProHealth.setContactPreference(contactPreference);
                    String ahcphysician = proHealthObj.getAHCPHYSICIAN();
                    if (ahcphysician != null)
                        dbProHealth.setAhcphysician(ahcphysician);
                    String doctorsSpeciality = proHealthObj.getDOCTORS_SPECIALITY();
                    if (doctorsSpeciality != null)
                        dbProHealth.setDoctorsSpeciality(doctorsSpeciality);
                    String height = proHealthObj.getHEIGHT();
                    if (height != null)
                        dbProHealth.setHeight(height);
                    String weight = proHealthObj.getWEIGHT();
                    if (weight != null)
                        dbProHealth.setWeight(weight);
                    String vip = proHealthObj.getVIP();
                    if (vip != null)
                        dbProHealth.setVip(vip);
                    String impression = proHealthObj.getIMPRESSION();
                    if (impression != null)
                        dbProHealth.setImpression(impression);
                    String dietChanges = proHealthObj.getDIETCHANGES();
                    if (dietChanges != null)
                        dbProHealth.setDietChanges(dietChanges);
                    String activityChanges = proHealthObj.getACTIVITYCHANGES();
                    if (activityChanges != null)
                        dbProHealth.setActivityChanges(activityChanges);
                    String otherLifeStyleChanges = proHealthObj.getOTHERLIFESTYLECHANGES();
                    if (otherLifeStyleChanges != null)
                        dbProHealth.setOtherLifeStyleChanges(otherLifeStyleChanges);
                    String followupPreview = proHealthObj.getFOLLOWUPREVIEW();
                    if (followupPreview != null)
                        dbProHealth.setFollowupPreview(followupPreview);
                    String allergy = proHealthObj.getALLERGY();
                    if (allergy != null)
                        dbProHealth.setAllergy(allergy);
                    String investigationsDetail = proHealthObj.getINVESTIGATIONS_DETAIL();
                    if (investigationsDetail != null)
                        dbProHealth.setInvestigationsDetail(investigationsDetail);
                    String consultationDetail = proHealthObj.getCONSULTATION_DETAIL();
                    if (consultationDetail != null)
                        dbProHealth.setConsultationDetail(consultationDetail);
                    String systolicBP = proHealthObj.getSYSTOLIC_BP();
                    if (systolicBP != null)
                        dbProHealth.setSystolicBP(systolicBP);
                    String diastolicBP = proHealthObj.getDIASTOLIC_BP();
                    if (diastolicBP != null)
                        dbProHealth.setDiastolicBP(diastolicBP);
                    String bmi = proHealthObj.getBMI();
                    if (bmi != null)
                        dbProHealth.setBmi(bmi);

                    String hypertension = proHealthObj.getHypertension();
                    if (hypertension != null)
                        dbProHealth.setHypertension(hypertension);
                    String dyslipidaemia = proHealthObj.getDYSLIPIDAEMIA();
                    if (dyslipidaemia != null)
                        dbProHealth.setDysplipideamia(dyslipidaemia);
                    String bmi_category = proHealthObj.getBMI_CATEGORY();
                    if (bmi_category != null)
                        dbProHealth.setBmiCategory(bmi_category);
                    String metabolic_syndrome = proHealthObj.getMetabolic_Syndrome();
                    if (metabolic_syndrome != null)
                        dbProHealth.setMetabolicSyndrome(metabolic_syndrome);
                    String diabetes = proHealthObj.getDIABETES();
                    if (diabetes != null)
                        dbProHealth.setDiabetes(diabetes);
                    String cardiac_risk = proHealthObj.getCARDIAC_RISK();
                    if (cardiac_risk != null)
                        dbProHealth.setCardiacRisk(cardiac_risk);
                    String status = proHealthObj.getSTATUS();
                    if (status != null)
                        dbProHealth.setStatus(status);

                    String packageId = proHealthObj.getPACKAGE_ID();
                    if (packageId != null)
                        dbProHealth.setPackageId(packageId);
                    String packageName = proHealthObj.getPACKAGE_NAME();
                    if (packageName != null && !packageName.isEmpty())
                        dbProHealth.setPackageName(packageName);
                    String billDate = proHealthObj.getBILLDATE();
                    if (billDate != null && !billDate.isEmpty()) {
                        Date dtBillDate = null;
                        try {
                            dtBillDate = billDateFormat.parse(billDate);
                        } catch (Exception ignored) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Unable to parse BILLDATE " + billDate + " for " + uhid, Level.INFO, null);
                        }
                        if (dtBillDate != null)
                            dbProHealth.setBillDate(dtBillDate);
                    }

                    Object overAllSeverity = proHealthObj.getOVERALLSEVERITY();
                    if (overAllSeverity != null)
                        dbProHealth.setOverAllSeverity(overAllSeverity.toString());
                    Object callTag = proHealthObj.getCALLTAG();
                    if (callTag != null)
                        dbProHealth.setCallTag(callTag.toString());
                    Object packageCategory = proHealthObj.getPACKAGECATEGORY();
                    if (packageCategory != null)
                        dbProHealth.setPackageCategory(packageCategory.toString());

                    dbProHealth.setDbUser(dbUser);
                    dbProHealth.setUpdatedAt(new Date());
                    dbUser.setUpdatedAt(new Date());

                    proHealthDao.save(dbProHealth);
                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ProHealth : Saved pro Health Data for Uhid %s", uhid), Level.INFO, null);
                    userDao.save(dbUser);
                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("ProHealth : Saved User Data for Uhid %s", uhid), Level.INFO, null);
                    proHealthList.remove(proHealthObj);
                    successCount++;
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                    pushProhealthInfoToHMDashboard(dbUser, proHealthObj, dbProHealth, logMetaData);
                    try {
                        Date updatedDate = null;
                        if (proHealthObj.getFINALIZEUPDATEDATE() != null)
                            updatedDate = dateformat.parse(getDateWithoutGMT(proHealthObj.getFINALIZEUPDATEDATE()));
                        else if (proHealthObj.getBILLDATE() != null)
                            updatedDate = billDateFormat.parse(proHealthObj.getBILLDATE());

                        if (updatedDate != null)
                            logTurnAroundTime(updatedDate, logMetaData);
                    } catch (Exception e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.INFO, null);
                    }

                    this.notificationDao.addNotification(dbUser, "UHID - " + uhid + " from " + siteDetails.getSiteName() + " - Imported Pro Health record", NotificationLevel.System, NotificationType.HealthCheck, siteId, null, uhid);
                } catch (ConcurrentModificationException curEx) {
                    failedProHealthList.add(proHealthObj);
                    proHealthList.remove(proHealthObj);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedProHealthList.add(proHealthObj);
                    proHealthList.remove(proHealthObj);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedProHealthList.isEmpty()) {
                ffinput.prohealth = failedProHealthList;
                String flowFileContent = this.getGson().toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.prohealth = proHealthList;

        } catch (Exception ex) {
            setFlowFileDate(inputFF, session);
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }

        String flowFileContent = this.getGson().toJson(ffinput);
        session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
        session.transfer(inputFF, REL_SUCCESS);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
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

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }

    private void logTurnAroundTime(Date jsonUpdatedDate, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        Date currentDate = new Date();
        long tat = (currentDate.getTime() - jsonUpdatedDate.getTime()) / 1000;
        otherDetails.put("receivedDate", jsonUpdatedDate);
        otherDetails.put("processedDate", currentDate);
        otherDetails.put("turn_around_time", tat);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Prohealth: turn around time is: " + tat, Level.INFO, otherDetails);
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
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not find the site for UHID: " + uhid, Level.WARN, null);
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

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.proHealthDao = new ProHealthDao(client);
        this.notificationDao = new NotificationDao(client);
    }

    private void pushProhealthInfoToHMDashboard(DBUser dbUser, ProHealth prohealthObj, DBProHealth dbProHealth, Map<String, Object> logMetaData) {
        String prohealthRequestbody = createProhealthRequestbody(dbUser, prohealthObj, dbProHealth);
        boolean hasPushed = false;

        if (this.debugOn)
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "created request body for hmdashboard api call. Body: \n" + prohealthRequestbody, Level.INFO, null);

        if (!this.gethMDashboardPushApi().isEmpty() && !this.gethMDashboardApiToken().isEmpty())
            hasPushed = this.getServiceUtil().sendProhealthInfoToHMDashboard(this.gethMDashboardPushApi(), this.gethMDashboardApiToken(), prohealthRequestbody, logMetaData);
        else
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "either hmdashboard push api url or key is empty. APIURL: " + this.gethMDashboardPushApi() + " ,key:" + this.gethMDashboardApiToken(), Level.INFO, null);

        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("hasPushed", hasPushed);
        if (!hasPushed)
            otherDetails.put("body", prohealthRequestbody);

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Info related to prohealth data", Level.INFO, otherDetails);
    }

    private String createProhealthRequestbody(DBUser dbUser, ProHealth prohealthObj, DBProHealth dbProHealth) {
        ProhealthPatientInfo prohealthPatientInfo = new ProhealthPatientInfo();
        prohealthPatientInfo.setPatientId(dbUser.getId().toString());
        prohealthPatientInfo.setUhid(prohealthObj.getUHID());
        prohealthPatientInfo.setFirstName(dbUser.getFirstName());
        prohealthPatientInfo.setMiddleName(dbUser.getMiddleName());
        prohealthPatientInfo.setLastName(dbUser.getLastName());
        prohealthPatientInfo.setEmail(dbUser.getUserContactInfo() != null ? dbUser.getUserContactInfo().getEmail1() : "");
        prohealthPatientInfo.setGender(dbUser.getUserBasicInfo() != null ? dbUser.getUserBasicInfo().getSex() : "");
        prohealthPatientInfo.setMobile(dbUser.getMobileNumber());
        prohealthPatientInfo.setPackageName(dbProHealth.getPackageName());
        prohealthPatientInfo.setPackageId(dbProHealth.getPackageId());
        prohealthPatientInfo.setPackageDate(dbProHealth.getBillDate() != null ? dbProHealth.getBillDate().toString() : "");
        prohealthPatientInfo.setRegistrationDate(dbUser.getDateImported() != null ? dbUser.getDateImported().toString() : "");
        prohealthPatientInfo.setDob(dbUser.getUserBasicInfo() != null ? (dbUser.getUserBasicInfo().getDateOfBirth() != null ? dbUser.getUserBasicInfo().getDateOfBirth().toString() : "") : "");

        if (dbProHealth.getTestDate() != null)
            prohealthPatientInfo.setTestDate(dbProHealth.getTestDate().getTime());
        prohealthPatientInfo.setGuestLocation(dbProHealth.getGuestLocation());
        prohealthPatientInfo.setAhcno(dbProHealth.getAhcno());
        prohealthPatientInfo.setPackageName(dbProHealth.getPackageName());
        prohealthPatientInfo.setProHealthLocation(dbProHealth.getProHealthLocation());
        prohealthPatientInfo.setContactPreference(dbProHealth.getContactPreference());
        prohealthPatientInfo.setAhcphysician(dbProHealth.getAhcphysician());
        prohealthPatientInfo.setDoctorsSpeciality(dbProHealth.getDoctorsSpeciality());
        prohealthPatientInfo.setHeight(dbProHealth.getHeight());
        prohealthPatientInfo.setWeight(dbProHealth.getWeight());
        prohealthPatientInfo.setImpression(dbProHealth.getImpression());
        prohealthPatientInfo.setDietChanges(dbProHealth.getDietChanges());
        prohealthPatientInfo.setActivityChanges(dbProHealth.getActivityChanges());
        prohealthPatientInfo.setOtherLifeStyleChanges(dbProHealth.getOtherLifeStyleChanges());
        prohealthPatientInfo.setFollowupPreview(dbProHealth.getFollowupPreview());
        prohealthPatientInfo.setAllergy(dbProHealth.getAllergy());
        prohealthPatientInfo.setSystolicBP(dbProHealth.getSystolicBP());
        prohealthPatientInfo.setDiastolicBP(dbProHealth.getDiastolicBP());
        prohealthPatientInfo.setBmi(dbProHealth.getBmi());
        prohealthPatientInfo.setHypertension(dbProHealth.getHypertension());
        prohealthPatientInfo.setDysplipideamia(dbProHealth.getDysplipideamia());
        prohealthPatientInfo.setBmi_category(dbProHealth.getBmiCategory());
        prohealthPatientInfo.setMetabolic_syndrome(dbProHealth.getMetabolicSyndrome());
        prohealthPatientInfo.setDiabetes(dbProHealth.getDiabetes());
        prohealthPatientInfo.setCardiac_risk(dbProHealth.getCardiacRisk());
        prohealthPatientInfo.setStatus(dbProHealth.getStatus());
        prohealthPatientInfo.setVip(dbProHealth.getVip());

        prohealthPatientInfo.setBillDate(dbProHealth.getBillDate() != null ? dbProHealth.getBillDate().toString() : "");
        prohealthPatientInfo.setFinalizeDate(prohealthObj.getFINALIZEDATE());
        prohealthPatientInfo.setFinalizeUpdateDate(prohealthObj.getFINALIZEUPDATEDATE());
        prohealthPatientInfo.setCreatedatepoch(prohealthObj.getCREATEDATEPOCH());
        prohealthPatientInfo.setUpdatedatepoch(prohealthObj.getUPDATEDATEPOCH());

        return this.getGson().toJson(prohealthPatientInfo);
    }

    private void logMajorDetails(Map<String, Object> logMetaData, ProHealth proHealthObj) {
        logMetaData.put("uhid", proHealthObj.getUHID());
        logMetaData.put("isBillingData", (proHealthObj.getBILLDATE() != null));

        try {
            if (proHealthObj.getBILLDATE() != null && !proHealthObj.getBILLDATE().isEmpty()) {
                Date billDate = billDateFormat.parse(proHealthObj.getBILLDATE());
                logMetaData.put("prohealth_billDate", getDateWithoutTime(billDate));
                logMetaData.put("prohealth_billDateTime", proHealthObj.getBILLDATE());
            }

            if (proHealthObj.getCREATEDATEPOCH() != null && !proHealthObj.getCREATEDATEPOCH().isEmpty()) {
                Date createDateEpoch = billDateFormat.parse(proHealthObj.getCREATEDATEPOCH());
                logMetaData.put("prohealth_createDateEpoch", getDateWithoutTime(createDateEpoch));
                logMetaData.put("prohealth_createDateTimeEpoch", proHealthObj.getCREATEDATEPOCH());
            }

            if (proHealthObj.getFINALIZEUPDATEDATE() != null && !proHealthObj.getFINALIZEUPDATEDATE().isEmpty()) {
                Date finalizeUpdateDate = billDateFormat.parse(proHealthObj.getFINALIZEUPDATEDATE());
                logMetaData.put("prohealth_finalizeUpdateDate", getDateWithoutTime(finalizeUpdateDate));
                logMetaData.put("prohealth_finalizeUpdateDateTime", proHealthObj.getFINALIZEUPDATEDATE());
            }

            if (proHealthObj.getFINALIZEDATE() != null && !proHealthObj.getFINALIZEDATE().isEmpty()) {
                Date finalizeDate = billDateFormat.parse(proHealthObj.getFINALIZEDATE());
                logMetaData.put("prohealth_finalizeDate", getDateWithoutTime(finalizeDate));
                logMetaData.put("prohealth_finalizeDateTime", proHealthObj.getFINALIZEDATE());
            }

            if (proHealthObj.getUPDATEDATEPOCH() != null && !proHealthObj.getUPDATEDATEPOCH().isEmpty()) {
                Date updateDateEpoch = billDateFormat.parse(proHealthObj.getUPDATEDATEPOCH());
                logMetaData.put("prohealth_updateDateEpoch", getDateWithoutTime(updateDateEpoch));
                logMetaData.put("prohealth_updateDateTimeEpoch", proHealthObj.getUPDATEDATEPOCH());
            }
        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
        }
    }

    private Date getDateWithoutTime(Date date) {
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);
        calDate.set(Calendar.HOUR_OF_DAY, 0);
        calDate.set(Calendar.MINUTE, 0);
        calDate.set(Calendar.SECOND, 0);
        calDate.set(Calendar.MILLISECOND, 0);
        return calDate.getTime();
    }
}