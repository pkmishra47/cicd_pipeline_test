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
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.HomeHealthDao;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.DBHomeHealth;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.HomehealthVisit;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
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
@Tags({"dax", "operations", "dataman", "home", "health", "visits", "oldsite"})
@CapabilityDescription("")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class ProcessHomeHealthVisits extends AbstractProcessor {

    private static final String processorName = "ProcessHomeHealthVisits";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private HomeHealthDao homeHealthDao = null;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;

    private MongoClient mongoClient = null;
    protected MongoClientService mongoClientService;

    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public String getProcessorName() {
        return ProcessHomeHealthVisits.processorName;
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
        logMetaData.put("processor_name", this.getProcessorName());
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        List<HomehealthVisit> failedHomehealthVisit = new ArrayList<>();

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

            List<HomehealthVisit> homehealthVisitList = ffinput.homehealthVisit;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            totalProcessed = homehealthVisitList.size();
            while (!homehealthVisitList.isEmpty()) {

                HomehealthVisit dbOrig = homehealthVisitList.get(0);
                try {
                    String uhid = dbOrig.getUhid();
                    logMetaData.put("uhid", uhid);
                    String siteId = getSiteKeyFromUhid(uhid, logMetaData);
                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);

                    if (dbUser == null) {
                        throw new Exception("User doesn't exists for uhid: " + uhid);
                    }

                    DBHomeHealth dbHomeHealth = new DBHomeHealth();
                    try {
                        dbHomeHealth.setCheckupDate(dateformat.parse(getDateWithoutGMT(dbOrig.getCREATED_AT())));
                    } catch (Exception exception) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse CREATED_AT {} for UHID {}", dbOrig.getCREATED_AT(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setTemperature(Float.parseFloat(dbOrig.getTEMP()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  Temperature {} for UHID {}", dbOrig.getTEMP(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setWeight(Float.parseFloat(dbOrig.getWEIGHT()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  Weight {} for UHID {}", dbOrig.getWEIGHT(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setPulse(Integer.parseInt(dbOrig.getPULSE()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  Pulse {} for UHID {}", dbOrig.getPULSE(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setBloodPressureSystolic(Integer.parseInt(dbOrig.getMAX()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  MAX {} for UHID {}", dbOrig.getMAX(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setBloodPressureDiastolic(Integer.parseInt(dbOrig.getMIN()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  MIN {} for UHID {}", dbOrig.getMIN(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setRespiration(Integer.parseInt(dbOrig.getRESPIRATION()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  RESPIRATION {} for UHID {}", dbOrig.getRESPIRATION(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setPainScore(Integer.parseInt(dbOrig.getPAIN_SCORE()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  PAIN_SCORE {} for UHID {}", dbOrig.getPAIN_SCORE(), uhid), Level.ERROR, null);
                    }

                    try {
                        dbHomeHealth.setSpO2(Integer.parseInt(dbOrig.getSPO2()));
                    } catch (NumberFormatException e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  SPO2 {} for UHID {}", dbOrig.getSPO2(), uhid), Level.ERROR, null);
                    }


                    if (dbOrig.getOXYGEN_USE().equals("1")) {
                        dbHomeHealth.setOxygenUse(true);
                        try {
                            dbHomeHealth.setOxygenValue(Integer.parseInt(dbOrig.getLITRES()));
                        } catch (NumberFormatException e) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("HomeCare: Could not parse  LITRES {} for UHID {}", dbOrig.getLITRES(), uhid), Level.ERROR, null);
                        }

                        if (dbOrig.getDEVICE_TYPE() != null)
                            dbHomeHealth.setOxygenDevice(dbOrig.getDEVICE_TYPE());
                    } else {
                        dbHomeHealth.setOxygenUse(false);
                        dbHomeHealth.setOxygenValue(0);
                    }

                    String remarks = "";
                    String val = "";
                    if (dbOrig.getCOMMENTS() != null) {
                        val = dbOrig.getCOMMENTS();
                        if (val.length() != 0)
                            remarks += "Comments: " + val + "<br/>";
                    }

                    if (dbOrig.getPLAN() != null) {
                        val = dbOrig.getPLAN();
                        if (val.length() != 0)
                            remarks += "Plan: " + val + "<br/>";
                    }

                    if (dbOrig.getCHEIF_COMP() != null) {
                        val = dbOrig.getCHEIF_COMP();
                        if (val.length() != 0)
                            remarks += "Chief Complaint: " + val + "<br/>";
                    }

                    if (dbOrig.getSUBJECTIVE() != null) {
                        val = dbOrig.getSUBJECTIVE();
                        if (val.length() != 0)
                            remarks += "Subjective: " + val + "<br/>";
                    }

                    dbHomeHealth.setRemarks(remarks);
                    homeHealthDao.save(dbHomeHealth);
                    dbUser.getHomeHealth().add(dbHomeHealth);

                    if (!dbUser.getRoles().contains("HomeHealth"))
                        dbUser.getRoles().add("HomeHealth");

                    userDao.save(dbUser);
                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Homehealthvisit : Saved home health visit Data for Uhid %s", uhid), Level.INFO, null);
                    homehealthVisitList.remove(dbOrig);
                    successCount++;
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedHomehealthVisit.add(dbOrig);
                    homehealthVisitList.remove(dbOrig);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedHomehealthVisit.add(dbOrig);
                    homehealthVisitList.remove(dbOrig);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedHomehealthVisit.isEmpty()) {
                ffinput.homehealthVisit = failedHomehealthVisit;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.homehealthVisit = homehealthVisitList;
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

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
    }

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.homeHealthDao = new HomeHealthDao(client);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        if (inputFF == null) {
            return null;
        }
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
}