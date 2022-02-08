package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.DBAttachement;
import org.apache.nifi.processors.daxoperation.dbo.DBPrescription;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.dm.DischargeSummary;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class ProcessPrescriptions extends AbstractProcessor {
    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder().name("Database Connection Pooling Service")
            .displayName("Database Connection Pooling Service").description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class).build();
    private static final String processorName = "ProcessPrescriptions";
    private static final Charset charset = Charset.forName("UTF-8");

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
    private DBCPService dbcpService;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private DBCollection dbFiles = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private HospitalizationDao hospitalizationDao = null;
    private PrescriptionDao prescriptionDao = null;
    private NotificationDao notificationDao = null;
    private BillDao billDao = null;
    private BillDownloadDao billDownloadDao = null;
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat healthCheckDFormatDate = new SimpleDateFormat("dd-MM-yyyy");
    private List<DischargeSummary> prescriptionList = null;
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
        azureConnStr = context.getProperty(AZURE_CONNECTION_STR).evaluateAttributeExpressions().getValue();
        azureFileShare = context.getProperty(AZURE_FILE_SHARE).evaluateAttributeExpressions().getValue();
        azureBaseDirName = context.getProperty(AZURE_BASE_DIRECTORY_NAME).evaluateAttributeExpressions().getValue();
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();
        List<DischargeSummary> failedPrescriptionList = new ArrayList<>();
        String ffContent = null;

        long startTime = this.getDateUtil().getEpochTimeInSecond();
        int successCount = 0;
        int totalProcessed = 0;
        try {
            this.mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
            ffContent = readFlowFileContent(flowFile, session);
            FFInput ffinput = gson.fromJson(ffContent, FFInput.class);

            if (flowFile == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);


            MongoClient client = this.mongoClientService.getMongoClient();

            siteMasterMap = SiteMaster.loadSiteMasterMap(client, logMetaData, getLogUtil());
            siteDetails = siteMasterMap.get(ffinput.siteApiKey);
            siteStats = siteDetails.getStats();

            prescriptionList = ffinput.prescriptions;
            totalProcessed = prescriptionList.size();
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());

            identityDao = new IdentityDao(client);
            userDao = new UserDao(client);
            notificationDao = new NotificationDao(client);
            prescriptionDao = new PrescriptionDao(client);

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("%s Prescription %s", siteDetails.getSiteName(), prescriptionList.size()), Level.INFO, null);

            this.setDbUtil(new DBUtil(client, this.getAzureConnStr(), this.getAzureFileShare(), this.getAzureBaseDirName(), logMetaData));
            DischargeSummary dbFile = null;
            while (!prescriptionList.isEmpty()) {
                try {
//                        logCursorSize(cur, logMetaData);
                    dbFile = prescriptionList.get(0);
                    String fileName = dbFile.fileName;
                    logMetaData.put("fileName", fileName);

                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                            String.format("Processing File:: %s", fileName), Level.INFO, null);
                    String[] parts = fileName.split("\\.");


                    if (parts.length < 4) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                "Prescription: unable to parse file properly: " + fileName, Level.INFO, null);
//                        log.info("Prescription: unable to parse file properly: {}", fileName);
                        prescriptionList.remove(dbFile);
                        continue;
                    }

                    if (parts[0].contains("OP") || parts[0].contains("EP")) {
                        String uhid = parts[1] + "." + parts[2];
                        logMetaData.put("uhid", uhid);
                        String siteId = getSiteKeyFromUhid(uhid);
                        String siteName = getSiteNameFromUhid(uhid);

                        byte[] decodedContent = Base64.decodeBase64(dbFile.content);
                        logMetaData.put("fileSize", decodedContent.length / 1024); // Size in kb
                        logMetaData.put("fileSizeInBytes", decodedContent.length); // Size in bytes

                        if (decodedContent.length == 0) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL,
                                    "File size is 0 bytes. Discarding file processing.", Level.INFO, null);
//                            dbFiles.remove(dbOrig);
                            prescriptionList.remove(dbFile);
                            continue;
                        }

                        DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                        if (dbUser == null) {
                            prescriptionList.remove(dbFile);
                            failedPrescriptionList.add(dbFile);
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                    String.format("User not found for %s ", uhid), Level.INFO, null);
                            continue; //This user is not yet imported.
                        }

                        boolean found = false;
                        Date dPrescription = new Date();

                        for (DBPrescription pres : dbUser.getPrescriptions()) {
                            if (pres.getDateOfPrescription().equals(dPrescription)) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            DBPrescription dbPres = new DBPrescription();
                            dbPres.setDateOfPrescription(dPrescription);
                            dbPres.setPrescribedBy(parts[3].replace("_", " "));
                            dbPres.setHospitalName(siteName);
                            dbPres.setSource(siteDetails.getSiteKey());

                            Attachment attach = new Attachment();
                            attach.setFileName(fileName);
                            attach.setMimeType(dbFile.fileType);
                            attach.setContent(decodedContent);
                            DBAttachement dbAttach = this.getDbUtil().writeDBAttachment(uhid, attach);
                            dbPres.getPrescriptionFiles().add(dbAttach);

                            prescriptionDao.save(dbPres);
                            dbUser.getPrescriptions().add(dbPres);
                            userDao.save(dbUser);

                            siteStats.setPrescriptionsImported(siteStats.getPrescriptionsImported() + 1);
                            notificationDao.addNotification(dbUser, "Prescription -" + dbPres.getPrescriptionName() + " is getting imported",
                                    NotificationLevel.System, NotificationType.Prescription, siteDetails.getSiteKey(), null, uhid);
                            //Send the SMS, inviting the user to Prism.

                            try {
                                long delayDays = getDaysBetweenTwoDates(dPrescription.getTime(), new Date().getTime());
                                if (delayDays <= 7 && siteDetails.isSms()) {
                                    DBsms dbSms = new DBsms();
                                    dbSms.setSmsPurpose("PRESCRIPTION-INTIMATION");
                                    dbSms.setMobileNumber(dbUser.getMobileNumber());
                                    dbSms.setDbUser(dbUser);
                                    dbSms.setUhid(uhid);
                                    dbSms.setSiteKey(siteDetails.getSiteKey());
                                    dbSms.setSendAt(new Date());
                                    SMSDao smsDao = new SMSDao(client);
                                    smsDao.save(dbSms);

                                }
                            } catch (Exception e) {
                                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                        "PHR | OldProcessorJob | Sending Prescription Intimation SMS | Error " + Utility.stringifyException(e), Level.INFO, null);
                            }
                        }
                        successCount++;
                        prescriptionList.remove(dbFile);
                    }
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedPrescriptionList.add(dbFile);
                    prescriptionList.remove(dbFile);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception e) {
                    failedPrescriptionList.add(dbFile);
                    prescriptionList.remove(dbFile);
                    getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedPrescriptionList.isEmpty()) {
                ffinput.prescriptions = failedPrescriptionList;
                String flowFileContent = gson.toJson(ffinput);
                FlowFile output = getFlowFile(session, flowFile, flowFileContent);
                setFlowFileDate(output, session);
                session.transfer(output, REL_FAILURE);
            } else {
                ffinput.prescriptions = prescriptionList;
                FlowFile output = getFlowFile(session, flowFile, ffContent);
                session.transfer(output, REL_SUCCESS);
            }

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("%s Prescription Added:  %s", siteDetails.getSiteName(), siteStats.getPrescriptionsImported()), Level.INFO, null);

        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.INFO, null);

            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
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

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    private void log_site_processing_time(Long duration, int totalProcessed, int successCount, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("site_name", siteDetails.getSiteName());
        otherDetails.put("site_key", siteDetails.getSiteKey());
        otherDetails.put("total_processed", totalProcessed);
        otherDetails.put("success_count", successCount);
        otherDetails.put("failed_count", totalProcessed - successCount);
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, siteDetails.getSiteName() + " processed successfully.", Level.INFO, otherDetails);
    }

    protected String getSiteNameFromUhid(String uhid) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                if (siteDetails.isDebug()) {
//                    log.error("Could not find the site for UHID: {}", uhid);
                }

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
//                log.error("Imporper UHID: {}, unable to split it.", uhid);
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                if (siteDetails.isDebug()) {

                }
//                    log.error("Could not find the site for UHID: {}", uhid);
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

    long getDaysBetweenTwoDates(long firstDate, long secondDate) {
        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(new Date(firstDate));

        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTime(new Date(secondDate));

        long diffTime = secondCalendar.getTimeInMillis() - firstCalendar.getTimeInMillis();
        long diffDays = diffTime / (24 * 60 * 60 * 1000);

        return diffDays;
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

    private void logCursorSize(DBCursor cur, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("cursor_size", cur.size());
        otherDetails.put("threshold_limit", 50000);
        otherDetails.put("hasMoreToProcess", cur.size() >= 50000);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Got  " + cur.size() + " prescription objects for site name " + siteDetails.getSiteName(), Level.INFO, otherDetails);
    }
}