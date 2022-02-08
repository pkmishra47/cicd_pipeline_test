package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
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
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.IdType;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Pradeep Mishra
 */
//@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"daxoperation", "dataman", "patients", "oldsite"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class PatientProcessor extends AbstractProcessor {
    private static final String processorName = "PatientProcessor";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private ServiceUtil serviceUtil = null;
    private static Gson gson = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private String prismProfileApiUrl = null;
    private String prismProfileApiKey = null;
    private Integer mongoDbQueryLimit = 5000;
    private List<Patient> patientList = null;
    private Long ffCreatedEpoch = null;

    private IdentityDao identityDao;
    private UserDao userDao;
    private SugarInfoDao sugarInfoDao;
    private EntityDao entityDao;
    private SMSDao smsDao;
    private NotificationDao notificationDao;

    protected MongoClientService mongoClientService;
    private boolean debugOn = false;

    public String getProcessorName() {
        return PatientProcessor.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public ServiceUtil getServiceUtil() {
        if (this.serviceUtil == null)
            this.serviceUtil = new ServiceUtil();
        return this.serviceUtil;
    }

    public Gson getGson() {
        if (PatientProcessor.gson == null)
            PatientProcessor.gson = GsonUtil.getGson();
        return PatientProcessor.gson;
    }

    public String getPrismProfileApiUrl() {
        return prismProfileApiUrl;
    }

    public String getPrismProfileApiKey() {
        return prismProfileApiKey;
    }

    public List<Patient> getPatientList() {
        return this.patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public Map<String, SiteDetails> getSiteMasterMap() {
        return siteMasterMap;
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

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
    }

    public SiteStats getSiteStats() {
        return this.siteStats;
    }

    public IdentityDao getIdentityDao() {
        return identityDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public SugarInfoDao getSugarInfoDao() {
        return sugarInfoDao;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public SMSDao getSmsDao() {
        return smsDao;
    }

    public NotificationDao getNotificationDao() {
        return notificationDao;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public Long getFfCreatedEpoch() {
        return this.ffCreatedEpoch;
    }

    public void setFfCreatedEpoch(Long ffCreatedEpoch) {
        this.ffCreatedEpoch = ffCreatedEpoch;
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

    public static final PropertyDescriptor PRISM_PROFILE_API_URL = new PropertyDescriptor
            .Builder().name("PRISM_PROFILE_API_URL")
            .displayName("PRISM PROFILE API URL")
            .description("provide apollo247 webhook url to push new uhid info")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PRISM_PROFILE_API_KEY = new PropertyDescriptor
            .Builder().name("PRISM_PROFILE_API_KEY")
            .displayName("PRISM PROFILE API KEY")
            .description("provide apollo247 webhook API key")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
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
            .name("success")
            .description("marks process successful when processor achieves success condition.")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("marks process failure when processor achieves failure condition.")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private String processorID;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, PRISM_PROFILE_API_URL, PRISM_PROFILE_API_KEY, QUERY_LIMIT, DEBUG);
        this.relationships = Set.of(REL_SUCCESS, REL_FAILURE);
        this.processorID = context.getIdentifier();
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    public final String getProcessorID() {
        return this.processorID;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        prismProfileApiUrl = context.getProperty(PRISM_PROFILE_API_URL).toString();
        prismProfileApiKey = context.getProperty(PRISM_PROFILE_API_KEY).toString();
        mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
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
        String ffContent;
        FFInput ffinput = new FFInput();
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        int totalProcessed = 0, successCount = 0;
        MongoClient client;
        List<Patient> failedPatientsList = new ArrayList<>();

        try {
            logMetaData.put("processor_name", this.getProcessorName());

            if (inputFF == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            ffContent = readFlowFileContent(inputFF, session);
            ffinput = this.getGson().fromJson(ffContent, FFInput.class);
            loadFFCreatedEpoch(inputFF);

            if (!isValidFlowFileContent(ffinput))
                throw new Exception("flowfile doesn't have valid siteDetails or siteMasterMap or PatientList");

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
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, getSiteDetails().getEntityName());
            initializeDaos(client);

            if (debugOn)
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Daos initialized......", Level.INFO, null);

            totalProcessed = this.getPatientList().size();
            while (!this.getPatientList().isEmpty()) {
                Patient patientOrig = this.getPatientList().get(0);

                try {
                    if (patientOrig.getPatient() != null)
                        patientOrig = patientOrig.getPatient();

                    String uhid = patientOrig.getUhid();
                    String siteId = getSiteKeyFromUhid(uhid, logMetaData);

                    logMetaData.put(Constants.UHID, uhid);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Processing Patient with uhid: " + patientOrig.getUhid(), Level.INFO, null);

                    if (siteId == null || siteId.trim().length() == 0) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "PHR | DAXOperations | oldProcesorJob | processPatients | SiteName " + siteDetails.getSiteName() + " Uhid " + uhid + " Site Id is Null ", Level.ERROR, null);
                        this.getPatientList().remove(patientOrig);
                        continue;
                    }

                    DBUser dbUser = this.getIdentityDao().findByUhid(uhid, siteId);
                    // if user object not created or it has been created by putting some placeholder
                    if (dbUser == null || (dbUser.getUpdateIdentityObject() != null && dbUser.getUpdateIdentityObject())) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Adding user with UHID " + uhid + " for site " + siteDetails.getSiteName(), Level.INFO, null);

                        DBUser user = createNewDBUser(dbUser, patientOrig);
                        setUserMobileNumber(user, patientOrig);
                        setUserIdDetails(user, patientOrig);
                        setUserBasicInfo(user, patientOrig, uhid, logMetaData);
                        setUserContactInfo(user, patientOrig, uhid, logMetaData);
                        setUserPreferenceInfo(user);
                        setUserMiscellaneousinfo(user);
                        this.getUserDao().save(user);

                        try {
                            setUserSugarinfo(user, patientOrig, uhid, siteId, logMetaData);
                            this.getUserDao().save(user);
                        } catch (Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse billing date " + patientOrig.getBilling_date() + " for UHID " + uhid, Level.INFO, null);
                            continue;
                        }
                        setIdentityInfo(user, patientOrig, uhid, siteId, logMetaData);
                        setInvitationSMS(user, uhid, siteId, logMetaData);
                        user.setUpdateIdentityObject(false);
                        this.getUserDao().save(user);
                        this.getSiteStats().setPatientsImported(this.getSiteStats().getPatientsImported() + 1);
                        pushPrismProfileUpdatesTo247Queue(uhid, user, logMetaData);
                    } else {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "User already exists for uhid: " + uhid, Level.INFO, null);
                        setUserIdDetails(dbUser, patientOrig);
                        updateExistingUserInfo(dbUser, patientOrig, uhid, siteId, logMetaData);
                        setAlreadyImportedNotification(dbUser, uhid, siteId);
                        this.getUserDao().save(dbUser);
                    }
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                    this.getPatientList().remove(patientOrig);
                    successCount += 1;
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedPatientsList.add(patientOrig);
                    this.getPatientList().remove(patientOrig);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedPatientsList.add(patientOrig);
                    this.getPatientList().remove(patientOrig);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }
            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedPatientsList.isEmpty()) {
                ffinput.patients = failedPatientsList;
                String flowFileContent = this.getGson().toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.patients = this.getPatientList();
        } catch (Exception ex) {
            setFlowFileDate(inputFF, session);
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        } finally {
            if (this.getMongoClientService() != null && this.getSiteStats() != null)
                Utility.saveSiteStats(this.getMongoClientService().getMongoClient(), this.getSiteStats());
        }

        String flowFileContent = this.getGson().toJson(ffinput);
        session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
        session.transfer(inputFF, REL_SUCCESS);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
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

    public DBUser createNewDBUser(DBUser dbUser, Patient patient) {

        // if user object is already created by putting some placeholder then do not create new user object
        DBUser user = dbUser;
        if (dbUser == null)
            user = new DBUser();

        Object firstName = patient.getFirst_name();
        if (firstName != null)
            user.setFirstName(firstName.toString());
        Object middleName = patient.getMiddle_name();
        if (middleName != null)
            user.setMiddleName(middleName.toString());
        Object lastName = patient.getLast_name();
        if (lastName != null)
            user.setLastName(lastName.toString());
        return user;
    }

    public void setUserMobileNumber(DBUser user, Patient patient) {
        Object phone = patient.getMobile();
        if (phone == null)
            phone = patient.getPhone();
        if (phone != null)
            user.setMobileNumber(formatPhone(phone.toString()));
    }

    public void setUserIdDetails(DBUser user, Patient patient) {
        Object idType = patient.getUidtype();
        Object idNum = patient.getUidnumber();
        if (idType != null && idNum != null) {
            if (idType.toString().equals(IdType.license.getValue()))
                user.setLicense(idNum.toString());

            if (idType.toString().equals(IdType.pancard.getValue()))
                user.setPanCard(idNum.toString());

            if (idType.toString().equals(IdType.aadharcard.getValue()))
                user.setAadharNumber(idNum.toString());
        }
    }

    public void setUserBasicInfo(DBUser user, Patient patient, String uhid, Map<String, Object> logMetaData) {
        DBUserBasicInfo basicInfo = new DBUserBasicInfo();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat pgDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        user.setUserBasicInfo(basicInfo);
        try {
            basicInfo.setDateOfBirth(dateformat.parse(getDateWithoutGMT(patient.getDob())));
        } catch (Exception exception) {
            try {
                basicInfo.setDateOfBirth(pgDateFormat.parse(patient.getDob()));
            } catch (Exception expception) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse DOB " + patient.getDob() + " for UHID " + uhid, Level.WARN, null);
            }
        }

        Object sex = patient.getSex();
        if (sex != null) {
            String origSex = sex.toString().toUpperCase();
            String strSex = "male";
            if (origSex.startsWith("F"))
                strSex = "female";
            basicInfo.setSex(strSex);
        }
    }

    public void setUserContactInfo(DBUser user, Patient patient, String uhid, Map<String, Object> logMetaData) {
        DBUserContactInfo contactInfo = new DBUserContactInfo();
        user.setUserContactInfo(contactInfo);

        Object address = patient.getAddress();
        if (address != null)
            contactInfo.setAddressLine1(address.toString());
        Object city = patient.getCity();
        if (city != null)
            contactInfo.setCity(city.toString());
        Object state = patient.getState();
        if (state != null)
            contactInfo.setState(state.toString());
        Object pin = patient.getPin();
        if (pin != null && !pin.toString().isEmpty()) {
            try {
                contactInfo.setPincode(Integer.parseInt(pin.toString()));
            } catch (Exception ex) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "unable to parse pincode: " + pin.toString() + " for UHID " + uhid, Level.WARN, null);
            }
        }
        Object email = patient.getEmail();
        if (email != null)
            contactInfo.setEmail1(email.toString());
    }

    public void setUserPreferenceInfo(DBUser user) {
        DBUserPreferenceInfo preferenceInfo = new DBUserPreferenceInfo();
        preferenceInfo.setSmsAlert("YES");
        preferenceInfo.setEmailAlert("YES");
        user.setUserPreferenceInfo(preferenceInfo);
    }

    public void setUserMiscellaneousinfo(DBUser user) {
        //Set imported and updatedAt date
        Date currentDate = new Date();
        user.setDateImported(currentDate);
        user.setUpdatedAt(currentDate);

        // Set EntityName
        if (!(user.getEntitys().contains(siteDetails.getEntityName())))
            user.getEntitys().add(siteDetails.getEntityName());

        // if user status is already set by some other way then do not change it.
        if (user.getStatus() == null)
            user.setStatus(DBUser.UserStatus.NOT_ACTIVATED);

        //Set role
        List<String> userRoles = new ArrayList<>();
        userRoles.add("user");
        user.setRoles(userRoles);
    }

    public void setUserSugarinfo(DBUser user, Patient patient, String uhid, String siteId, Map<String, Object> logMetaData) throws ParseException {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        Object sugarFlag = patient.getSUGARFLAG();
        String sugarFlagValue = null;
        if (sugarFlag != null) {
            sugarFlagValue = sugarFlag.toString().toUpperCase();
        }

        if (sugarFlagValue != null && sugarFlagValue.equals("Y")) {
            if (debugOn)
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Processing sugar flag of user with UHID " + uhid + " for site " + this.getSiteDetails().getSiteName(), Level.WARN, null);

            Date billingDate;
            billingDate = dateformat.parse(getDateWithoutGMT(patient.getBilling_date()));

            List<DBSugarInfo> dbSugarInfoList = new ArrayList<>();
            DBSugarInfo dbSugarInfo;
            dbSugarInfo = new DBSugarInfo();

            dbSugarInfo.setBillingDate(billingDate);
            dbSugarInfo.setPacakgeId("-1");
            dbSugarInfo.setPackageName("NONPACKAGE");
            dbSugarInfo.setSiteType("INSTA");

            this.getSugarInfoDao().save(dbSugarInfo);
            dbSugarInfoList.add(dbSugarInfo);

            user.setSugarInfo(dbSugarInfoList);
            user.getEntitys().clear();
            user.getEntitys().add("Sugar");
            user.getRoles().add("sugar");

            user.setUpdatedAt(new Date());
            this.getSiteStats().setSugarPatientsImported(this.getSiteStats().getSugarPatientsImported() + 1);

            setSugarSMS(user, uhid, logMetaData);
            setSugarNotification(user, uhid, siteId);
        }
    }

    public void setSugarSMS(DBUser user, String uhid, Map<String, Object> logMetaData) {
        try {
            if (this.getSiteDetails().isSms() && uhid != null && !uhid.contains("DHNH") && !uhid.contains("DAVS")) {
                DBsms dbSms = new DBsms();
                dbSms.setSmsPurpose("SUGARINVITATION");
                dbSms.setMobileNumber(user.getMobileNumber());
                dbSms.setDbUser(user);
                dbSms.setUhid(uhid);
                dbSms.setSiteKey(siteDetails.getSiteKey());
                dbSms.setSendAt(new Date());
                if (debugOn)
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Saving SUGAR SMS " + uhid + " for site " + siteDetails.getSiteName(), Level.INFO, null);
                this.getSmsDao().save(dbSms);

                DBEntity dbEntity = this.getEntityDao().getEntity("Sugar");
                if (dbEntity.getSmsInvitationTwo() != null) {
                    DBsms dbSmsTwo = new DBsms();
                    dbSmsTwo.setSmsPurpose("INVITATION-TWO");
                    dbSmsTwo.setMobileNumber(user.getMobileNumber());
                    dbSmsTwo.setDbUser(user);
                    dbSmsTwo.setUhid(uhid);
                    dbSmsTwo.setSiteKey(siteDetails.getSiteKey());
                    dbSmsTwo.setEntityName("Sugar");

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.DATE, dbEntity.getSmsDelayDays());
                    Date nextDate = cal.getTime();
                    dbSmsTwo.setSendAt(nextDate);
                    this.getSmsDao().save(dbSmsTwo);
                }
            }
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
    }

    private void setSugarNotification(DBUser user, String uhid, String siteId) {
        this.getNotificationDao().addNotification(user, "UHID - " + uhid + " from " + siteDetails.getSiteName() + " - Imported Sugar Patient",
                NotificationLevel.System, NotificationType.Activation, siteId, null, uhid);
    }

    private void setAlreadyImportedNotification(DBUser dbUser, String uhid, String siteId) {
        this.getNotificationDao().addNotification(dbUser, "UHID - " + uhid + " from " + siteDetails.getSiteName() + " - Already Imported",
                NotificationLevel.System, NotificationType.UhidExist, siteId, null, uhid);
    }

    public void setIdentityInfo(DBUser user, Patient patient, String uhid, String siteId, Map<String, Object> logMetaData) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat pgDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // if identity object is already created by putting some placeholder then do not create new identity object
        DBIdentity iden = new DBIdentity();
        if (user.getUpdateIdentityObject() != null && user.getUpdateIdentityObject())
            iden = this.getIdentityDao().findByUhidSitekey(uhid, siteId);

        iden.setUhid(uhid.toUpperCase());
        iden.setUhidSource(DBIdentity.UhidSource.HOSPITAL);
        if (patient.getEmail() != null)
            iden.setEmail(patient.getEmail());
        iden.setSiteKey(siteId);

        String isIndian = "";
        if (patient.getIndian() != null)
            isIndian = patient.getIndian();

        iden.setIsIndian(isIndian.toUpperCase());
        iden.setMobileNumber(user.getMobileNumber());

        // if userID is already set to some value then do not change it.
        if (iden.getUserId() == null)
            iden.setUserId(uhid.toLowerCase() + " " + siteId);

        try {
            iden.setRegistrationDate(dateformat.parse(getDateWithoutGMT(patient.getRegistration_date())));
        } catch (Exception exception) {
            try {
                iden.setRegistrationDate(pgDateFormat.parse(patient.getRegistration_date()));
            } catch (Exception ex) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
            }
        }
        iden.setDbUser(user);
        this.getIdentityDao().save(iden);

        if (iden.getRegistrationDate().after(this.getSiteStats().getLastRegistrationDate())) {
            this.getSiteStats().setLastRegistrationDate(iden.getRegistrationDate());
            this.getSiteStats().setLastUhid(uhid);
        }
    }

    public void setInvitationSMS(DBUser user, String uhid, String siteId, Map<String, Object> logMetaData) {
        Map<String, DBEntity> entitys = new HashMap<>();

        try {
            if (debugOn)
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "sending INVITATION SMS " + uhid + " for site " + this.getSiteDetails().getSiteName(), Level.INFO, null);
            if (uhid.contains("DHNH") || uhid.contains("DAVS") || uhid.contains("KHS"))
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE UHID is " + uhid + " and siteKey : " + this.getSiteDetails().getSiteKey() + " : locationID " + this.getSiteDetails().getLocationId(), Level.INFO, null);
            if (this.getSiteDetails().isSms() && !uhid.contains("DHNH") && !uhid.contains("DAVS") && !uhid.contains("DAVI") && !uhid.contains("KHS")) {
                DBsms dbSms = new DBsms();
                dbSms.setSmsPurpose("INVITATION");
                dbSms.setMobileNumber(user.getMobileNumber());
                dbSms.setDbUser(user);
                dbSms.setUhid(uhid);
                dbSms.setSiteKey(siteId);
                dbSms.setSendAt(new Date());
                if (debugOn)
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "saving INVITATION SMS " + uhid + " for site " + this.getSiteDetails().getSiteName(), Level.INFO, null);

                this.getSmsDao().save(dbSms);

                //Send the Second SMS after some delay.
                for (String entityName : user.getEntitys()) {
                    if (!entitys.containsKey(entityName)) {
                        DBEntity dbEntity = this.getEntityDao().getEntity(entityName);
                        if (dbEntity == null)
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Unable to find the Entity for entity name: " + entityName, Level.INFO, null);

                        entitys.put(entityName, dbEntity);
                    }
                    DBEntity dbEntity = entitys.get(entityName);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Second Invitation SMS | Process Patients  | Entity Name " + dbEntity, Level.INFO, null);

                    if (dbEntity.getSmsInvitationTwo() != null) {
                        DBsms dbSmsTwo = new DBsms();
                        dbSmsTwo.setSmsPurpose("INVITATION-TWO");
                        dbSmsTwo.setMobileNumber(user.getMobileNumber());
                        dbSmsTwo.setDbUser(user);
                        dbSmsTwo.setUhid(uhid);
                        dbSmsTwo.setSiteKey(siteId);
                        dbSmsTwo.setEntityName(entityName);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        cal.add(Calendar.DATE, dbEntity.getSmsDelayDays());
                        Date nextDate = cal.getTime();
                        dbSmsTwo.setSendAt(nextDate);
                        this.getSmsDao().save(dbSmsTwo);
                    }
                }
            }
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
    }

    public void updateExistingUserInfo(DBUser dbUser, Patient patient, String uhid, String siteId, Map<String, Object> logMetaData) {
        boolean flag = false;

        /*
         *  Update gender if changed
         */
        Object sex_modified = patient.getSex();
        if (sex_modified != null) {
            String origSex = sex_modified.toString().toUpperCase();
            String strSex = "male";
            if (origSex.startsWith("F"))
                strSex = "female";

            if (dbUser.getUserBasicInfo() != null) {
                String sex = dbUser.getUserBasicInfo().getSex();
                if (sex != null) {
                    if (!sex.equalsIgnoreCase(strSex)) {
                        dbUser.getUserBasicInfo().setSex(strSex);
                        flag = true;
                    }
                }
            }
        }

        /*
         *  Update dob if changed
         */
        Date dob = null;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat pgDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dob = dateformat.parse(getDateWithoutGMT(patient.getDob()));
        } catch (Exception exception) {
            try {
                dob = pgDateFormat.parse(patient.getDob());
            } catch (Exception expception) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Could not parse DOB " + patient.getDob() + " for UHID " + uhid, Level.ERROR, null);
            }
        }
        if (dob != null) {
            if (dbUser.getUserBasicInfo() != null) {
                Date saved_dob = dbUser.getUserBasicInfo().getDateOfBirth();
                if (saved_dob != null) {
                    if (!dob.equals(saved_dob)) {
                        dbUser.getUserBasicInfo().setDateOfBirth(dob);
                        flag = true;
                    }
                }
            }
        }

        /*
         *  Update name if changed
         */
        Object firstName = patient.getFirst_name();
        if (firstName != null) {
            if (dbUser.getFirstName() != null && !dbUser.getFirstName().equals(firstName.toString())) {
                dbUser.setFirstName(firstName.toString());
                flag = true;
            }
        }
        Object middleName = patient.getMiddle_name();
        if (middleName != null) {
            if (dbUser.getMiddleName() != null && !dbUser.getMiddleName().equals(middleName.toString())) {
                dbUser.setMiddleName(middleName.toString());
                flag = true;
            }
        }

        Object lastName = patient.getLast_name();
        if (lastName != null) {
            if (dbUser.getLastName() != null && !dbUser.getLastName().equals(lastName.toString())) {
                dbUser.setLastName(lastName.toString());
                flag = true;
            }
        }
        if (flag) {
            dbUser.setUpdatedAt(new Date());
            this.getUserDao().save(dbUser);
        }

        /*
         *  Update other details
         */
        Object gMobile = patient.getMobile();
        if (gMobile != null) {
            if (dbUser.getMobileNumber() == null || !(dbUser.getMobileNumber().equals(formatPhone(gMobile.toString())))) {
                dbUser.setMobileNumber(formatPhone(gMobile.toString()));
                dbUser.setUpdatedAt(new Date());
                DBIdentity iden = this.getIdentityDao().findByUhidSitekey(uhid, siteId);
                iden.setMobileNumber(formatPhone(gMobile.toString()));

                String isIndian = "";
                if (patient.getIndian() != null)
                    isIndian = patient.getIndian();

                iden.setIsIndian(isIndian);
                this.getIdentityDao().save(iden);
            }
        }
        if (dbUser.getEntitys().size() == 0) {
            dbUser.getEntitys().add(siteDetails.getEntityName());
            dbUser.setUpdatedAt(new Date());
        }
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
                if (debugOn)
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

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, String flowFileContent, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.write(flowFile, out -> out.write(flowFileContent.getBytes(charset)));
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    public String formatPhone(String phone) {
        phone = phone.replaceAll("[-_ ]", "");

        if (phone.length() > 10)
            phone = phone.substring(phone.length() - 10);

        return phone.trim();
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
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.sugarInfoDao = new SugarInfoDao(client);
        this.entityDao = new EntityDao(client);
        this.smsDao = new SMSDao(client);
        this.notificationDao = new NotificationDao(client);
    }

    private void pushPrismProfileUpdatesTo247Queue(String uhid, DBUser user, Map<String, Object> logMetaData) {
        String prismProfileInfo = createPrismProfileBody(uhid, user);
        boolean hasPushed = false;

        if (!this.getPrismProfileApiUrl().isEmpty())
            hasPushed = this.getServiceUtil().update247PrismProfileInfo(this.getPrismProfileApiUrl(), this.getPrismProfileApiKey(), prismProfileInfo, logMetaData);

        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("uhid", uhid);
        otherDetails.put("hasPushed", hasPushed);
        if (!hasPushed)
            otherDetails.put("body", prismProfileInfo);

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Info related to prism profile queue update", Level.INFO, otherDetails);
    }

    private String createPrismProfileBody(String uhid, DBUser user) {
        Data data = new Data();
        UserBasicInfo userBasicInfo = new UserBasicInfo();
        UserContactInfo userContactInfo = new UserContactInfo();

        userBasicInfo.sex = user.getUserBasicInfo().getSex();
        userBasicInfo.dateOfBirth = user.getUserBasicInfo().getDateOfBirth();
        data.userBasicInfo = userBasicInfo;

        userContactInfo.email1 = user.getUserContactInfo().getEmail1();
        data.userContactInfo = userContactInfo;

        data.updateType = "update";
        data.uhid = uhid;
        data.mobileNumber = (user.getMobileNumber() != null && user.getMobileNumber().length() <= 10 ? "+91" + user.getMobileNumber() : user.getMobileNumber());
        data.firstName = user.getFirstName();
        data.lastName = user.getLastName();

        PrismProfileInfo prismProfileInfo = new PrismProfileInfo();
        prismProfileInfo.data = data;
        prismProfileInfo.syncType = "newProfile";
        return this.getGson().toJson(prismProfileInfo);
    }

    private void logCursorSize(Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("patient_list_size", this.getPatientList().size());
        otherDetails.put("threshold_limit", mongoDbQueryLimit);
        otherDetails.put("hasMoreToProcess", mongoDbQueryLimit == this.getPatientList().size());
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Got  " + this.getPatientList().size() + " objects for site name " + siteDetails.getSiteName(), Level.INFO, otherDetails);
    }

    private boolean isValidFlowFileContent(FFInput ffInput) {
        if (ffInput.patients == null)
            return false;
        return true;
    }

    private void loadFlowFileContents(FFInput ffInput) {
        this.setPatientList(ffInput.patients);
    }

    private void loadFFCreatedEpoch(FlowFile inputFF) {
        try {
            long createdEpoch = Long.parseLong(inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH));
            this.setFfCreatedEpoch(createdEpoch);
        } catch (Exception ignored) {
        }
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        if (this.getFfCreatedEpoch() == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
    }
}
