package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.ConsultationDao;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.DBConsultation;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dm.Consultation;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "operations", "dataman", "consultations", "oldsite"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class ProcessConsultations extends AbstractProcessor {

    private static final String processorName = "ProcessConsultations";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private ConsultationDao consultationDao = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;

    private Map<String, String> doctorMapping = new HashMap<>();

    private MongoClient mongoClient = null;
    protected MongoClientService mongoClientService;

    private SimpleDateFormat pgDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public String getProcessorName() {
        return ProcessConsultations.processorName;
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

    public void setDoctorMapping(Map<String, String> doctorMapping) {
        this.doctorMapping = doctorMapping;
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

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor DOCTOR_MEDMANTRA_CSV = new PropertyDescriptor
            .Builder().name("DOCTOR_MEDMANTRA_LINKING_CSV")
            .displayName("DOCTOR_MEDMANTRA_LINKING_CSV")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .description("Doctor MedMantra Mapping in CSV ex. Medmantra ID,doctorId")
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, DOCTOR_MEDMANTRA_CSV);
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
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", this.getProcessorName());

        setMongoClientService(context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class));
        loadDoctorMapping(context.getProperty(DOCTOR_MEDMANTRA_CSV).toString(), logMetaData);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", this.getProcessorName());
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        List<Consultation> failedConsultationList = new ArrayList<>();

        try {
            if (inputFF == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
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

            List<Consultation> consultationList = ffinput.consultation;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            totalProcessed = consultationList.size();
            while (!consultationList.isEmpty()) {
                Consultation dbConsultation = consultationList.get(0);
                try {
                    if (dbConsultation.getConsultation() != null)
                        dbConsultation = dbConsultation.getConsultation();

                    if (dbConsultation.getUhid() == null) {
                        consultationList.remove(dbConsultation);
                    }

                    String uhid = dbConsultation.getUhid();
                    logMetaData.put("uhid", uhid);

                    if (dbConsultation.getDoctorid() == null) {
                        consultationList.remove(dbConsultation);
                    }

                    String doctorid = dbConsultation.getDoctorid();

                    String siteId = getSiteKeyFromUhid(uhid, logMetaData);
                    if (siteId == null || siteId.trim().length() == 0) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("SiteName %s Uhid %s is Null", siteDetails.getSiteName(), uhid), Level.INFO, null);
                        consultationList.remove(dbConsultation);
                        continue;
                    }

                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                    if (dbUser == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to find UHID: %s in site: %s ", uhid, siteId), Level.INFO, null);
                        failedConsultationList.add(dbConsultation);
                        consultationList.remove(dbConsultation);
                        continue;
                    }

                    DBConsultation DBConsultation = new DBConsultation();
                    String appointmentid = null;
                    if (dbConsultation.getAppointmentid() != null) {
                        appointmentid = dbConsultation.getAppointmentid();
                    }

                    String specialityid = null;
                    if (dbConsultation.getSpecialityid() != null)
                        specialityid = dbConsultation.getSpecialityid();

                    String speciality = null;
                    if (dbConsultation.getSpeciality() != null)
                        speciality = dbConsultation.getSpeciality();

                    String appointmenttype = null;
                    if (dbConsultation.getAppointmenttype() != null)
                        appointmenttype = dbConsultation.getAppointmenttype();

                    String modeofappointment = null;
                    if (dbConsultation.getModeofappointment() != null)
                        modeofappointment = dbConsultation.getModeofappointment();

                    Date consultedtime = null;
                    if (dbConsultation.getConsultedtime() != null)
                        try {
                            consultedtime = dateformat.parse(getDateWithoutGMT(dbConsultation.getConsultedtime()));
                        } catch (Exception exception) {
                            try {
                                consultedtime = pgDateFormat.parse(dbConsultation.getConsultedtime());
                            } catch (Exception expception) {
                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse consultedtime date " + dbConsultation.getConsultedtime() + " for UHID " + uhid, Level.WARN, null);
                            }
                        }

                    String doctor_name = null;
                    if (dbConsultation.getDoctor_name() != null)
                        doctor_name = dbConsultation.getDoctor_name();

                    String locationid = null;
                    if (dbConsultation.getLocationid() != null)
                        locationid = dbConsultation.getLocationid();

                    String location_name = null;
                    if (dbConsultation.getLocation_name() != null)
                        location_name = dbConsultation.getLocation_name();

                    boolean found = false;
                    for (DBConsultation consult : dbUser.getConsultations()) {
                        if (appointmentid != null && appointmentid.equals(consult.getAppointmentid())) {
                            if (locationid != null && locationid.equals(consult.getLocationid())) {
                                if (consultedtime != null && consultedtime.equals(consult.getConsultedtime())) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!found) {
                        DBConsultation.setAppointmentid(appointmentid);
                        DBConsultation.setAppointmenttype(appointmenttype);
                        DBConsultation.setConsultedtime(consultedtime);
                        DBConsultation.setDoctor_name(doctor_name);
                        DBConsultation.setDoctorid(doctorid);
                        DBConsultation.setLocation_name(location_name);
                        DBConsultation.setLocationid(locationid);
                        DBConsultation.setModeofappointment(modeofappointment);
                        DBConsultation.setSpeciality(speciality);
                        DBConsultation.setSpecialityid(specialityid);
                        DBConsultation.setUhid(uhid);
                        if (doctorMapping != null) {
                            DBConsultation.setDoctorid_247(doctorMapping.get(doctorid));
                        }
                        dbUser.getConsultations().add(DBConsultation);
                        dbUser.setUpdatedAt(new Date());
                        consultationDao.save(DBConsultation);
                        userDao.save(dbUser);
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "saved consultation data for uhid: " + uhid + "  ,appointmentid: " + appointmentid + " ,locationid: " + locationid + " ,consultedtime: " + consultedtime, Level.INFO, null);
                    }
                    consultationList.remove(dbConsultation);
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                    successCount++;
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedConsultationList.add(dbConsultation);
                    consultationList.remove(dbConsultation);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedConsultationList.add(dbConsultation);
                    consultationList.remove(dbConsultation);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedConsultationList.isEmpty()) {
                ffinput.consultation = failedConsultationList;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.consultation = consultationList;
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
        outputFF = session.write(outputFF, out -> out.write(data.getBytes(charset)));
        return outputFF;
    }

    public void loadDoctorMapping(String fileName, Map<String, Object> logMetaData) {
        HashMap<String, String> map = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = null;

            while ((line = br.readLine()) != null) {
                String str[] = line.split(",");
                map.put(str[0], str[1]);

            }

        } catch (Exception e) {
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(e), Level.ERROR, null);
        }
        setDoctorMapping(map);
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

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
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

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        if (inputFF == null) {
            return null;
        }
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.consultationDao = new ConsultationDao(client);
    }
}