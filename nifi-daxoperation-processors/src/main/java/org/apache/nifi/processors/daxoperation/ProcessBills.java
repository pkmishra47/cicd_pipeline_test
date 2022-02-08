package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.dm.Bills;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;
import org.apache.nifi.processor.util.StandardValidators;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProcessBills extends AbstractProcessor {
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    private static final String processorName = "ProcessBills";
    private static final Charset charset = Charset.forName("UTF-8");
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

    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private BillDao billDao = null;
    private BillDownloadDao billDownloadDao = null;
    private List<Bills> bill_group = null;
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat healthCheckDFormatDate = new SimpleDateFormat("dd-MM-yyyy");
    protected MongoClientService mongoClientService;
    private DBUtil dbUtil = null;
    private String azureConnStr = "";
    private String azureFileShare = "";
    private String azureBaseDirName = "";

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

    public DBUtil getDbUtil() {
        return this.dbUtil;
    }

    public void setDbUtil(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
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
        this.mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
        azureConnStr = context.getProperty(AZURE_CONNECTION_STR).evaluateAttributeExpressions().getValue();
        azureFileShare = context.getProperty(AZURE_FILE_SHARE).evaluateAttributeExpressions().getValue();
        azureBaseDirName = context.getProperty(AZURE_BASE_DIRECTORY_NAME).evaluateAttributeExpressions().getValue();
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        String ffContent;
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        MongoClient client;
        List<Bills> failedBillsList = new ArrayList<>();

        try {
            logMetaData.put("processor_name", processorName);

            if (inputFF == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            ffContent = readFlowFileContent(inputFF, session);
            ffinput = gson.fromJson(ffContent, FFInput.class);

            client = this.mongoClientService.getMongoClient();
            this.siteMasterMap = SiteMaster.loadSiteMasterMap(client, logMetaData, this.getLogUtil());
            SiteDetails siteDetails = getSiteDetailsForSiteApiKey(ffinput.siteApiKey);
            if (siteDetails == null)
                throw new Exception("invalid siteDetails for " + ffinput.siteApiKey);

            this.bill_group = ffinput.bill_group;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(client);

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("%s Bill Files: %s", siteDetails.getSiteName(), this.bill_group.size()), Level.INFO, null);
            totalProcessed = this.bill_group.size();

            while (!this.bill_group.isEmpty()) {
                Bills bills = this.bill_group.get(0);
                try {
                    String fileName = bills.getFileName();

                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Processing bills File:: %s", fileName), Level.INFO, null);
                    String[] parts = fileName.split("_");
                    if (parts.length == 0) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to parse file Name: %s", fileName), Level.INFO, null);
                        this.bill_group.remove(bills);
                        continue;
                    }

                    if (parts.length < 6) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Wrong file name %s", fileName), Level.INFO, null);
                        this.bill_group.remove(bills);
                        continue;
                    }

                    String uhid = parts[4];
                    String siteId = getSiteKeyFromUhid(uhid);
                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                    logMetaData.put("uhid", uhid);
                    if (dbUser == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Unable to find uhid for : " + uhid + " in site: " + siteId, Level.INFO, null);
                        failedBillsList.add(bills);
                        this.bill_group.remove(bills);
                        continue;
                    }

                    boolean found = false;
                    Date dbillDate = new Date();
                    String billDate = parts[1] + "-" + parts[2] + "-" + parts[3]; // dd-MM-yyy from fileName
                    try {
                        dbillDate = healthCheckDFormatDate.parse(billDate);
                    } catch (Exception e) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to get the Bill date parsed: %s ", fileName), Level.INFO, null);
                        this.bill_group.remove(bills);
                        continue;
                    }

                    String billno = parts[5];
                    String bill_no = billno;
                    if (bill_no != null)
                        bill_no = billno.replaceAll(".pdf", "");
                    for (DBBill bill : dbUser.getBills()) {
                        if (bill.getBillNo().equals(bill_no)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        DBBill dbBill = new DBBill();
                        dbBill.setBillDate(dbillDate);
                        dbBill.setBillNo(bill_no);
                        dbBill.setSource(siteDetails.getSiteKey());
                        if (parts[0] != null && !parts[0].isEmpty())
                            dbBill.setHospitalName(getSiteNameFromLocId(parts[0]));

                        byte[] decodedContent = Base64.decodeBase64(bills.getContent());
                        logMetaData.put("fileSize", decodedContent.length / 1024); // Size in kb
                        Attachment attachement = new Attachment();
                        attachement.setFileName(fileName);
                        attachement.setMimeType(bills.getFileType());
                        attachement.setContent(Base64.decodeBase64(bills.getContent()));
                        DBAttachement dbAttach = this.getDbUtil().writeDBAttachment(uhid, attachement);

                        dbBill.getBillFilesList().add(dbAttach);
                        billDao.save(dbBill);
                        dbUser.getBills().add(dbBill);
                        dbUser.setUpdatedAt(new Date());
                        userDao.save(dbUser);

                        String generatedString = UUID.randomUUID().toString();
                        DBBillDownload billDownload = new DBBillDownload();
                        billDownload.setCreatedDate(new Date());
                        billDownload.setDownloadId(generatedString);
                        billDownload.setMobileNumber(dbUser.getMobileNumber());
                        billDownload.setUserObjectId(dbUser.getId());
                        billDownload.setBill_no(bill_no);
                        billDownloadDao.save(billDownload);

                        try {
                            if (siteDetails.isSms()) {
                                DBsms dbSms = new DBsms();
                                dbSms.setSmsPurpose("BILLS-INTIMATION");
                                dbSms.setMobileNumber(dbUser.getMobileNumber());
                                dbSms.setPatientName(dbUser.getFirstName());
                                if (generatedString != null)
                                    dbSms.setDownloadId(generatedString);
                                dbSms.setDbUser(dbUser);
                                dbSms.setUhid(uhid);
                                dbSms.setSiteKey(siteDetails.getSiteKey());
                                dbSms.setSendAt(new Date());
                                SMSDao smsDao = new SMSDao(client);
                                smsDao.save(dbSms);
                            }
                        } catch (Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.INFO, null);
                        }
                    }
                    successCount++;
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                    this.bill_group.remove(bills);
                } catch (Exception ex) {
                    failedBillsList.add(bills);
                    this.bill_group.remove(bills);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }
            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedBillsList.isEmpty()) {
                ffinput.bill_group = failedBillsList;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.bill_group = this.bill_group;
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

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
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

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        if (inputFF == null) {
            return null;
        }
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));
        return new String(buffer, charset);
    }

    protected String getSiteNameFromUhid(String uhid) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                return null;
            }
            return uhidSite.getSiteName();
        }
        return siteDetails.getSiteName();
    }

    String getSiteKeyFromUhid(String uhid) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                return null;
            }
            return uhidSite.getSiteKey();
        }
        return siteDetails.getSiteKey();
    }

    public SiteDetails getSiteBasedOnPrefix(String sitePrefix) {
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getUhidPrefix() != null &&
                    siteDetails.getUhidPrefix().equals(sitePrefix))
                return siteDetails;
        }
        return null;
    }

    protected String getDateWithoutGMT(String dt) {
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

    protected String getSiteNameFromLocId(String locId) {

        SiteDetails uhidSite = getSiteNameOnLocId(locId);
        if (uhidSite == null) {
            if (siteDetails.isDebug())
                System.out.println("Could not find the site for Location Id: " + locId);
            return null;
        }
        return uhidSite.getSiteName();

    }

    public SiteDetails getSiteNameOnLocId(String locId) {
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getLocationId() != null &&
                    siteDetails.getLocationId().equals(locId))
                return siteDetails;
        }

        return null;
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if ((this.siteMasterMap).get(siteApiKey) != null)
            return (this.siteMasterMap).get(siteApiKey);
        return null;
    }

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.billDao = new BillDao(client);
        this.billDownloadDao = new BillDownloadDao(client);
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }
}