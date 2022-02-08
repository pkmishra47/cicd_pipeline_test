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
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.LabResultDownloadDao;
import org.apache.nifi.processors.daxoperation.dao.LabTestDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Pradeep Mishra
 */
@Tags({"daxoperation", "dataman", "paperless", "labresult", "labtest"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class PaperlessLabReportProcessor extends AbstractProcessor {
    private static final String processorName = "PaperlessLabResult";
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final SimpleDateFormat logDateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private MongoClient client = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBUtil dbUtil = null;
    private static Gson gson = null;
    private SiteStats siteStats = null;
    private IdentityDao identityDao;
    private LabTestDao labTestDao;
    private UserDao userDao;
    private LabResultDownloadDao labResultDownloadDao;
    private String azureConnStr = "";
    private String azureFileShare = "";
    private String azureBaseDirName = "";
    private boolean hasSendSMS = false;
    private boolean hasSendWhatsappSMS = false;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    protected MongoClientService mongoClientService;

    public String getProcessorName() {
        return PaperlessLabReportProcessor.processorName;
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
        if (PaperlessLabReportProcessor.gson == null)
            PaperlessLabReportProcessor.gson = GsonUtil.getGson();
        return PaperlessLabReportProcessor.gson;
    }

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
    }

    public SiteStats getSiteStats() {
        return this.siteStats;
    }

    public LabTestDao getLabTestDao() {
        return labTestDao;
    }

    public void setLabTestDao(LabTestDao labTestDao) {
        this.labTestDao = labTestDao;
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

    public LabResultDownloadDao getLabResultDownloadDao() {
        return this.labResultDownloadDao;
    }

    public void setLabResultDownloadDao(LabResultDownloadDao labResultDownloadDao) {
        this.labResultDownloadDao = labResultDownloadDao;
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

    public static final PropertyDescriptor SEND_SMS = new PropertyDescriptor
            .Builder()
            .name("SEND_SMS")
            .displayName("SEND_SMS")
            .description("Do we have to send sms?")
            .required(true)
            .allowableValues("NO", "YES")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .defaultValue("NO")
            .build();

    public static final PropertyDescriptor SEND_WHATSAPP_SMS = new PropertyDescriptor
            .Builder()
            .name("SEND_WHATSAPP_SMS")
            .displayName("SEND_WHATSAPP_SMS")
            .description("Do we have to send whatsapp sms?")
            .required(true)
            .allowableValues("NO", "YES")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .defaultValue("NO")
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, AZURE_CONNECTION_STR, AZURE_FILE_SHARE, AZURE_BASE_DIRECTORY_NAME, SEND_SMS, SEND_WHATSAPP_SMS);
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

        hasSendSMS = (context.getProperty(SEND_SMS).getValue().equals("YES"));
        hasSendWhatsappSMS = (context.getProperty(SEND_WHATSAPP_SMS).getValue().equals("YES"));
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        long startTime;
        String fileName, fileType, recSiteApiKey, content;
        FlowFile output = null;
        Date fileConsumedDate = null, fileUploadedDate = null;
        boolean isSmartReport = false;

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
            if (!(fileNameAttributes.size() == 9 || fileNameAttributes.size() == 10))
                throw new Exception("Invalid fileName. file attributes length is not equals to 9 OR 10.");

            if (fileNameAttributes.size() == 10 && !fileNameAttributes.get(8).equals("SR"))
                throw new Exception("FileName attributes length is equal to 10 but file doesn't belong to smart report type.");

            if (isSmartReport(fileNameAttributes))
                isSmartReport = true;

            logMetaData.put("smartReport", isSmartReport);
            logMetaData.put("uhid", getUhid(fileNameAttributes));
            logMetaData.put(Constants.CLUSTER_NAME, Utility.getClusterName(getLocId(fileNameAttributes)));
            MongoClient mongoClient = this.getMongoClientService().getMongoClient();
            this.setMongoClient(mongoClient);

            loadSiteMasterMap(logMetaData);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "paperless pdf details received with file_name " + fileName, Level.INFO, null);

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
            int orderId = 0;
            String orderStr = null;
            String orderIDVal = fileNameAttributes.get(1);
            if (orderIDVal != null) {
                try {
                    orderId = new Float(Float.parseFloat(orderIDVal)).intValue();
                } catch (Exception ex) {
                    orderStr = orderIDVal;
                }
            }

            if (orderId == 0 && (orderStr == null || orderStr.isEmpty()))
                throw new Exception("Invalid orderID... ");

            DBUser dbUser = this.getIdentityDao().findByUhid(uhid, siteId);
            if (dbUser == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Adding user with UHID : " + uhid + " for site : " + siteId, Level.INFO, null);
                dbUser = Utility.createUserWithPlaceHolder(uhid, siteId, "TO_BE_UPDATED_FROM_HOSPITAL", getMobileNumber(fileNameAttributes), this.getSiteDetails(), this.getIdentityDao(), this.getUserDao());
            }

            String identifier = getIdentifier(fileNameAttributes);
            logMetaData.put("identifier", identifier);
            DBLabTest dbLabTest = getLabTestObject(orderId, orderStr, identifier, dbUser);
            boolean isNewLabTest = false, isFirstLabTestODNormalFile = false, isFirstLabTestODSmartFile = false;
            if (dbLabTest == null) {
                dbLabTest = new DBLabTest();
                setLabTestMetaData(dbLabTest, uhid, orderId, orderStr, identifier, logMetaData);
                isNewLabTest = true;
            }

            if (!isSmartReport && isFirstFileForNormalLabReport(dbLabTest))
                isFirstLabTestODNormalFile = true;

            if (isSmartReport && isFirstFileForSmartLabReport(dbLabTest))
                isFirstLabTestODSmartFile = true;

            setLabTestAdditionalInfo(dbLabTest, siteId, fileNameAttributes);
            createLabTestAttachment(fileType, fileNameAttributes, content, dbLabTest, logMetaData);
            this.getLabTestDao().save(dbLabTest);

            if (isNewLabTest) {
                dbUser.getLabtests().add(dbLabTest);
                dbUser.setUpdatedAt(new Date());
                this.getUserDao().save(dbUser);
            }

            String downloadID = null;
            if (isFirstLabTestODNormalFile || isFirstLabTestODSmartFile)
                downloadID = createNewLabResultDownload(dbUser, dbLabTest, isSmartReport);

            if (hasSendSMS && downloadID != null & this.getSiteDetails().isSms() && this.getSiteDetails().getEntityName().toUpperCase().equals("APOLLO")) {
                String strLabIntimationDownloadSmsObjFFContent = getLabIntimationDownloadSmsObjFFContent(dbUser, uhid, siteId, downloadID);
                output = getFlowFile(session, inputFF, strLabIntimationDownloadSmsObjFFContent);
                session.transfer(output, REL_SUCCESS);
//                String strLabIntimationVisitSmsObjFFContent = getLabIntimationVisitSmsObjFFContent(dbUser, uhid, siteId);
//                output = getFlowFile(session, inputFF, strLabIntimationVisitSmsObjFFContent);
//                session.transfer(output, REL_SUCCESS);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "SMS object details sent to SMS processor. downloadId: " + downloadID + " ,orderId:" + orderId, Level.INFO, null);
            }
        } catch (Exception ex) {
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }

        long endTime = this.getDateUtil().getEpochTimeInSecond();
        logFileProcessingTime(endTime - startTime, fileName, logMetaData);
        logTurnAroundTime(fileConsumedDate, fileUploadedDate, fileName, logMetaData);
        session.remove(inputFF);
    }

    private void logFileProcessingTime(Long duration, String fileName, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "paperless pdf: " + fileName + " processed successfully.", Level.INFO, otherDetails);
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
        this.setLabTestDao(new LabTestDao(this.getMongoClient()));
        this.setUserDao(new UserDao(this.getMongoClient()));
        this.setLabResultDownloadDao(new LabResultDownloadDao(this.getMongoClient()));
    }

    private String getFileName(List<String> fileNameAttributes) {
        return String.join("_", fileNameAttributes);
    }

    private String getRequestRaisedDate(List<String> fileNameAttributes) {
        return fileNameAttributes.get(3);
    }

    private String getSysDateTime(List<String> fileNameAttributes) {
        if (isSmartReport(fileNameAttributes))
            return (fileNameAttributes.get(9).split("\\."))[0];
        else
            return (fileNameAttributes.get(8).split("\\."))[0];
    }

    private boolean isSmartReport(List<String> fileNameAttributes) {
        return fileNameAttributes.size() == 10 && fileNameAttributes.get(8).equals("SR");
    }

    private String getUhid(List<String> fileNameAttributes) {
        return fileNameAttributes.get(0);
    }

    private String getLocId(List<String> fileNameAttributes) {
        return fileNameAttributes.get(2);
    }

    private void createLabTestAttachment(String fileType, List<String> fileNameAttributes, String content, DBLabTest dbLabTest, Map<String, Object> logMetaData) throws ParseException, IOException {
        SimpleDateFormat reqDateformat = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat sysDateTimeformat = new SimpleDateFormat("yyyyMMddHHmmss");

        Attachment attach = new Attachment();
        byte[] decodedContent = Base64.decodeBase64(content);
        attach.setFileName(getFileName(fileNameAttributes));
        attach.setMimeType(fileType);
        attach.setContent(decodedContent);
        logMetaData.put("fileSize", decodedContent.length / 1024); // Size in kb

        DBAttachement dbAttach = this.getDbUtil().writeDBAttachment(getUhid(fileNameAttributes), attach);

        if (!getRequestRaisedDate(fileNameAttributes).isEmpty())
            dbAttach.setReqRaisedDate(reqDateformat.parse(getRequestRaisedDate(fileNameAttributes)));
        dbAttach.setSysDateTime(sysDateTimeformat.parse(getSysDateTime(fileNameAttributes)));

        if (isSmartReport(fileNameAttributes))
            dbLabTest.getLabTestSmartReports().add(dbAttach);
        else
            dbLabTest.getLabTestFiles().add(dbAttach);
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

    public DBLabTest getLabTestObject(int orderId, String orderStr, String identifier, DBUser dbUser) {
        DBLabTest labTest = null;

        for (DBLabTest lTest : dbUser.getLabtests()) {
            int lTestOrderId = 0;
            String lTestOrderStr = null;

            if (lTest.getOrderId() == null)
                continue;

            try {
                lTestOrderId = new Float(Float.parseFloat(lTest.getOrderId().trim())).intValue();
            } catch (NumberFormatException ex) {
                lTestOrderStr = lTest.getOrderId().trim();
            }

            if (orderId != 0 && lTestOrderId == orderId) {
                labTest = lTest;
                break;
            }

            if (lTestOrderStr != null && lTestOrderStr.equals(orderStr)) {
                labTest = lTest;
                break;
            }
        }
        return labTest;
    }

    public void setLabTestMetaData(DBLabTest dbLabTest, String uhid, int orderId, String orderStr, String identifier, Map<String, Object> logMetaData) {
        dbLabTest.setUhid(uhid);
        dbLabTest.setCreatedDateTime(new Date());

        if (orderId != 0)
            dbLabTest.setOrderId(Integer.toString(orderId));
        else
            dbLabTest.setOrderId(orderStr);

        dbLabTest.setIdentifier(identifier);
        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "For UHID: " + uhid + " creating metadata for new labtest with OrderId: " + dbLabTest.getOrderId(), Level.INFO, null);
    }

    public String createNewLabResultDownload(DBUser dbUser, DBLabTest dbLabTest, boolean isSmartReport) {
        SecureRandom random = new SecureRandom();
        String generatedString = new BigInteger(130, random).toString(32);
        DBLabResultDownload labDownload = new DBLabResultDownload();
        labDownload.setCreatedDate(new Date());
        labDownload.setDownloadId(generatedString);
        labDownload.setMobileNumber(dbUser.getMobileNumber());
        labDownload.setUserObjectId(dbUser.getId());
        labDownload.setOrderId(dbLabTest.getOrderId());
        labDownload.setIdentifier(dbLabTest.getIdentifier());
        labDownload.setSmartReport(isSmartReport);
        this.getLabResultDownloadDao().save(labDownload);

        return generatedString;
    }

    private void setLabTestAdditionalInfo(DBLabTest dbLabTest, String sitekey, List<String> fileNameAttributes) throws ParseException {
        SimpleDateFormat sysDateTimeformat = new SimpleDateFormat("yyyyMMddHHmmss");

        dbLabTest.setSiteKey(sitekey);
        dbLabTest.setSource(sitekey);
        dbLabTest.setOwnedBy(0);
        dbLabTest.setDateImported(new Date());

        if (dbLabTest.getBillId() == null || dbLabTest.getBillId().isEmpty())
            dbLabTest.setBillId(fileNameAttributes.get(6));
        dbLabTest.setTestDate(sysDateTimeformat.parse(getSysDateTime(fileNameAttributes)));

        String mobileNumber = getMobileNumber(fileNameAttributes);
        dbLabTest.setMobileNumber(mobileNumber);
    }

    private String getMobileNumber(List<String> fileNameAttributes) {
        String mobileNumber = fileNameAttributes.get(7);
        if (mobileNumber.contains("-") && mobileNumber.split("-").length == 2)
            return (mobileNumber.split("-")[1]).trim();
        else
            return mobileNumber.trim();
    }

    private String getIdentifier(List<String> fileNameAttributes) {
        return fileNameAttributes.get(5);
    }

    private String getLabIntimationDownloadSmsObjFFContent(DBUser dbUser, String uhid, String siteID, String downloadID) {
        DBsms dbSms = new DBsms();
        dbSms.setSmsPurpose("LABTEST-INTIMATION-DOWNLOAD");
        dbSms.setMobileNumber(dbUser.getMobileNumber());

        if (dbUser.getFirstName() != null && !dbUser.getFirstName().equals("TO_BE_UPDATED_FROM_HOSPITAL"))
            dbSms.setPatientName(dbUser.getFirstName());
        if (downloadID != null)
            dbSms.setDownloadId(downloadID);
        dbSms.setUhid(uhid);
        dbSms.setSiteKey(siteID);
        dbSms.setSendAt(new Date());

        if (hasSendWhatsappSMS)
            dbSms.setWhatsapp(true);

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = this.getSiteDetails().getSiteKey();
        ffInput.sms = dbSms;
        return GsonUtil.getGson().toJson(ffInput);
    }

    private String getLabIntimationVisitSmsObjFFContent(DBUser dbUser, String uhid, String siteID) {
        DBsms dbSms = new DBsms();
        dbSms.setSmsPurpose("LABTEST-INTIMATION-VISIT");
        dbSms.setMobileNumber(dbUser.getMobileNumber());

        if (dbUser.getFirstName() != null && !dbUser.getFirstName().equals("TO_BE_UPDATED_FROM_HOSPITAL"))
            dbSms.setPatientName(dbUser.getFirstName());
        dbSms.setUhid(uhid);
        dbSms.setSiteKey(siteID);
        dbSms.setSendAt(new Date());

        FFInput ffInput = new FFInput();
        ffInput.siteMasterMap = this.getSiteMasterMap();
        ffInput.siteDetails = this.getSiteDetails();
        ffInput.sms = dbSms;
        return GsonUtil.getGson().toJson(ffInput);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    private boolean isFirstFileForNormalLabReport(DBLabTest dbLabTest) {
        return dbLabTest.getLabTestFiles() == null || dbLabTest.getLabTestFiles().size() <= 0;
    }

    private boolean isFirstFileForSmartLabReport(DBLabTest dbLabTest) {
        return dbLabTest.getLabTestSmartReports() == null || dbLabTest.getLabTestSmartReports().size() <= 0;
    }
}
