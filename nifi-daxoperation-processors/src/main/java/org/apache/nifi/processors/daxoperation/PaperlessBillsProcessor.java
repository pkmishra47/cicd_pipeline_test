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
import org.apache.nifi.processors.daxoperation.dao.*;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Pradeep Mishra
 */
@Tags({"dax", "operations", "dataman", "paperless", "bills"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class PaperlessBillsProcessor extends AbstractProcessor {
    private static final String processorName = "PaperlessBills";
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final SimpleDateFormat logDateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private MongoClient client = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBUtil dbUtil = null;
    private static Gson gson = null;
    private SiteStats siteStats = null;
    private IdentityDao identityDao;
    private BillDao billDao;
    private UserDao userDao;
    private BillDownloadDao billDownloadDao;
    private SMSDao smsDao;
    private String azureConnStr = "";
    private String azureFileShare = "";
    private String azureBaseDirName = "";
    private boolean hasSendSMS = false;
    private boolean hasSendWhatsappSMS = false;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    protected MongoClientService mongoClientService;

    public String getProcessorName() {
        return PaperlessBillsProcessor.processorName;
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
        if (PaperlessBillsProcessor.gson == null)
            PaperlessBillsProcessor.gson = GsonUtil.getGson();
        return PaperlessBillsProcessor.gson;
    }

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
    }

    public SiteStats getSiteStats() {
        return this.siteStats;
    }

    public BillDao getBillDao() {
        return billDao;
    }

    public void setBillDao(BillDao billDao) {
        this.billDao = billDao;
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

    public BillDownloadDao getBillDownloadDao() {
        return this.billDownloadDao;
    }

    public void setBillDownloadDao(BillDownloadDao billDownloadDao) {
        this.billDownloadDao = billDownloadDao;
    }

    public SMSDao getSmsDao() {
        return this.smsDao;
    }

    public void setSmsDao(SMSDao smsDao) {
        this.smsDao = smsDao;
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
        FlowFile output = null;
        long startTime;
        String fileName, fileType, recSiteApiKey, content;
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

            if (ffinput.fileConsumedDate != null)
                fileConsumedDate = logDateTimeformat.parse(ffinput.fileConsumedDate);

            if (ffinput.fileUploadedDate != null)
                fileUploadedDate = logDateTimeformat.parse(ffinput.fileUploadedDate);

            PaperlessContent paperlessContent = extractData(ffinput.data);
            if (paperlessContent.fileName == null || (paperlessContent.fileName).isEmpty() || paperlessContent.fileType == null || (paperlessContent.fileType).isEmpty() || paperlessContent.content == null || (paperlessContent.content).isEmpty()) {
                logMetaData.put("fileName", paperlessContent.fileName);
                logMetaData.put("fileType", paperlessContent.fileType);
                markProcessorFailure(session, inputFF, logMetaData, null, "Invalid paperless request values. Values are either null or blank.");
                return;
            }

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
            if (fileNameAttributes.size() != 7)
                throw new Exception("Invalid fileName. file attributes length is not equals to 7.");

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

            DBUser dbUser = this.getIdentityDao().findByUhid(uhid, siteId);
            if (dbUser == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Adding user with UHID : " + uhid + " for site : " + siteId, Level.INFO, null);
                dbUser = Utility.createUserWithPlaceHolder(uhid, siteId, "TO_BE_UPDATED_FROM_HOSPITAL", getMobileNumber(fileNameAttributes), this.getSiteDetails(), this.getIdentityDao(), this.getUserDao());
            }

            Date billDate = getBillDate(fileNameAttributes);
            String billNo = getBillNo(fileNameAttributes);

            DBBill dbBill = getBillObject(billNo, dbUser);
            boolean isNewBill = false;
            if (dbBill == null) {
                dbBill = createNewBill(billNo, siteId, billDate, fileNameAttributes, logMetaData);
                isNewBill = true;
            }

            String mobileNumber = getMobileNumber(fileNameAttributes);
            if (!mobileNumber.isEmpty())
                dbBill.setMobileNumber(mobileNumber);

            createBillAttachment(fileType, fileNameAttributes, content, dbBill, logMetaData);
            dbBill.setUpdatedDateTime(new Date());
            this.getBillDao().save(dbBill);

            String downloadID = null;
            if (isNewBill) {
                dbUser.getBills().add(dbBill);
                dbUser.setUpdatedAt(new Date());
                this.getUserDao().save(dbUser);
                downloadID = createNewBillDownload(dbUser, dbBill, billNo);
            }

            if (hasSendSMS && downloadID != null && this.getSiteDetails().isSms() && this.getSiteDetails().getEntityName().toUpperCase().equals("APOLLO")) {
                String strSmsObj = getBillIntimationSMSObject(dbUser, uhid, siteId, downloadID);
                output = getFlowFile(session, inputFF, strSmsObj);
                session.transfer(output, REL_SUCCESS);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "SMS object details sent to SMS processor. downloadId: " + downloadID + " ,billno:" + billNo, Level.INFO, null);
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

    private String getMobileNumber(List<String> fileNameAttributes) {
        String recMobileNumber = fileNameAttributes.get(fileNameAttributes.size() - 1).split("\\.")[0];

        if (recMobileNumber.contains("-"))
            return (recMobileNumber.split("-")[1]).trim();
        else
            return recMobileNumber.trim();
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

        if (oneDriveFileConsumedDate != null) {
            long processingTime = (currentDate.getTime() - oneDriveFileConsumedDate.getTime()) / 1000;
            logMetaData.put("fetched_date", oneDriveFileConsumedDate);
            logMetaData.put("dax_processing_time", processingTime);
        }
        logMetaData.put("processed_date", currentDate);
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
        this.setBillDao(new BillDao(this.getMongoClient()));
        this.setUserDao(new UserDao(this.getMongoClient()));
        this.setBillDownloadDao(new BillDownloadDao(this.getMongoClient()));
    }

    private String getActualFileName(List<String> fileNameAttributes) {
        return String.join("_", fileNameAttributes);
    }

    private String getUhid(List<String> fileNameAttributes) {
        return fileNameAttributes.get(4);
    }

    private String getLocId(List<String> fileNameAttributes) {
        return fileNameAttributes.get(0);
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

    private Date getBillDate(List<String> fileNameAttributes) throws ParseException {
        SimpleDateFormat healthCheckDFormatDate = new SimpleDateFormat("dd-MM-yyyy");

        Date dbillDate = new Date();
        String billDate = fileNameAttributes.get(1) + "-" + fileNameAttributes.get(2) + "-" + fileNameAttributes.get(3); // dd-MM-yyy from fileName
        dbillDate = healthCheckDFormatDate.parse(billDate);

        return dbillDate;
    }

    private String getBillNo(List<String> fileNameAttributes) {
//        String billno = fileNameAttributes.get(5);
//        String bill_no = billno;
//        if (bill_no != null)
//            bill_no = billno.replaceAll(".pdf", "");
//
//        return bill_no;
        return fileNameAttributes.get(5);
    }

    private DBBill getBillObject(String billNo, DBUser dbUser) {
        DBBill dbBill = null;
        for (DBBill bill : dbUser.getBills()) {
            if (bill.getBillNo().equals(billNo)) {
                dbBill = bill;
                break;
            }
        }
        return dbBill;
    }

    private DBBill createNewBill(String billNo, String siteID, Date billDate, List<String> fileNameAttributes, Map<String, Object> logMetaData) {
        DBBill dbBill = new DBBill();
        dbBill.setBillDate(billDate);
        dbBill.setCreatedDatetime(new Date());
        dbBill.setBillNo(billNo);
        dbBill.setSource(siteID);
        if (fileNameAttributes.get(0) != null && !fileNameAttributes.get(0).isEmpty()) {
            SiteDetails uhidSiteDetails = getSiteDetailsFromLocId(fileNameAttributes.get(0));
            if (uhidSiteDetails == null)
                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not find the site for Location Id: {}" + fileNameAttributes.get(0), Level.ERROR, null);
            else
                dbBill.setHospitalName(uhidSiteDetails.getSiteName());
        }
        return dbBill;
    }

    private SiteDetails getSiteDetailsFromLocId(String locId) {
        SiteDetails uhidSite = null;
        for (String siteKey : this.getSiteMasterMap().keySet()) {
            SiteDetails siteDetails = this.getSiteMasterMap().get(siteKey);
            if (siteDetails.getLocationId() != null && siteDetails.getLocationId().equals(locId)) {
                uhidSite = siteDetails;
                break;
            }
        }
        return uhidSite;
    }

    private String createNewBillDownload(DBUser dbUser, DBBill dbBill, String bill_no) {
        String generatedString = UUID.randomUUID().toString();
        DBBillDownload billDownload = new DBBillDownload();
        billDownload.setCreatedDate(new Date());
        billDownload.setDownloadId(generatedString);
        billDownload.setMobileNumber(dbUser.getMobileNumber());
        billDownload.setUserObjectId(dbUser.getId());
        billDownload.setBill_no(bill_no);
        this.getBillDownloadDao().save(billDownload);

        return generatedString;
    }

    private String getBillIntimationSMSObject(DBUser dbUser, String uhid, String siteID, String downloadID) {
        DBsms dbSms = new DBsms();
        dbSms.setSmsPurpose("BILLS-INTIMATION-DOWNLOAD");
        dbSms.setMobileNumber(dbUser.getMobileNumber());
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

    private void createBillAttachment(String fileType, List<String> fileNameAttributes, String content, DBBill dbBill, Map<String, Object> logMetaData) throws ParseException, IOException {
        //If file already exists then remove the file and again re-created one with new content
        if (dbBill.getBillFilesList() != null && dbBill.getBillFilesList().size() > 0) {
            removeExistingAttachment(fileNameAttributes, dbBill.getBillFilesList().get(0));
            dbBill.getBillFilesList().remove(0);
        }

        Attachment attach = new Attachment();
        byte[] decodedContent = Base64.decodeBase64(content);

        attach.setFileName(getActualFileName(fileNameAttributes));
        attach.setMimeType(fileType);
        attach.setContent(decodedContent);
        logMetaData.put("fileSize", decodedContent.length / 1024); // Size in kb

        DBAttachement dbAttach = this.getDbUtil().writeDBAttachment(getUhid(fileNameAttributes), attach);
        dbBill.getBillFilesList().add(dbAttach);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    private DBUser createNewDBUserWithPlaceHolder(String uhid, String siteId, List<String> fileNameAttributes) {
        DBUser user = new DBUser();
        user.setMobileNumber(getMobileNumber(fileNameAttributes));

        DBUserBasicInfo basicInfo = new DBUserBasicInfo();
        user.setUserBasicInfo(basicInfo);

        DBUserContactInfo contactInfo = new DBUserContactInfo();
        user.setUserContactInfo(contactInfo);

        DBUserPreferenceInfo preferenceInfo = new DBUserPreferenceInfo();
        preferenceInfo.setSmsAlert("YES");
        preferenceInfo.setEmailAlert("YES");
        user.setUserPreferenceInfo(preferenceInfo);

        Date currentDate = new Date();
        user.setFirstName("TO_BE_UPDATED_FROM_HOSPITAL");
        user.setDateImported(currentDate);
        user.setUpdatedAt(currentDate);
        user.setStatus(DBUser.UserStatus.NOT_ACTIVATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("user");
        user.setRoles(userRoles);
        user.getEntitys().add(this.getSiteDetails().getEntityName());
        this.getUserDao().save(user);

        DBIdentity identity = new DBIdentity();
        identity.setUhid(uhid);
        identity.setSiteKey(siteId);
        identity.setMobileNumber(getMobileNumber(fileNameAttributes));
        identity.setUserId(uhid.toLowerCase() + " " + siteId);
        identity.setDbUser(user);
        this.getIdentityDao().save(identity);
        user.setUpdateIdentityObject(true);
        this.getUserDao().save(user);
        return user;
    }

    private void removeExistingAttachment(List<String> fileNameAttributes, DBAttachement dbAttachement) {
        if (dbAttachement.getFileAttached() != null)
            this.getDbUtil().deleteFile(dbAttachement.getFileAttached().toString());
        else if (dbAttachement.getAzurePath() != null)
            this.getDbUtil().deleteAzureFile(getUhid(fileNameAttributes), dbAttachement.getFileName());
    }
}
