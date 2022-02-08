

package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.*;
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
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
import org.apache.nifi.processors.daxoperation.dm.SpecialResult;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.models.dax.TestData;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.slf4j.event.Level;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.nifi.processors.daxoperation.utils.Utility.readFlowFileContent;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "operations", "dataman", "analyseTestData", "oldsite"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class AnalyseTestDataProcessor extends AbstractProcessor {
    private static final String processorName = "AnalyseTestDataProcessor";
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final SimpleDateFormat logDateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static Gson gson = null;
    protected MongoClientService mongoClientService;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private SiteStats siteStats = null;
    private List<SpecialResult> specialResultList = null;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private Map<String, String> doctorMapping = new HashMap<>();
    private Map<String, DBEntity> entitys = new HashMap<>();
    private Integer intimationSMSDurationDays;
    private IdentityDao identityDao;
    private UserDao userDao;
    private LabResultDownloadDao labResultDownloadDao;
    private LabTestDao labTestDao;
    private NotificationDao notificationDao;
    private CircleDao circleDao;
    private CorporateDao corporateDao;
    private SMSDao smsDao;
    private EntityDao entityDao;
    private Integer mongoDbQueryLimit = 5000;
    private boolean debugOn = false;

    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public String getProcessorName() {
        return AnalyseTestDataProcessor.processorName;
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
        if (AnalyseTestDataProcessor.gson == null)
            AnalyseTestDataProcessor.gson = new Gson();
        return AnalyseTestDataProcessor.gson;
    }

    public Map<String, String> getDoctorMapping() {
        return this.doctorMapping;
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

    public Integer getIntimationSMSDurationDays() {
        return intimationSMSDurationDays;
    }

    public void setIntimationSMSDurationDays(Integer intimationSMSDurationDays) {
        this.intimationSMSDurationDays = intimationSMSDurationDays;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public IdentityDao getIdentityDao() {
        return this.identityDao;
    }

    public void setIdentityDao(IdentityDao identityDAO) {
        this.identityDao = identityDAO;
    }

    public UserDao getUserDao() {
        return this.userDao;
    }

    public void setUserDao(UserDao userDAO) {
        this.userDao = userDAO;
    }

    public LabResultDownloadDao getLabResultDownloadDao() {
        return this.labResultDownloadDao;
    }

    public void setLabResultDAO(LabResultDownloadDao labResultDownloadDAO) {
        this.labResultDownloadDao = labResultDownloadDAO;
    }

    public LabTestDao getLabTestDao() {
        return this.labTestDao;
    }

    public void setLabTestDao(LabTestDao labTestDAO) {
        this.labTestDao = labTestDAO;
    }

    public NotificationDao getNotificationDao() {
        return this.notificationDao;
    }

    public void setNotificationDao(NotificationDao notificationDAO) {
        this.notificationDao = notificationDAO;
    }

    public CircleDao getCircleDao() {
        return this.circleDao;
    }

    public void setCircleDao(CircleDao circleDAO) {
        this.circleDao = circleDAO;
    }

    public CorporateDao getCorporateDao() {
        return this.corporateDao;
    }

    public void setCorporateDAO(CorporateDao corporateDAO) {
        this.corporateDao = corporateDAO;
    }

    public SMSDao getSmsDao() {
        return this.smsDao;
    }

    public void setSmsDao(SMSDao smsDAO) {
        this.smsDao = smsDAO;
    }

    public EntityDao getEntityDao() {
        return this.entityDao;
    }

    public void setEntityDao(EntityDao entityDAO) {
        this.entityDao = entityDAO;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public List<SpecialResult> getSpecialResultList() {
        return this.specialResultList;
    }

    public void setSpecialResultList(List<SpecialResult> specialResultList) {
        this.specialResultList = specialResultList;
    }

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
    }

    public SiteStats getSiteStats() {
        return this.siteStats;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor SMS_INTIMATION_DAYS = new PropertyDescriptor
            .Builder().name("INTIMATION_SMS_DURATION_DAYS")
            .displayName("INTIMATION SMS DURATION DAYS")
            .required(false)
            .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
            .description("Default : 7")
            .build();

    public static final PropertyDescriptor QUERY_LIMIT = new PropertyDescriptor
            .Builder().name("QUERY_LIMIT")
            .displayName("QUERY_LIMIT")
            .required(false)
            .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
            .description("Default : 5000")
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, SMS_INTIMATION_DAYS, QUERY_LIMIT, DEBUG);
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
        Integer intimationDays = context.getProperty(SMS_INTIMATION_DAYS).asInteger();
        if (intimationDays == null || intimationDays < -1) {
            intimationDays = 7;
        }
        setIntimationSMSDurationDays(intimationDays);


        Integer lim = context.getProperty(QUERY_LIMIT).asInteger();
        if (lim != null) {
            mongoDbQueryLimit = lim;
        }

        if (context.getProperty(DEBUG).evaluateAttributeExpressions().getValue() != null)
            debugOn = Boolean.parseBoolean(context.getProperty(DEBUG).evaluateAttributeExpressions().getValue());
    }


    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        long startTime;
        int totalProcessed = 0, successCount = 0, failedCount = 0;
        MongoClient client;
        Date fileConsumedDate;
        String loggingFileName;
        String ffContent;
        FFInput ffinput;
        List<SpecialResult> failedSpecialResultList = new ArrayList<>();

        try {
            logMetaData.put("processor_name", this.getProcessorName());

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            ffContent = readFlowFileContent(inputFF, session);
            ffinput = this.getGson().fromJson(ffContent, FFInput.class);

            if (!isValidFlowFileContent(ffinput))
                throw new Exception("flowfile doesn't have valid siteDetails or siteMasterMap or AnalyseTestDataList");

            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            client = this.getMongoClientService().getMongoClient();
            this.setSiteMasterMap(SiteMaster.loadSiteMasterMap(client, logMetaData, this.getLogUtil()));
            SiteDetails siteDetails = getSiteDetailsForSiteApiKey(ffinput.siteApiKey);
            this.setSiteDetails(siteDetails);
            if (siteDetails == null)
                throw new Exception("invalid siteDetails for " + ffinput.siteApiKey);

            this.setSiteStats(this.getSiteDetails().getStats());

            if (debugOn) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "SITE_DETAILS received: " + this.getSiteDetails().toString(), Level.INFO, null);
            }

            loadFlowFileContents(ffinput);
            logMetaData.put(Constants.SITE_NAME, getSiteDetails().getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, getSiteDetails().getEntityName());
            initializeDaos(client);

            String generatedString = null;
            Integer daystoDelayHealthChecks = 0;
            startTime = this.getDateUtil().getEpochTimeInSecond();

            logCursorSize(logMetaData);
            while (!this.getSpecialResultList().isEmpty()) {
                totalProcessed++;
                SpecialResult specialResultObj = this.getSpecialResultList().get(0);

                try {
                    if (specialResultObj.getSpecial_result() != null)
                        specialResultObj = specialResultObj.getSpecial_result();

                    logMetaData.put(Constants.CLUSTER_NAME, Utility.getClusterName(this.getSiteDetails().getLocationId()));
                    String uhid = specialResultObj.getUhid();
                    String siteId = getSiteKeyFromUhid(uhid, logMetaData);
                    logMetaData.put(Constants.UHID, uhid);

                    if (siteId == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Lab Test: Unable to find site details for : " + uhid + " in site: " + siteId, Level.INFO, null);
                        failedCount += 1;
                        this.getSpecialResultList().remove(specialResultObj);
                        continue;
                    }

                    DBUser dbUser = getIdentityDao().findByUhid(uhid, siteId);
                    if (dbUser == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Lab Test: Unable to find uhid for : " + uhid + " in site: " + siteId, Level.INFO, null);
                        failedCount += 1;
                        failedSpecialResultList.add(specialResultObj);
                        this.getSpecialResultList().remove(specialResultObj);
                        continue;
                    }

                    int orderId = 0;
                    String orderStr = null;
                    if (specialResultObj.getOrder_id() != null) {
                        try {
                            orderId = new Float(Float.parseFloat(specialResultObj.getOrder_id())).intValue();
                        } catch (Exception ex) {
                            orderStr = specialResultObj.getOrder_id();
                        }
                    }

                    int testId = 0;
                    String testIdStr = null;
                    try {
                        if (specialResultObj.getRecord_group_id() != null)
                            testId = new Float(Float.parseFloat(specialResultObj.getRecord_group_id())).intValue();
                        if (specialResultObj.getService_code() != null) /*Higher preference.*/ {
                            testId = new Float(Float.parseFloat(specialResultObj.getService_code())).intValue();
                            logMetaData.put("service_code", specialResultObj.getService_code());
                        }
                    } catch (NumberFormatException ex) {
                        testIdStr = specialResultObj.getService_code();
                    }

                    Object objHCUId = specialResultObj.getHcu_id();
                    String hcuId = null;
                    if (objHCUId != null) {
                        hcuId = objHCUId.toString();
                        int hId = 0;

                        /*
                         * With holding the test for the next three days if it is part of Health Check.
                         */
                        DBEntity dbEntity = getEntity(siteDetails.getEntityName(), logMetaData);
                        if (dbEntity != null) {
                            daystoDelayHealthChecks = dbEntity.getDaysToDelayHealthCheck();
                        }
                        if (hId != 0 && daystoDelayHealthChecks != null && daystoDelayHealthChecks != 0) {
                            Date testDate = null;
                            String crtTime = null;
                            try {
                                //saving created_at as the date of test creation.
                                crtTime = specialResultObj.getCreated_at();
                                if (specialResultObj.getReceived_on() != null && specialResultObj.getReceived_on().length() != 0)
                                    crtTime = specialResultObj.getReceived_on();
                                testDate = dateformat.parse(getDateWithoutGMT(crtTime));
                            } catch (Exception e) {
                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse crtTime " + crtTime + " for UHID " + uhid, Level.INFO, null);
                            }

                            if (testDate != null && daysBetween(testDate, new Date()) < daystoDelayHealthChecks) {
                                boolean delayFlag = true;
                                Object oDelayFlag = specialResultObj.getDelay();
                                if (oDelayFlag != null && oDelayFlag.toString().equalsIgnoreCase("n"))
                                    delayFlag = false;
                                if (delayFlag) {
                                    //Send An SMS that a delay in healthcheck will be there.
                                    Date sentDate = dbUser.getHealthCheckDelaySMSSent();
                                    if (sentDate == null || daysBetween(sentDate, testDate) > daystoDelayHealthChecks) {
                                        //Send a Healthchek delay SMS.
                                        try {

                                            if (uhid.contains("DHNH")) {
                                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is DHNH and siteKey : " + siteDetails.getSiteKey() + ", : locationID {}" + siteDetails.getLocationId(), Level.INFO, null);
                                            }
                                            if (uhid.contains("DAVS")) {
                                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is DAVS and siteKey : " + siteDetails.getSiteKey() + ", : locationID {}" + siteDetails.getLocationId(), Level.INFO, null);
                                            }
                                            if (uhid.contains("KHS")) {
                                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is KHS and siteKey : " + siteDetails.getSiteKey() + ", : locationID {}" + siteDetails.getLocationId(), Level.INFO, null);
                                            }
//                                                if (siteDetails.isSms() && uhid != null && !uhid.contains("DHNH") && !uhid.contains("DAVS") && !uhid.contains("DAVI") && !uhid.contains("KHS")) {
                                            if (siteDetails.isSms() && uhid != null && !uhid.contains("DHNH") && !uhid.contains("DAVS") && !uhid.contains("DAVI") && !uhid.contains("KHS") && (specialResultObj.getClientName() != null && !specialResultObj.getClientName().contains("24X7"))) {
                                                DBsms dbSms = new DBsms();
                                                dbSms.setSmsPurpose("HEALTHCHECK-DELAY");
                                                dbSms.setMobileNumber(dbUser.getMobileNumber());
                                                dbSms.setDbUser(dbUser);
                                                dbSms.setUhid(uhid);
                                                dbSms.setSiteKey(siteDetails.getSiteKey());
                                                dbSms.setSendAt(new Date());
                                                getSmsDao().save(dbSms);
                                            }

                                            dbUser.setHealthCheckDelaySMSSent(new Date());
                                            getUserDao().save(dbUser);
                                        } catch (Exception e) {
                                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "PHR | OldProcessorJob | Sending HealthCheckDelay SMS | Error " + Utility.stringifyException(e), Level.ERROR, null);
                                        }
                                    }
                                    failedCount += 1;
                                    failedSpecialResultList.add(specialResultObj);
                                    this.getSpecialResultList().remove(specialResultObj);
                                    continue;
                                }
                            }
                        }
                    }

                    //Add this user into the circle of referral doctor.
                    Object dbReferDocId = specialResultObj.getDoctor_id();
                    if (dbReferDocId != null) {
                        String referDocId = dbReferDocId.toString();

                        //Get the user who has this referral Id as the userId.
                        DBUser dbDoc = getIdentityDao().findByUserID(referDocId);
                        if (dbDoc != null) {
                            DBCircle refCircle = null;
                            for (DBCircle dbCircle : dbDoc.getCircles()) {
                                if (dbCircle.getName().equals("_labTestRefs_")) {
                                    refCircle = dbCircle;
                                    break;
                                }
                            }
                            if (refCircle == null) { //This circle does not exist before.
                                refCircle = new DBCircle();
                                refCircle.setName("_labTestRefs_");
                                refCircle.setCategory("internal");
                                refCircle.setOwnerId(dbDoc.getId().toString());
                                refCircle.setUpdatedAt(new Date());

                                refCircle.getUserList().add(dbUser.getId().toString());
                                getCircleDao().save(refCircle);
                                dbDoc.getCircles().add(refCircle);
                                dbDoc.setUpdatedAt(new Date());
                                getUserDao().save(dbDoc);
                            } else {
                                if (!refCircle.getUserList().contains(dbUser.getId().toString())) {
                                    refCircle.getUserList().add(dbUser.getId().toString());
                                    refCircle.setUpdatedAt(new Date());
                                    getCircleDao().save(refCircle);
                                }
                            }
                        }
                    }

                    //Find this lab Test, If it already exists for this user.
                    DBLabTest dbLabTest = null;
                    boolean found = false;
                    for (DBLabTest lTest : dbUser.getLabtests()) {
                        // If order id is null then labtest might be a duplicate.OrderId is should not be null. Need to fix this issue in Agent.
                        int lTestOrderId = 0;
                        String lTestOrderStr = null;
                        try {
                            try {
                                lTestOrderId = new Float(Float.parseFloat(lTest.getOrderId().trim())).intValue();
                            } catch (NumberFormatException ex) {
                                lTestOrderStr = lTest.getOrderId().trim();
                            }
                        } catch (Exception e) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "PHR | OldProcessorJob | Lab TestOrderId is empty for uhid " + uhid + " Site Key " + siteId, Level.INFO, null);
                        }
                        if (orderId != 0 && lTestOrderId == orderId) {
                            dbLabTest = lTest;
                            found = true;
                            break;
                        }

                        if (lTestOrderStr != null && lTestOrderStr.equals(orderStr)) {
                            dbLabTest = lTest;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        dbLabTest = new DBLabTest();
                        dbLabTest.setUhid(uhid);
                        dbLabTest.setCreatedDateTime(new Date());
                        if (orderId != 0)
                            dbLabTest.setOrderId(Integer.toString(orderId));
                        else
                            dbLabTest.setOrderId(orderStr);
                        dbLabTest.setSiteKey(siteDetails.getSiteKey());
                        dbLabTest.setSource(siteDetails.getSiteKey());
                        dbLabTest.setOwnedBy(0);
                        if (hcuId != null && !hcuId.equals("0")) {
                            dbLabTest.setPackageId(hcuId);
                            Object objHCUName = specialResultObj.getHcu_name();
                            if (objHCUName != null)
                                dbLabTest.setPackageName(objHCUName.toString());
                        }

                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "For UHID: " + uhid + " adding new OrderId: " + dbLabTest.getOrderId(), Level.INFO, null);

                        dbLabTest.setDateImported(new Date());
                        Object refDoc = specialResultObj.getDoctor_name();
                        if (refDoc != null) {
                            dbLabTest.setReferredBy(refDoc.toString());
                        }
                        String crtTime = null;
                        try {
                            //saving created_at as the date of test creation.
                            if (specialResultObj.getCreated_at() != null)
                                crtTime = specialResultObj.getCreated_at();
                            if (specialResultObj.getReceived_on() != null && specialResultObj.getReceived_on().length() != 0)
                                crtTime = specialResultObj.getReceived_on();
                            dbLabTest.setTestDate(dateformat.parse(getDateWithoutGMT(crtTime)));
                        } catch (Exception e) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse Test Createddate " + specialResultObj.getCreated_at() + " for OrderId " + dbLabTest.getOrderId(), Level.INFO, null);
                        }

                        dbUser.getLabtests().add(dbLabTest);
                        dbUser.setUpdatedAt(new Date());
                        // generatedString = UUID.randomUUID().toString();
                        SecureRandom random = new SecureRandom();
                        generatedString = new BigInteger(130, random).toString(32);
                        DBLabResultDownload labDownload = new DBLabResultDownload();
                        labDownload.setCreatedDate(new Date());
                        labDownload.setDownloadId(generatedString);
                        labDownload.setMobileNumber(dbUser.getMobileNumber());
                        labDownload.setUserObjectId(dbUser.getId());
                        labDownload.setOrderId(dbLabTest.getOrderId());
                        getLabResultDownloadDao().save(labDownload);
                        getLabTestDao().save(dbLabTest);
                        getUserDao().save(dbUser);

                        // TODO : look into this
                        if (dbLabTest.getTestDate() != null && dbLabTest.getTestDate().after(this.getSiteStats().getLastTestImportDate())) {
                            this.getSiteStats().setLastOrderId(dbLabTest.getOrderId());
                            this.getSiteStats().setLastTestImportDate(dbLabTest.getTestDate());
                        }
                        //Send the SMS, inviting the user to Prism.
                    }

                    if (specialResultObj.getBill_id() != null && !specialResultObj.getBill_id().isEmpty() && (dbLabTest.getBillId() == null || dbLabTest.getBillId().isEmpty())) {
                        dbLabTest.setBillId(specialResultObj.getBill_id());
                        getLabTestDao().save(dbLabTest);
                    } else if (specialResultObj.getBill_no() != null && !specialResultObj.getBill_no().isEmpty() && (dbLabTest.getBillId() == null || dbLabTest.getBillId().isEmpty())) {
                        dbLabTest.setBillId(specialResultObj.getBill_no());
                        getLabTestDao().save(dbLabTest);
                    }

                    if (hcuId != null && !hcuId.equals("0")) {
                        //Set the OwnerShip of this labtest based on corporate packageIds
                        //If so include the user into the corporate circle.
                        DBCorporate dbCorp = null;
                        for (DBCorporate corporate : getCorporateDao().getAll()) {
                            for (DBLabPackage pack : corporate.getExclusivePackages()) {
                                if (pack.getSiteKey().equals(siteId) && hcuId.equals(pack.getPackageId())) {
                                    dbLabTest.setOwnedBy(1);
                                    dbCorp = corporate;
                                    break;
                                }
                            }
                            for (DBLabPackage pack : corporate.getUseralsoPackages()) {
                                if (pack.getSiteKey().equals(siteId) && hcuId.equals(pack.getPackageId())) {
                                    dbLabTest.setOwnedBy(2);
                                    dbCorp = corporate;
                                    break;
                                }
                            }

                            if (dbCorp != null) {
                                //Add this user into the corporate Circle.
                                DBCircle dbCircle = dbCorp.getCircle();
                                if (dbCircle == null) { //This circle does not exist before.
                                    dbCircle = new DBCircle();
                                    dbCircle.setName("_corpUsers_");
                                    dbCircle.setCategory("internal");
                                    dbCircle.setOwnerId(dbCorp.getId().toString());
                                    dbCircle.setUpdatedAt(new Date());

                                    dbCircle.getUserList().add(dbUser.getId().toString());
                                    getCircleDao().save(dbCircle);
                                    dbCorp.setCircle(dbCircle);
                                    getCorporateDao().save(dbCorp);
                                } else {
                                    if (!dbCircle.getUserList().contains(dbUser.getId().toString())) {
                                        dbCircle.getUserList().add(dbUser.getId().toString());
                                        dbCircle.setUpdatedAt(new Date());
                                        getCircleDao().save(dbCircle);
                                    }
                                }

                                //We assume that a package Id is associated with one corporate only.
                                break;
                            }
                        }
                    }

                    found = false;
                    DBTest dbTest = null;
                    for (DBTest dTest : dbLabTest.getTests()) {
                        if (testId != 0 && dTest.getTestid() == testId) {
                            dbTest = dTest;
                            found = true;
                            break;
                        }

                        if (testIdStr != null && dTest.getTestidString().equals(testIdStr)) {
                            dbTest = dTest;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        dbTest = new DBTest();
                        if (testId != 0)
                            dbTest.setTestid(testId);
                        else
                            dbTest.setTestid(testIdStr);

                        //Validate if this test is blocked && eliminate it.
                        if (siteDetails.getBlackListTests() != null) {
                            List<String> BlockList = Arrays.asList(siteDetails.getBlackListTests());

                            if (BlockList.contains(Integer.toString(testId))) {
                                dbTest.setBlocked(1);
                            }
                        }

                        try {
                            int hId = Integer.parseInt(hcuId);
                            if (siteDetails.getBlackListTests() != null) {
                                if (Arrays.binarySearch(siteDetails.getBlackListTests(), hId) >= 0) {
                                    dbTest.setBlocked(1);
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        dbTest.setSource(siteDetails.getSiteKey());

                        Object testName = specialResultObj.getService_name();
                        if (testName == null || testName.toString().isEmpty()) {
                            failedCount += 1;
                            failedSpecialResultList.add(specialResultObj);
                            this.specialResultList.remove(specialResultObj);
                            continue;
                        }

                        dbTest.setTestName(testName.toString());
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "For UHID: " + uhid + " adding new Test: " + dbTest.getTestName(), Level.INFO, null);

                        if (specialResultObj.getDepartment_name() != null)
                            dbTest.setDepartmentName(specialResultObj.getDepartment_name());
                        if (specialResultObj.getStatic_comment() != null)
                            dbTest.setStatic_comments(specialResultObj.getStatic_comment());
                        if (specialResultObj.getPatienttype() != null)
                            dbTest.setPatientservice(specialResultObj.getPatienttype());
                        if (specialResultObj.getIdentifier() != null)
                            dbTest.setIdentifier(specialResultObj.getIdentifier());
                        if (specialResultObj.getVisitId() != null)
                            dbTest.setVisitId(specialResultObj.getVisitId());
                        if (specialResultObj.getIdentifier() != null && (dbLabTest.getIdentifier() == null || dbLabTest.getIdentifier().isEmpty()))
                            dbLabTest.setIdentifier(specialResultObj.getIdentifier());
                        if (specialResultObj.getVisitId() != null && (dbLabTest.getVisitId() == null || dbLabTest.getVisitId().isEmpty()))
                            dbLabTest.setVisitId(specialResultObj.getVisitId());
                        if (specialResultObj.getSpecimen() != null)
                            dbTest.setSpecimen(specialResultObj.getSpecimen());
                        if (specialResultObj.getChecked_by() != null)
                            dbTest.setCheckedBy(specialResultObj.getChecked_by());
                        if (specialResultObj.getSigning_doc_name() != null)
                            dbTest.setSigningDocName(specialResultObj.getSigning_doc_name());
                        if (specialResultObj.getDoc_sign() != null)
                            dbTest.setDoc_sign(specialResultObj.getDoc_sign());
                        if (specialResultObj.getRefer_doc_name() != null)
                            dbTest.setLaboratoryHod(specialResultObj.getRefer_doc_name());

                        try {
                            if (specialResultObj.getCollected_on() != null && specialResultObj.getCollected_on().length() != 0) {
                                String crtTime = specialResultObj.getCollected_on();
                                dbTest.setCollectedOn(dateformat.parse(getDateWithoutGMT(crtTime)));
                            }
                            if (specialResultObj.getCreated_at() != null && specialResultObj.getCreated_at().length() != 0) {
                                String crtTime = specialResultObj.getCreated_at();
                                dbTest.setReportedOn(dateformat.parse(getDateWithoutGMT(crtTime)));
                            }

                            if (specialResultObj.getFirst_report_printed_on() != null && specialResultObj.getFirst_report_printed_on().length() != 0) {
                                String crtTime = specialResultObj.getFirst_report_printed_on();
                                dbTest.setFirst_report_printed_on(dateformat.parse(getDateWithoutGMT(crtTime)));
                            }

                            if (specialResultObj.getPrinted_on() != null && specialResultObj.getPrinted_on().length() != 0) {
                                String crtTime = specialResultObj.getPrinted_on();
                                dbTest.setPrintedOn(dateformat.parse(getDateWithoutGMT(crtTime)));
                            }

                            if (specialResultObj.getReceived_on() != null && specialResultObj.getReceived_on().length() != 0) {
                                String crtTime = specialResultObj.getReceived_on();
                                dbTest.setReceivedOn(dateformat.parse(getDateWithoutGMT(crtTime)));
                            }
                        } catch (Exception e) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse Printed Dates " + specialResultObj.getFirst_report_printed_on() + " for OrderId " + dbLabTest.getOrderId(), Level.INFO, null);
                        }
                        if (specialResultObj.getDrn() != null)
                            dbTest.setDrn(specialResultObj.getDrn());

                        // TODO :  look into this
                        this.getSiteStats().setTestsImported(this.getSiteStats().getTestsImported() + 1);
                        getNotificationDao().addNotification(dbUser, "LabTest -" + dbTest.getTestName() + " is getting imported",
                                NotificationLevel.System, NotificationType.LabTest, siteDetails.getSiteKey(), null, uhid);
                        dbLabTest.getTests().add(dbTest);

                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Coming to send LAB-INVITATION SMS " + uhid + " for site " + siteDetails.getSiteName(), Level.INFO, null);

                        if (uhid.contains("DHNH")) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is DHNH and siteKey : " + siteDetails.getSiteKey() + " : locationID " + siteDetails.getLocationId(), Level.INFO, null);
                        }
                        if (uhid.contains("DAVS")) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is DAVS and siteKey : " + siteDetails.getSiteKey() + " : locationID " + siteDetails.getLocationId(), Level.INFO, null);
                        }
                        if (uhid.contains("KHS")) {
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is KHS and siteKey : " + siteDetails.getSiteKey() + " : locationID " + siteDetails.getLocationId(), Level.INFO, null);
                        }

                        boolean cond1 = (siteDetails.isSms() && !(siteDetails.getEntityName().toUpperCase().equals("APOLLO")) && uhid != null && !uhid.contains("DHNH") && !uhid.contains("DAVS") && !uhid.contains("DAVI") && !uhid.contains("KHS"));
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS,
                                LogType.GENERAL, "Condition 1 " + cond1, Level.INFO, null);

                        if (cond1) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date());
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            long currentDate = cal.getTimeInMillis();
                            List<DBsms> dbSmsList = getSmsDao().findLabTestIntimationSMSByuserObjectId(dbUser.getId(), currentDate);
                            long testDateLong = dbLabTest.getTestDate() != null ? dbLabTest.getTestDate().getTime() : 0L;
                            long delayDays = getDaysBetweenTwoDates(testDateLong, currentDate);

                            boolean cond2 = ((dbSmsList == null || dbSmsList.size() == 0) && delayDays <= getIntimationSMSDurationDays());
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Condition 2 " + cond2, Level.INFO, null);

                            if (cond2) {
                                try {
                                    boolean cond3 = true;
                                    if ((specialResultObj.getClientName() != null && specialResultObj.getClientName().contains("24X7"))) {
                                        cond3 = false;
                                    }
                                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Condition 3 " + cond3, Level.INFO, null);
                                    if (cond3) {
                                        DBsms dbSms = new DBsms();
                                        dbSms.setSmsPurpose("LABTEST-INTIMATION");
                                        dbSms.setMobileNumber(dbUser.getMobileNumber());
                                        dbSms.setDbUser(dbUser);
                                        dbSms.setPatientName(dbUser.getFirstName());
                                        dbSms.setUhid(uhid);
                                        if (generatedString != null)
                                            dbSms.setDownloadId(generatedString);
                                        dbSms.setSiteKey(siteId);
                                        dbSms.setWhatsapp(true);
                                        dbSms.setSendAt(new Date());

                                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Coming to save LAB-INVITATION SMS " + uhid + " for site " + siteDetails.getSiteName(), Level.INFO, null);
                                        getSmsDao().save(dbSms);
                                    }
                                    // TODO: SMS Service (commented in source code)
                                    //config.getSMSService().sendLabTestIntimation(dbUser.getMobileNumber(), String.valueOf(smsId));
                                } catch (Exception e) {
                                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "PHR | OldProcessorJob | Sending Lab Test Intimation SMS | Error " + Utility.stringifyException(e), Level.INFO, null);
                                }
                            }
                        }
                    }

                    Object imp = specialResultObj.getImpression();
                    if (imp == null || imp.toString().isEmpty())
                        imp = specialResultObj.getInterpretation();
                    if (imp != null && !imp.toString().isEmpty()) {
                        String result = imp.toString();
                        dbTest.setObservation(result);
                        if (result.startsWith("{\\rtf1")) {
                            //Convert RTf to Html
                            dbTest.setObservation((RtfToHtml.convertRtfToHtml(result)));
                        }
                    }

                    BasicDBList testParams = (BasicDBList) specialResultObj.getSpecial_result_values();
                    if (testParams == null) {
                        testParams = new BasicDBList();
                        DBObject testParam = new BasicDBObject();
                        if (specialResultObj.getParameter_id() != null)
                            testParam.put("parameter_id", specialResultObj.getParameter_id());
                        if (specialResultObj.getParametername_new() != null)
                            testParam.put("parametername_new", specialResultObj.getParametername_new());
                        if (specialResultObj.getParameter_name() != null)
                            testParam.put("parameter_name", specialResultObj.getParameter_name());
                        if (specialResultObj.getReference_val() != null)
                            testParam.put("reference_val", specialResultObj.getReference_val());
                        if (specialResultObj.getUnit() != null)
                            testParam.put("unit", specialResultObj.getUnit());
                        if (specialResultObj.getResult() != null)
                            testParam.put("result", specialResultObj.getResult());
                        if (specialResultObj.getSequence() != null)
                            testParam.put("sequence", specialResultObj.getSequence());
                        if (specialResultObj.getUpdated_at() != null)
                            testParam.put("updated_at", specialResultObj.getUpdated_at());
                        if (specialResultObj.getMax() != null)
                            testParam.put("max", specialResultObj.getMax());
                        if (specialResultObj.getMin() != null)
                            testParam.put("min", specialResultObj.getMin());
                        if (specialResultObj.getCondition() != null)
                            testParam.put("condition", specialResultObj.getCondition());
                        testParams.add(testParam);
                    }
                    for (String key : testParams.keySet()) {
                        DBObject testParam = (DBObject) testParams.get(key);

                        found = false;
                        int testParamId = 0;
                        DBLabResult dbLabResult = null;
                        if (testParam.get("parameter_id") != null && !testParam.get("parameter_id").toString().isEmpty()) {
                            testParamId = Integer.parseInt(testParam.get("parameter_id").toString());
                            for (DBLabResult lResult : dbTest.getResults()) {
                                if (lResult.getParameterId() == testParamId) {
                                    dbLabResult = lResult;
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            dbLabResult = new DBLabResult();
                            dbLabResult.setParameterId(testParamId);
                            if (testParam.get("parameter_name") != null)
                                dbLabResult.setParameterName(testParam.get("parameter_name").toString());
                            if (testParam.get("parametername_new") != null)
                                dbLabResult.setStd_parameterName(testParam.get("parametername_new").toString());
                            if (testParam.get("reference_val") != null)
                                dbLabResult.setRange(testParam.get("reference_val").toString());
                            if (testParam.get("unit") != null)
                                dbLabResult.setUnit(testParam.get("unit").toString());
                            if (testParam.get("result") != null) {
                                String result = testParam.get("result").toString();
                                dbLabResult.setResult(result);
                                if (result.startsWith("{\\rtf1")) {
                                    //Convert RTf to Html
                                    dbLabResult.setResult((RtfToHtml.convertRtfToHtml(result)));
                                }
                            }

                            if (testParam.get("sequence") != null && !testParam.get("sequence").toString().isEmpty())
                                dbLabResult.setSequence(Integer.parseInt(testParam.get("sequence").toString()));
                            try {
                                String updTime = testParam.get(TestData.updated_at.name()).toString();
                                if (specialResultObj.getResult_date() != null && specialResultObj.getResult_date().length() != 0)
                                    updTime = specialResultObj.getResult_date();
                                dbLabResult.setResultDateTime(dateformat.parse(getDateWithoutGMT(updTime)));
                            } catch (Exception e) {
                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse TestParam Updateddate " + " for OrderId " + dbLabTest.getOrderId(), Level.INFO, null);
                            }
                            dbTest.getResults().add(dbLabResult);
                        } else {
                            Date testDate = new Date();
                            try {
                                testDate = dateformat.parse(getDateWithoutGMT(testParam.get("updated_at").toString()));
                            } catch (Exception e) {
                                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse TestParam Updateddate " + " for TestParamID " + testParamId, Level.INFO, null);
                            }
                            if (testDate.after(dbLabResult.getResultDateTime()) && testParam.get(TestData.result.name()) != null) {
                                String result = testParam.get("result").toString();
                                dbLabResult.setResult(result);
                                if (result.startsWith("{\\rtf1")) {
                                    //Convert RTf to Html
                                    dbLabResult.setResult((RtfToHtml.convertRtfToHtml(result)));
                                }
                                // TODO : look into this
                                this.getSiteStats().setLastResultImportDate(testDate);
                            }
                        }

                        boolean validMaxString = true;
                        String sex = null;
                        if (dbUser.getUserBasicInfo() != null && dbUser.getUserBasicInfo().getSex() != null)
                            sex = dbUser.getUserBasicInfo().getSex();
                        Object max = testParam.get(TestData.max.name());
                        Object min = testParam.get(TestData.min.name());
                        String maxStr = (max == null) ? "" : max.toString();
                        String minStr = (min == null) ? "" : min.toString();
                        if (testParam.get(TestData.condition.name()) != null && sex != null) {
                            String condString = testParam.get(TestData.condition.name()).toString().toLowerCase();
                            if (condString.length() != 0 && !condString.equals(sex)) {
                                validMaxString = false;
                            }
                        }

                        if (validMaxString)
                            dbLabResult.setOutOfRange(isOutofRange(dbLabResult, maxStr, minStr));
                    }

                    //Save the labTest with the update.
                    getLabTestDao().save(dbLabTest);

                    // uhid-order_id-locationId-created_at-department_name-parameter_id-specimen-identifier-bill_id-updated_at
                    List<String> fileParams = getFileNameParams(specialResultObj);

                    loggingFileName = createFileName(fileParams);

                    try {
                        fileConsumedDate = logDateTimeformat.parse(specialResultObj.getUpdated_at());
                        logTurnAroundTime(specialResultObj, fileConsumedDate, loggingFileName, logMetaData);
                        logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                    } catch (Exception e) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.INFO, null);
                    }
                    this.getSpecialResultList().remove(specialResultObj);
                    successCount += 1;
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedSpecialResultList.add(specialResultObj);
                    this.getSpecialResultList().remove(specialResultObj);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedSpecialResultList.add(specialResultObj);
                    this.getSpecialResultList().remove(specialResultObj);
                    getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }
            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedSpecialResultList.isEmpty()) {
                ffinput.specialResults = failedSpecialResultList;
                String flowFileContent = this.getGson().toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.specialResults = this.getSpecialResultList();

        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
            setFlowFileDate(inputFF, session);
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        } finally {
            if (this.getMongoClientService() != null && this.getSiteStats() != null)
                Utility.saveSiteStats(this.mongoClientService.getMongoClient(), this.getSiteStats());
        }

        String flowFileContent = this.getGson().toJson(ffinput);
        session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
        session.transfer(inputFF, REL_SUCCESS);
        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private void log_site_processing_time(Long duration, int totalProcessed, int successCount, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("site_name", this.getSiteDetails().getSiteName());
        otherDetails.put("site_key", this.getSiteDetails().getSiteKey());
        otherDetails.put("total_processed", totalProcessed);
        otherDetails.put("success_count", successCount);
        otherDetails.put("failed_count", totalProcessed - successCount);
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, this.getSiteDetails().getSiteName() + " processed successfully.", Level.INFO, otherDetails);
    }

    private void initializeDaos(MongoClient client) {
        setIdentityDao(new IdentityDao(client));
        setUserDao(new UserDao(client));
        setLabResultDAO(new LabResultDownloadDao(client));
        setLabTestDao(new LabTestDao(client));
        setNotificationDao(new NotificationDao(client));
        setCircleDao(new CircleDao(client));
        setCorporateDAO(new CorporateDao(client));
        setSmsDao(new SMSDao(client));
        setEntityDao(new EntityDao(client));
    }

    public SiteDetails getActualSiteDetails(String siteDetails) {
        return this.getGson().fromJson(siteDetails, SiteDetails.class);
    }

    public String getSiteKeyFromUhid(String uhid, Map<String, Object> logMetaData) {
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
        getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
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

    private DBEntity getEntity(String entityName, Map<String, Object> logMetaData) {
        if (entitys.containsKey(entityName))
            return entitys.get(entityName);

        DBEntity dbEntity = getEntityDao().getEntity(entityName);
        if (dbEntity == null) {
            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Unable to find the Entity for entity name: " + entityName, Level.ERROR, null);
        }

        entitys.put(entityName, dbEntity);
        return dbEntity;
    }

    private long daysBetween(Date d1, Date d2) {
        return Math.abs((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    private long getDaysBetweenTwoDates(long firstDate, long secondDate) {
        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(new Date(firstDate));

        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTime(new Date(secondDate));

        long diffTime = secondCalendar.getTimeInMillis() - firstCalendar.getTimeInMillis();
        long diffDays = diffTime / (24 * 60 * 60 * 1000);

        return diffDays;
    }

    private boolean isOutofRange(DBLabResult dbLabResult, String maxStr, String minStr) {
        float val = 0;
        try {
            val = Float.parseFloat(dbLabResult.getResult());
        } catch (Exception e) {
            return false;
        }

        float min = -1;
        float max = -1;
        try {
            max = Float.parseFloat(maxStr);
        } catch (Exception ignored) {
        }
        try {
            min = Float.parseFloat(minStr);
        } catch (Exception ignored) {
        }

        if (min == -1 && max == -1 && dbLabResult.getRange() != null) {
            String[] subRange = dbLabResult.getRange().split("-");
            if (subRange.length != 2)
                return false;
            try {
                min = Float.parseFloat(subRange[0]);
            } catch (Exception ignored) {
            }
            try {
                max = Float.parseFloat(subRange[1]);
            } catch (Exception ignored) {
            }
        }

        if (dbLabResult.getRange() == null || dbLabResult.getRange().isEmpty()) {
            if (min != -1)
                dbLabResult.setRange(minStr + " - ");
            if (max != -1) {
                if (min == -1)
                    dbLabResult.setRange(" - ");
                dbLabResult.setRange(dbLabResult.getRange() + maxStr);
            }
        }

        if (min != -1 && val < min)
            return true;

        if (max != -1 && val > max)
            return true;

        return false;
    }

    private void logTurnAroundTime(SpecialResult specialResultObj, Date jsonUpdatedDate, String fileName, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        Date currentDate = new Date();
        long tat = (currentDate.getTime() - jsonUpdatedDate.getTime()) / 1000;
        otherDetails.put("receivedDate", jsonUpdatedDate);
        otherDetails.put("processedDate", currentDate);
        otherDetails.put("turn_around_time", tat);

        Object serviceName = specialResultObj.getService_name();
        if (serviceName != null && (serviceName.toString().toUpperCase()).contains("CULTURE"))
            otherDetails.put("isCultureReport", true);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "turn around time for " + fileName + " is:  " + tat, Level.INFO, otherDetails);
    }

    private String createFileName(List<String> fileValues) {
        String fileName = "";
        for (Integer i = 0; i < fileValues.size(); i++) {
            String val = fileValues.get(i);
            if (val != null && !val.equals("")) {
                fileName += val;
            }
            if (i + 1 != fileValues.size()) {
                fileName += "_";
            }
        }
        return fileName;
    }

    private List<String> getFileNameParams(SpecialResult specialResultObj) {
        List<String> fileParams = new ArrayList<>();
        fileParams.add((specialResultObj.getUhid() == null) ? null : specialResultObj.getUhid());
        fileParams.add((specialResultObj.getOrder_id() == null) ? null : specialResultObj.getOrder_id());
        fileParams.add(getSiteDetails().getLocationId());
        fileParams.add((specialResultObj.getCreated_at() == null) ? null : specialResultObj.getCreated_at());
        fileParams.add((specialResultObj.getDepartment_name() == null) ? null : specialResultObj.getDepartment_name());
        fileParams.add((specialResultObj.getParameter_id() == null) ? null : specialResultObj.getParameter_id());
        fileParams.add((specialResultObj.getSpecimen() == null) ? null : specialResultObj.getSpecimen());
        fileParams.add((specialResultObj.getIdentifier() == null) ? null : specialResultObj.getIdentifier());
        fileParams.add((specialResultObj.getBill_id() == null) ? null : specialResultObj.getBill_id());
        fileParams.add(fileNameFormat.format(new Date()));
        return fileParams;
    }

    private void logCursorSize(Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("cursor_size", this.getSpecialResultList().size());
        otherDetails.put("threshold_limit", mongoDbQueryLimit);
        otherDetails.put("hasMoreToProcess", mongoDbQueryLimit == this.getSpecialResultList().size());
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Got  " + this.getSpecialResultList().size() + " lab tests for site name " + siteDetails.getSiteName(), Level.INFO, otherDetails);
    }

    private boolean isValidFlowFileContent(FFInput ffInput) {
        if (ffInput.specialResults == null)
            return false;
        return true;
    }

    private void loadFlowFileContents(FFInput ffInput) {
        this.setSpecialResultList(ffInput.specialResults);
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
    }
}
