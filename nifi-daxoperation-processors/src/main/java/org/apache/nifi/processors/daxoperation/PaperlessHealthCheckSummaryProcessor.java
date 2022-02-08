package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
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
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.dao.HealthCheckDao;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.DBAttachement;
import org.apache.nifi.processors.daxoperation.dbo.DBHealthCheck;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Pradeep Mishra
 */
@Tags({"daxoperation", "dataman", "paperless", "healthchecksummary"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class PaperlessHealthCheckSummaryProcessor extends AbstractProcessor {
    private static final String processorName = "PaperlessHealthCheckSummary";
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final SimpleDateFormat logDateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private MongoClient client = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBUtil dbUtil = null;
    private static Gson gson = null;
    private SiteStats siteStats = null;
    private IdentityDao identityDao;
    private HealthCheckDao healthCheckDao;
    private UserDao userDao;
    private String azureConnStr = "";
    private String azureFileShare = "";
    private String azureBaseDirName = "";

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    protected MongoClientService mongoClientService;

    public String getProcessorName() {
        return PaperlessHealthCheckSummaryProcessor.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public Gson getGson() {
        if (PaperlessHealthCheckSummaryProcessor.gson == null)
            PaperlessHealthCheckSummaryProcessor.gson = GsonUtil.getGson();
        return PaperlessHealthCheckSummaryProcessor.gson;
    }

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
    }

    public SiteStats getSiteStats() {
        return this.siteStats;
    }

    public IdentityDao getIdentityDao() {
        return identityDao;
    }

    public void setIdentityDao(IdentityDao identityDao) {
        this.identityDao = identityDao;
    }

    public UserDao getUserDao() {
        return this.userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public HealthCheckDao getHealthCheckDao() {
        return this.healthCheckDao;
    }

    public void setHealthCheckDao(HealthCheckDao healthCheckDao) {
        this.healthCheckDao = healthCheckDao;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public DBUtil getDbUtil() {
        return this.dbUtil;
    }

    public void setDbUtil(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public MongoClient getMongoClient() {
        return this.client;
    }

    public void setMongoClient(MongoClient client) {
        this.client = client;
    }

    public Map<String, SiteDetails> getSiteMasterMap() {
        return this.siteMasterMap;
    }

    public void setSiteMasterMap(Map<String, SiteDetails> siteMasterMap) {
        this.siteMasterMap = siteMasterMap;
    }

    public SiteDetails getSiteDetails() {
        return this.siteDetails;
    }

    public void setSiteDetails(SiteDetails siteDetails) {
        this.siteDetails = siteDetails;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public String getAzureConnStr() {
        return this.azureConnStr;
    }

    public void setAzureConnStr(String connStr) {
        this.azureConnStr = connStr;
    }

    public String getAzureFileShare() {
        return this.azureFileShare;
    }

    public void setAzureFileShare(String azureFileShare) {
        this.azureFileShare = azureFileShare;
    }

    public String getAzureBaseDirName() {
        return this.azureBaseDirName;
    }

    public void setAzureBaseDirName(String azureBaseDirName) {
        this.azureBaseDirName = azureBaseDirName;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor AZURE_CONNECTION_STR = new PropertyDescriptor
            .Builder()
            .name("AZURE_CONNECTION_STR")
            .displayName("AZURE_CONNECTION_STR")
            .description("provide azure connection string.")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor AZURE_FILE_SHARE = new PropertyDescriptor
            .Builder()
            .name("AZURE_FILE_SHARE")
            .displayName("AZURE_FILE_SHARE")
            .description("provide azure file share name")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor AZURE_BASE_DIRECTORY_NAME = new PropertyDescriptor
            .Builder()
            .name("AZURE_BASE_DIRECTORY_NAME")
            .displayName("AZURE_BASE_DIRECTORY_NAME")
            .description("provide azure base directory name")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, AZURE_CONNECTION_STR, AZURE_FILE_SHARE, AZURE_BASE_DIRECTORY_NAME);
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
        mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
        azureConnStr = context.getProperty(AZURE_CONNECTION_STR).evaluateAttributeExpressions().getValue();
        azureFileShare = context.getProperty(AZURE_FILE_SHARE).evaluateAttributeExpressions().getValue();
        azureBaseDirName = context.getProperty(AZURE_BASE_DIRECTORY_NAME).evaluateAttributeExpressions().getValue();
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        long startTime;
        String fileName, fileType, recSiteApiKey, content;
        FlowFile output = null;
        Date fileConsumedDate = null, fileUploadedDate = null;

        try {
            logMetaData.put("processor_name", this.getProcessorName());
            startTime = this.getDateUtil().getEpochTimeInSecond();

            if (inputFF == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            UnoFFInput ffinput = this.getGson().fromJson(ffContent, UnoFFInput.class);

            if (ffinput.data == null || (ffinput.data).isEmpty()) {
                logMetaData.put("flowfile_content", ffContent);
                markProcessorFailure(session, inputFF, logMetaData, null, "Invalid flowfile content. Values are either null or blank.");
                return;
            }

            PaperlessContent paperlessContent = extractData(ffinput.data);
            if (paperlessContent.fileName == null || (paperlessContent.fileName).isEmpty() || paperlessContent.fileType == null || (paperlessContent.fileType).isEmpty() || paperlessContent.content == null || (paperlessContent.content).isEmpty()) {
                logMetaData.put("fileName", paperlessContent.fileName);
                logMetaData.put("fileType", paperlessContent.fileType);
                markProcessorFailure(session, inputFF, logMetaData, null, "Invalid paperless request values. Values are either null or blank.");
                return;
            }

            fileConsumedDate = logDateTimeformat.parse(ffinput.fileConsumedDate);
            if (ffinput.fileUploadedDate != null)
                fileUploadedDate = logDateTimeformat.parse(ffinput.fileUploadedDate);
            fileName = paperlessContent.fileName;
            fileType = paperlessContent.fileType;
            recSiteApiKey = paperlessContent.api_key;
            content = paperlessContent.content;
            Long executionID = ffinput.executionID != null ? Long.parseLong(ffinput.executionID) : null;
            logMetaData.put("execution_id", executionID);
            logMetaData.put("fileName", fileName);
            logMetaData.put("contentType", fileType);
            logMetaData.put("receivedSiteAPIKey", recSiteApiKey);

            List<String> fileNameAttributes = Arrays.asList(fileName.split("_"));
            if (fileNameAttributes.size() != 5)
                throw new Exception("Invalid fileName. file attributes length is not equals to 5.");

            logMetaData.put("uhid", getUhid(fileNameAttributes));
            logMetaData.put(Constants.CLUSTER_NAME, Utility.getClusterName(getLocId(fileNameAttributes)));
            MongoClient mongoClient = this.getMongoClientService().getMongoClient();
            this.setMongoClient(mongoClient);

            loadSiteMasterMap(logMetaData);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "paperless health check summary details received with file_name " + fileName, Level.INFO, null);

            this.setDbUtil(new DBUtil(this.getMongoClient(), this.getAzureConnStr(), this.getAzureFileShare(), this.getAzureBaseDirName(), logMetaData));

            initializeDaos();
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Daos initialized......", Level.INFO, null);

            String uhid = getUhid(fileNameAttributes);
            String siteId = getSiteKeyFromUhid(uhid, recSiteApiKey, logMetaData);
            if (siteId == null || siteId.trim().length() == 0) {
                throw new Exception("siteId is null...");
            }

            logMetaData.put(Constants.ENTITY_NAME, this.getSiteDetails().getEntityName());
            logMetaData.put(Constants.SITE_NAME, this.getSiteDetails().getSiteName());

            DBUser dbUser = this.getIdentityDao().findByUhid(uhid, siteId);
            if (dbUser == null)
                throw new Exception("User doesn't exist for uhid " + uhid);

            DBHealthCheck dbHealthCheck = getHealthCheckObject(dbUser, fileNameAttributes);
            boolean isNewHealthCheckSummary = false, isLatestFile = true;
            if (dbHealthCheck == null) {
                dbHealthCheck = new DBHealthCheck();
                setHealthCheckSummaryMetaData(dbHealthCheck, fileNameAttributes, siteId, logMetaData);
                isNewHealthCheckSummary = true;
            } else if (dbHealthCheck.getHealthCheckDate().after(getHealthCheckDate(fileNameAttributes))) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "HealthCheck is already updated with latest data. Ignoring file attachment " + fileName, Level.INFO, null);
                isLatestFile = false;
            }

            if (isLatestFile) {
                dbHealthCheck.setHealthCheckDate(getHealthCheckDate(fileNameAttributes));
                createHealthCheckSummaryAttachment(fileType, fileNameAttributes, content, dbHealthCheck, logMetaData);
                this.getHealthCheckDao().save(dbHealthCheck);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "saved dbHealthcheck object after adding attachment", Level.INFO, null);

                if (isNewHealthCheckSummary) {
                    dbUser.getHealthChecks().add(dbHealthCheck);
                    dbUser.setUpdatedAt(new Date());
                    this.getUserDao().save(dbUser);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "saved dbUser object after processing attachment", Level.INFO, null);
                }

                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "started creating sms object", Level.INFO, null);
                String strSMSObjFFContent = getSMSObjFFContent(dbUser, uhid, logMetaData);
                output = getFlowFile(session, inputFF, strSMSObjFFContent);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "generated output flowfile with sms object and other details", Level.INFO, null);
                session.transfer(output, REL_SUCCESS);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "SMS object details sent to SMS processor", Level.INFO, null);
            }
        } catch (Exception ex) {
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }

        long endTime = this.getDateUtil().getEpochTimeInSecond();
        logFileProcessingTime(endTime - startTime, fileName, logMetaData);
        logTurnAroundTime(fileConsumedDate, fileUploadedDate, fileName, logMetaData);
        session.remove(inputFF);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private void logFileProcessingTime(Long duration, String fileName, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "paperless pdf: " + fileName + " processed successfully.", Level.INFO, otherDetails);
    }

    private Date getHealthCheckDate(List<String> fileNameAttributes) throws ParseException {
        SimpleDateFormat healthCheckDFormatDate = new SimpleDateFormat("yyyyMMddHHmmss");
        String strHealthCheckDate = (fileNameAttributes.get(4).split("\\."))[0];
        Date healthCheckDate = healthCheckDFormatDate.parse(strHealthCheckDate);
        return healthCheckDate;
    }

    private String getSMSObjFFContent(DBUser dbUser, String uhid, Map<String, Object> logMetaData) {
        DBsms dbSms = new DBsms();
        dbSms.setSmsPurpose("HEALTHCHECK-INTIMATION");
        dbSms.setMobileNumber(dbUser.getMobileNumber());
        dbSms.setUhid(uhid);
        dbSms.setSiteKey(siteDetails.getSiteKey());
        dbSms.setSendAt(new Date());

        if (dbUser.getFirstName() != null && !dbUser.getFirstName().equals("TO_BE_UPDATED_FROM_HOSPITAL"))
            dbSms.setPatientName(dbUser.getFirstName());

        FFInput ffInput = new FFInput();
        ffInput.siteMasterMap = this.getSiteMasterMap();
        ffInput.siteDetails = this.getSiteDetails();
        ffInput.sms = dbSms;
        String strJson = GsonUtil.getGson().toJson(ffInput);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "created string sms object", Level.INFO, null);
        return strJson;
    }

    private void logTurnAroundTime(Date oneDriveFileConsumedDate, Date oneDriveFileUploadDate, String fileName, Map<String, Object> logMetaData) {
        Date currentDate = new Date();
        long turnAroundTime;
        if (oneDriveFileUploadDate != null) {
            turnAroundTime = (currentDate.getTime() - oneDriveFileUploadDate.getTime()) / 1000;
            logMetaData.put("turn_around_time", turnAroundTime);
            logMetaData.put("uploaded_date", oneDriveFileUploadDate);
        }
        long processingTime = (currentDate.getTime() - oneDriveFileConsumedDate.getTime()) / 1000;
        logMetaData.put("fetched_date", oneDriveFileConsumedDate);
        logMetaData.put("processed_date", currentDate);
        logMetaData.put("dax_processing_time", processingTime);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Turn around time calculation is done", Level.INFO, null);
    }

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    private void initializeDaos() {
        this.setIdentityDao(new IdentityDao(this.getMongoClient()));
        this.setHealthCheckDao(new HealthCheckDao(this.getMongoClient()));
        this.setUserDao(new UserDao(this.getMongoClient()));
    }

    private String getFileName(List<String> fileNameAttributes) {
        return String.join("_", fileNameAttributes);
    }

    private String getUhid(List<String> fileNameAttributes) {
        return fileNameAttributes.get(0);
    }

    private void createHealthCheckSummaryAttachment(String fileType, List<String> fileNameAttributes, String content, DBHealthCheck dbHealthCheck, Map<String, Object> logMetaData) throws IOException {
        // UHID_PackageName_BillNo_Locationid_YYYYDDMMHHMMSS
        if (dbHealthCheck.getHealthCheckFilesList() != null && dbHealthCheck.getHealthCheckFilesList().size() > 0) {
            removeExistingAttachment(fileNameAttributes, dbHealthCheck.getHealthCheckFilesList().get(0), logMetaData);
            dbHealthCheck.getHealthCheckFilesList().remove(0);
        }

        Attachment attach = new Attachment();
        byte[] decodedContent = Base64.decodeBase64(content);
        attach.setFileName(getFileName(fileNameAttributes));
        attach.setMimeType(fileType);
        attach.setContent(decodedContent);
        logMetaData.put("fileSize", decodedContent.length / 1024); // Size in kb

        DBAttachement dbAttach = this.getDbUtil().writeDBAttachment(getUhid(fileNameAttributes), attach);
        dbHealthCheck.getHealthCheckFilesList().add(dbAttach);
    }

    private String decompress(String data) throws IOException {
        byte[] base64 = Base64.decodeBase64(data);
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(base64));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0)
            out.write(buf, 0, len);

        gzipInputStream.close();
        out.close();

        return StringUtils.newStringUtf8(out.toByteArray());
    }

    private PaperlessContent extractData(String data) throws IOException {
        String actualData = decompress(data);
        PaperlessContent paperlessContent = this.getGson().fromJson(actualData, PaperlessContent.class);
        return paperlessContent;
    }

    private void loadSiteMasterMap(Map<String, Object> logMetaData) {
        Map<String, SiteDetails> siteMasterMap = new HashMap<>();

        DB appDb = this.getMongoClient().getDB("HHAppData");
        DBCollection coll = appDb.getCollection("SiteMasterDB");
        DBCursor cur = coll.find();
        while (cur.hasNext()) {
            DBObject obj = cur.next();
            String siteKey = obj.get("dbSiteKey").toString();
            SiteDetails siteDetails = new SiteDetails(siteKey);
            siteDetails.setSiteName(obj.get("dbSiteName").toString());
            siteDetails.setSiteKey(siteKey);
            siteDetails.setSiteType(obj.get("dbSiteType").toString());
            siteDetails.setEntityName(obj.get("entityName").toString());
            Object tbl = obj.get("testBlackList");
            if (tbl != null)
                siteDetails.setTestBlackList(tbl.toString());
            Object hbl = obj.get("hcuBlackList");
            if (hbl != null)
                siteDetails.setHcuBlackList(hbl.toString());
            Object uhp = obj.get("uhidPrefix");
            if (uhp != null)
                siteDetails.setUhidPrefix(uhp.toString());

            Object locId = obj.get("locationId");
            if (locId != null)
                siteDetails.setLocationId(locId.toString());

            Object deb = obj.get("debug");
            if (deb != null)
                siteDetails.setDebug(new Boolean(deb.toString()));

            siteDetails.setSms(true);
            Object sms = obj.get("sms");
            if (sms != null)
                siteDetails.setSms(new Boolean(sms.toString()));

            //Populate the hidden tests array from the configuration.
            loadHiddenTests(siteDetails);
            siteMasterMap.put(siteDetails.getSiteKey(), siteDetails);
        }
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "SiteMasterMap is prepared..", Level.INFO, null);
        this.setSiteMasterMap(siteMasterMap);
    }

    private boolean loadHiddenTests(SiteDetails siteDetails) {
        try {
            if (siteDetails.getTestBlackList() != null && siteDetails.getTestBlackList().length() != 0) {
                String[] testIds = siteDetails.getTestBlackList().split(",");
                int[] blackListTests = new int[testIds.length];
                siteDetails.setBlackListTests(testIds); // here to testIds
            }
            if (siteDetails.getBlackListHCUs() != null && siteDetails.getBlackListHCUs().length != 0) {
                String[] testIds = siteDetails.getHcuBlackList().split(",");
                int[] blackListHCUs = new int[testIds.length];
                for (int pos = 0; pos < testIds.length; pos++)
                    blackListHCUs[pos] = Integer.parseInt(testIds[pos].trim());
                Arrays.sort(blackListHCUs);
                siteDetails.setBlackListHCUs(blackListHCUs);
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public String getSiteKeyFromUhid(String uhid, String receivedSiteAPIKey, Map<String, Object> logMetaData) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Improper UHID: " + uhid + ", unable to split it.", Level.ERROR, null);
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not find the site for UHID: " + uhid, Level.INFO, null);
                return null;
            }
            return uhidSite.getSiteKey();
        }

        return receivedSiteAPIKey;
    }

    public SiteDetails getSiteBasedOnPrefix(String sitePrefix) {
        for (String siteKey : this.getSiteMasterMap().keySet()) {
            SiteDetails stDetails = this.getSiteMasterMap().get(siteKey);
            if (stDetails.getUhidPrefix() != null && stDetails.getUhidPrefix().equals(sitePrefix)) {
                this.setSiteDetails(stDetails);
                return stDetails;
            }
        }
        return null;
    }

    public DBHealthCheck getHealthCheckObject(DBUser dbUser, List<String> fileNameAttributes) {
        DBHealthCheck dbHealthCheck = null;
        String recbillNo = getBillNo(fileNameAttributes);
        String rechealthCheckName = getHealthCheckName(fileNameAttributes);
        String reclocId = getLocId(fileNameAttributes);

        for (DBHealthCheck healthCheck : dbUser.getHealthChecks()) {
            if (healthCheck.getBillNo() != null && healthCheck.getBillNo().equals(recbillNo)
                    && healthCheck.getHealthCheckName() != null && healthCheck.getHealthCheckName().equals(rechealthCheckName)
                    && healthCheck.getLocId() != null && healthCheck.getLocId().equals(reclocId)) {
                dbHealthCheck = healthCheck;
                break;
            }
        }
        return dbHealthCheck;
    }

    public void setHealthCheckSummaryMetaData(DBHealthCheck dbHealthCheck, List<String> fileNameAttributes, String sitekey, Map<String, Object> logMetaData) throws ParseException {
        dbHealthCheck.setBillNo(getBillNo(fileNameAttributes));
        dbHealthCheck.setHealthCheckName(getHealthCheckName(fileNameAttributes));
        dbHealthCheck.setLocId(getLocId(fileNameAttributes));
        dbHealthCheck.setSource(sitekey);
        this.getHealthCheckDao().save(dbHealthCheck);
    }

    private String getHealthCheckName(List<String> fileNameAttributes) {
        return fileNameAttributes.get(1).trim();
    }

    private String getLocId(List<String> fileNameAttributes) {
        return fileNameAttributes.get(3).trim();
    }

    private String getBillNo(List<String> fileNameAttributes) {
        return fileNameAttributes.get(2).trim();
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    private void removeExistingAttachment(List<String> fileNameAttributes, DBAttachement dbAttachement, Map<String, Object> logMetaData) {
        if (dbAttachement.getFileAttached() != null) {
            this.getDbUtil().deleteFile(dbAttachement.getFileAttached().toString());
            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "removed attachment id " + dbAttachement.getFileAttached() + " from mongo storage.", Level.INFO, null);
        } else if (dbAttachement.getAzurePath() != null) {
            this.getDbUtil().deleteAzureFile(getUhid(fileNameAttributes), dbAttachement.getFileName());
            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "removed azure file " + dbAttachement.getAzurePath() + " from azure storage.", Level.INFO, null);
        }
    }
}
