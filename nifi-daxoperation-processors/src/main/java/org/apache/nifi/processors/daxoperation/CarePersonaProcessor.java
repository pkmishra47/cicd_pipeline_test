package org.apache.nifi.processors.daxoperation;

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
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.dm.*;
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
@Tags({"daxoperations", "prescription", "pharmacy"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class CarePersonaProcessor extends AbstractProcessor {
    private static final String processorName = "CarePersonaProcessor";
    private static final Charset charset = StandardCharsets.UTF_8;
    private MongoClient client = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBUtil dbUtil = null;
    private static Gson gson = null;
    private SiteStats siteStats = null;
    private IdentityDao identityDao;
    private UserDao userDao;
    private PrescriptionDao prescriptionDao;
    private CarePersonaDao carePersonaDao;
    private DaxDao daxDao;
    private String docMappingCSVPath = "";
    private Map<String, ArrayList<String>> docMap = new HashMap<>();
    private String deeplinkGenerationApiUrl = "";
    private String deeplinkGenerationApiKey = "";
    private ServiceUtil serviceUtil = null;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    protected MongoClientService mongoClientService;
    private String deeplinkPid = "";
    private String deeplinkC = "";
    private boolean deeplinkIsRetargeting = false;
    private String deeplinkValue = "";
    private String deeplinkAfDP = "";
    private String deeplinkAfIosUrl = "";
    private String deeplinkTtl = "";
    private boolean isDeferredDeeplink = false;

    public String getProcessorName() {
        return CarePersonaProcessor.processorName;
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
        if (CarePersonaProcessor.gson == null)
            CarePersonaProcessor.gson = GsonUtil.getGson();
        return CarePersonaProcessor.gson;
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

    public CarePersonaDao getCarePersonaDao() {
        return this.carePersonaDao;
    }

    public void setCarePersonaDao(CarePersonaDao carePersonaDao) {
        this.carePersonaDao = carePersonaDao;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public DaxDao getDaxDao() {
        return this.daxDao;
    }

    public void setDaxDao(DaxDao daxDao) {
        this.daxDao = daxDao;
    }

    public PrescriptionDao getPrescriptionDao() {
        return this.prescriptionDao;
    }

    public void setPrescriptionDao(PrescriptionDao prescriptionDao) {
        this.prescriptionDao = prescriptionDao;
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

    public String getDocMappingCSVPath() {
        return this.docMappingCSVPath;
    }

    public void setDocMappingCSVPath(String path) {
        this.docMappingCSVPath = path;
    }

    public Map<String, ArrayList<String>> getDocMap() {
        return this.docMap;
    }

    public void setDocMap(Map<String, ArrayList<String>> docMap) {
        this.docMap = docMap;
    }

    public String getDeeplinkGenerationApiUrl() {
        return this.deeplinkGenerationApiUrl;
    }

    public void setDeeplinkGenerationApiUrl(String url) {
        this.deeplinkGenerationApiUrl = url;
    }

    public String getDeeplinkGenerationApiKey() {
        return this.deeplinkGenerationApiKey;
    }

    public void setDeeplinkGenerationApiKey(String key) {
        this.deeplinkGenerationApiKey = key;
    }

    public ServiceUtil getServiceUtil() {
        if (this.serviceUtil == null)
            this.serviceUtil = new ServiceUtil();
        return this.serviceUtil;
    }

    public String getDeeplinkPid() {
        return deeplinkPid;
    }

    public void setDeeplinkPid(String deeplinkPid) {
        this.deeplinkPid = deeplinkPid;
    }

    public String getDeeplinkC() {
        return deeplinkC;
    }

    public void setDeeplinkC(String deeplinkC) {
        this.deeplinkC = deeplinkC;
    }

    public boolean isDeeplinkIsRetargeting() {
        return deeplinkIsRetargeting;
    }

    public void setDeeplinkIsRetargeting(boolean deeplinkIsRetargeting) {
        this.deeplinkIsRetargeting = deeplinkIsRetargeting;
    }

    public String getDeeplinkValue() {
        return deeplinkValue;
    }

    public void setDeeplinkValue(String deeplinkValue) {
        this.deeplinkValue = deeplinkValue;
    }

    public String getDeeplinkAfDP() {
        return deeplinkAfDP;
    }

    public void setDeeplinkAfDP(String deeplinkAfDP) {
        this.deeplinkAfDP = deeplinkAfDP;
    }

    public String getDeeplinkAfIosUrl() {
        return deeplinkAfIosUrl;
    }

    public void setDeeplinkAfIosUrl(String deeplinkAfIosUrl) {
        this.deeplinkAfIosUrl = deeplinkAfIosUrl;
    }

    public String getDeeplinkTtl() {
        return deeplinkTtl;
    }

    public void setDeeplinkTtl(String deeplinkTtl) {
        this.deeplinkTtl = deeplinkTtl;
    }

    public void setServiceUtil(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public boolean isDeferredDeeplink() {
        return this.isDeferredDeeplink;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor DOCMAPPING_CSV_PATH = new PropertyDescriptor
            .Builder()
            .name("DOCMAPPING_CSV_PATH")
            .displayName("DOCMAPPING_CSV_PATH")
            .description("DOCMAPPING_CSV_PATH containing details of doctor")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_GENERATION_API_URL = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_GENERATION_URL")
            .displayName("DEEPLINK_GENERATION_URL")
            .description("provide deep link generation API url")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_GENERATION_API_KEY = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_GENERATION_API_KEY")
            .displayName("DEEPLINK_GENERATION_API_KEY")
            .description("provide deep link generation API Key")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_PID = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_PID_VALUE")
            .displayName("DEEPLINK_PID_VALUE")
            .description("provide DEEPLINK_PID_VALUE")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_C = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_C_VALUE")
            .displayName("DEEPLINK_C_VALUE")
            .description("provide DEEPLINK_C_VALUE")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_IS_RETARGETING = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_IS_RETARGETING")
            .displayName("DEEPLINK_IS_RETARGETING")
            .description("provide DEEPLINK_IS_RETARGETING")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_VALUE = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_VALUE")
            .displayName("DEEPLINK_VALUE")
            .description("provide DEEPLINK_VALUE")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_AF_DP = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_AF_DP")
            .displayName("DEEPLINK_AF_DP")
            .description("provide DEEPLINK_AF_DP")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_AF_IOS_URL = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_AF_IOS_URL")
            .displayName("DEEPLINK_AF_IOS_URL")
            .description("provide DEEPLINK_AF_IOS_URL")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEEPLINK_TTL = new PropertyDescriptor
            .Builder()
            .name("DEEPLINK_TTL")
            .displayName("DEEPLINK_TTL")
            .description("provide DEEPLINK_TTL")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor DEFERRED_DEEPLINK = new PropertyDescriptor
            .Builder()
            .name("DEFERRED_DEEPLINK")
            .displayName("DEFERRED_DEEPLINK")
            .description("provide DEFERRED_DEEPLINK as true of false value.")
            .required(true)
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, DOCMAPPING_CSV_PATH, DEEPLINK_GENERATION_API_URL, DEEPLINK_GENERATION_API_KEY, DEEPLINK_PID, DEEPLINK_C, DEEPLINK_IS_RETARGETING, DEEPLINK_VALUE, DEEPLINK_AF_DP, DEEPLINK_AF_IOS_URL, DEEPLINK_TTL, DEFERRED_DEEPLINK);
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

        mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
        deeplinkGenerationApiUrl = context.getProperty(DEEPLINK_GENERATION_API_URL).getValue();
        deeplinkGenerationApiKey = context.getProperty(DEEPLINK_GENERATION_API_KEY).getValue();
        deeplinkPid = context.getProperty(DEEPLINK_PID).getValue();
        deeplinkC = context.getProperty(DEEPLINK_C).getValue();
        deeplinkIsRetargeting = (context.getProperty(DEEPLINK_IS_RETARGETING).getValue().equals("true"));
        deeplinkValue = context.getProperty(DEEPLINK_VALUE).getValue();
        deeplinkAfDP = context.getProperty(DEEPLINK_AF_DP).getValue();
        deeplinkAfIosUrl = context.getProperty(DEEPLINK_AF_IOS_URL).getValue();
        deeplinkTtl = context.getProperty(DEEPLINK_TTL).getValue();
        isDeferredDeeplink = (context.getProperty(DEFERRED_DEEPLINK).getValue().equals("true"));
        logConfigValues(logMetaData);

        if (context.getProperty(DOCMAPPING_CSV_PATH).getValue() != null)
            docMappingCSVPath = context.getProperty(DOCMAPPING_CSV_PATH).getValue();
        loadDoctorMapping(this.getLogUtil(), logMetaData);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        DaxAPIResponse daxAPIResponse = new DaxAPIResponse();
        String deeplink = "", customerName = "", prescribedBy = "", mobileNumber = "";

        try {
            logMetaData.put("processor_name", this.getProcessorName());

            if (inputFF == null)
                throw new Exception("Exception occurred while processing");

            String ffContent = readFlowFileContent(inputFF, session);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "flowFile_content:\n" + ffContent, Level.INFO, null);
            PharmacyPrescriptionDetails pharmacyPresDetails = this.getGson().fromJson(ffContent, PharmacyPrescriptionDetails.class);

            if (pharmacyPresDetails == null)
                throw new Exception("Invalid content received");

            MongoClient mongoClient = this.getMongoClientService().getMongoClient();
            this.setMongoClient(mongoClient);
            this.setSiteMasterMap(SiteMaster.loadSiteMasterMap(client, logMetaData, this.getLogUtil()));
            initializeDaos();

            mobileNumber = getMobileNumber(pharmacyPresDetails);
            if (mobileNumber == null)
                throw new Exception("Invalid mobile number");
            logMetaData.put("mobileNumber", mobileNumber);

            Date prescriptionDate = getPrescriptionDate(pharmacyPresDetails);
            if (prescriptionDate == null)
                throw new Exception("Not a valid orderDate");
            logMetaData.put("prescriptionDate", prescriptionDate);

            prescribedBy = getPrescribedBy(pharmacyPresDetails);
            if (prescribedBy.isEmpty())
                throw new Exception("Not a valid DoctorName");
            logMetaData.put("prescribedBy", prescribedBy);

            DBCarePersona dbCarePersona = getCarePersona(mobileNumber, prescriptionDate, prescribedBy);
            boolean isNewCarePersona = false;
            if (dbCarePersona == null) {
                dbCarePersona = new DBCarePersona();
                dbCarePersona.setCreatedDateTime(new Date());
                isNewCarePersona = true;
            }
            DBPrescription dbPrescription = new DBPrescription();
            if (!isNewCarePersona)
                dbPrescription = dbCarePersona.getPrescription();

            updatePrescriptionDetails(dbPrescription, pharmacyPresDetails);
            updatePrescriptionMedicineDetails(dbPrescription, pharmacyPresDetails);
            this.getPrescriptionDao().save(dbPrescription);
            updateCarePersonaInfo(dbCarePersona, dbPrescription, pharmacyPresDetails);
            this.getCarePersonaDao().save(dbCarePersona);
            deeplink = generateDeepLink(pharmacyPresDetails, dbCarePersona.getId().toString(), logMetaData);
            customerName = pharmacyPresDetails.getCustomerDetails().getFirstName();
        } catch (Exception ex) {
            daxAPIResponse.setStatus("FAILURE");
            daxAPIResponse.setErrorMsg(ex.getMessage());
            daxAPIResponse.setErrorCode(101);
            inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(daxAPIResponse));
            session.putAttribute(inputFF, "exception", ex.getMessage());
            session.transfer(inputFF, REL_FAILURE);
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(ex), Level.INFO, null);
            return;
        }

        daxAPIResponse.setStatus("SUCCESS");
        daxAPIResponse.setErrorMsg("Data processed successfully.");
        daxAPIResponse.setErrorCode(0);
        session.putAttribute(inputFF, "carepersona.deeplink", deeplink);
        session.putAttribute(inputFF, "carepersona.customer_name", customerName);
        session.putAttribute(inputFF, "carepersona.doctor_name", prescribedBy);
        session.putAttribute(inputFF, "carepersona.mobile_number", mobileNumber);
        inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(daxAPIResponse));
        session.transfer(inputFF, REL_SUCCESS);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private void updateCarePersonaInfo(DBCarePersona dbCarePersona, DBPrescription
            dbPrescription, PharmacyPrescriptionDetails pharmacyPresDetails) throws ParseException {
        dbCarePersona.setDateOfPrescription(getPrescriptionDate(pharmacyPresDetails));
        dbCarePersona.setPrescribedBy(getPrescribedBy(pharmacyPresDetails));
        dbCarePersona.setPrescribedById(getDocId(pharmacyPresDetails));
        dbCarePersona.setMobileNumber(getMobileNumber(pharmacyPresDetails));
        dbCarePersona.setOrderId(pharmacyPresDetails.getOrderId());
        dbCarePersona.setShopId(pharmacyPresDetails.getShopId());
        if (pharmacyPresDetails.getCustomerDetails() != null)
            dbCarePersona.setCustomerDetails(getCustomerDetails(pharmacyPresDetails));
        dbCarePersona.setPrescription(dbPrescription);
    }

    private void updatePrescriptionDetails(DBPrescription dbPrescription, PharmacyPrescriptionDetails
            pharmacyPresDetails) throws ParseException {
        dbPrescription.setDateOfPrescription(getPrescriptionDate(pharmacyPresDetails));
        dbPrescription.setPrescribedBy(getPrescribedBy(pharmacyPresDetails));
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    private void initializeDaos() {
        this.setIdentityDao(new IdentityDao(this.getMongoClient()));
        this.setUserDao(new UserDao(this.getMongoClient()));
        this.setIdentityDao(new IdentityDao(this.getMongoClient()));
        this.setCarePersonaDao(new CarePersonaDao(this.getMongoClient()));
        this.setDaxDao(new DaxDao(this.getMongoClient()));
        this.setPrescriptionDao(new PrescriptionDao(this.getMongoClient()));
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

        return null;
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

    private FlowFile updateFlowFile(ProcessSession session, FlowFile ff, String data) {
        ff = session.write(ff, out -> out.write(data.getBytes(charset)));
        return ff;
    }

    private String getMobileNumber(PharmacyPrescriptionDetails pharmacyPrescriptionDetails) {
        if (pharmacyPrescriptionDetails.getCustomerDetails() != null) {
            CustomerDetails customerDetails = pharmacyPrescriptionDetails.getCustomerDetails();
            if (customerDetails.getMobileNo() != null && !customerDetails.getMobileNo().isEmpty())
                return customerDetails.getMobileNo();
        }
        return null;
    }

    private Date getPrescriptionDate(PharmacyPrescriptionDetails pharmacyPrescriptionDetails) throws ParseException {
        SimpleDateFormat orderDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (pharmacyPrescriptionDetails.getOrderDate() != null && !pharmacyPrescriptionDetails.getOrderDate().isEmpty())
            return orderDateFormat.parse(pharmacyPrescriptionDetails.getOrderDate());

        return null;
    }

    private String getPrescribedBy(PharmacyPrescriptionDetails pharmacyPrescriptionDetails) {
        if (pharmacyPrescriptionDetails.getDoctorName() != null && !pharmacyPrescriptionDetails.getDoctorName().isEmpty())
            return pharmacyPrescriptionDetails.getDoctorName();
        return "";
    }

    private DBCarePersona getCarePersona(String mobileNumber, Date prescriptionDate, String prescribedBy) {
        List<DBCarePersona> dbCarePersonaList = this.getCarePersonaDao().findCarePersonaPrescription(mobileNumber, prescribedBy);
        if (dbCarePersonaList != null && !dbCarePersonaList.isEmpty())
            for (DBCarePersona dbCarePersona : dbCarePersonaList) {
                if (dbCarePersona.getDateOfPrescription().equals(prescriptionDate))
                    return dbCarePersona;
            }
        return null;
    }

    private void updatePrescriptionMedicineDetails(DBPrescription dbPrescription, PharmacyPrescriptionDetails
            pharmacyPresc) {
        List<MedicineDetails> medicineDetailsList = pharmacyPresc.getItemDetails();
        if (medicineDetailsList != null && !medicineDetailsList.isEmpty()) {
            for (MedicineDetails medicineDetails : medicineDetailsList) {
                DBMedicinePrescription dbMedicinePrescriptionOrg = null;
                if (dbPrescription.getMedicinePrescriptions() != null) {
                    boolean found = false;
                    for (DBMedicinePrescription dbMedPres : dbPrescription.getMedicinePrescriptions()) {
                        if (dbMedPres.getId().equals(medicineDetails.getItemID())) {
                            dbMedicinePrescriptionOrg = dbMedPres;
                            found = true;
                            break;
                        }
                    }
                    if (found)
                        dbPrescription.getMedicinePrescriptions().remove(dbMedicinePrescriptionOrg);
                }
                if (dbMedicinePrescriptionOrg == null) {
                    dbMedicinePrescriptionOrg = new DBMedicinePrescription();
                    dbMedicinePrescriptionOrg.setId(medicineDetails.getItemID());
                }
                dbMedicinePrescriptionOrg.setMedicineName(medicineDetails.getItemName());
                dbMedicinePrescriptionOrg.setMou(medicineDetails.getMou());
                dbMedicinePrescriptionOrg.setPack(medicineDetails.getPack());
                dbMedicinePrescriptionOrg.setPrice(medicineDetails.getPrice());
                dbMedicinePrescriptionOrg.setQty(medicineDetails.getQty());

                dbPrescription.getMedicinePrescriptions().add(dbMedicinePrescriptionOrg);
            }
        }
    }

    private DBCustomerDetails getCustomerDetails(PharmacyPrescriptionDetails pharmacyPresc) {
        DBCustomerDetails dbCustomerDetails = new DBCustomerDetails();
        dbCustomerDetails.setCommAddr(pharmacyPresc.getCustomerDetails().getComm_addr());
        dbCustomerDetails.setDelAddr(pharmacyPresc.getCustomerDetails().getDel_addr());
        dbCustomerDetails.setFirstName(pharmacyPresc.getCustomerDetails().getFirstName());
        dbCustomerDetails.setLastName(pharmacyPresc.getCustomerDetails().getLastName());
        dbCustomerDetails.setCity(pharmacyPresc.getCustomerDetails().getCity());
        dbCustomerDetails.setPostCode(pharmacyPresc.getCustomerDetails().getPostCode());
        dbCustomerDetails.setMailId(pharmacyPresc.getCustomerDetails().getMailId());
        dbCustomerDetails.setAge(pharmacyPresc.getCustomerDetails().getAge());
        dbCustomerDetails.setCardNo(pharmacyPresc.getCustomerDetails().getCardNo());
        dbCustomerDetails.setPatientName(pharmacyPresc.getCustomerDetails().getPatientName());
        dbCustomerDetails.setLatitude(pharmacyPresc.getCustomerDetails().getLatitude());
        dbCustomerDetails.setLongitude(pharmacyPresc.getCustomerDetails().getLongitude());

        return dbCustomerDetails;
    }

    private void loadDoctorMapping(LogUtil logUtil, Map<String, Object> logMetaData) {
        Map<String, ArrayList<String>> docMap = new HashMap<>();
        if (this.getDocMappingCSVPath() != null && !this.getDocMappingCSVPath().isEmpty())
            docMap = Utility.getDoctorMap(this.getDocMappingCSVPath(), logUtil, logMetaData);

        if (!docMap.isEmpty()) {
            this.setDocMap(docMap);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Doc map is loaded....", Level.INFO, null);
        } else
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Doc map is empty.", Level.INFO, null);
    }

    private String getDocId(PharmacyPrescriptionDetails pharmacyPrescriptionDetails) {
        String docName = getPrescribedBy(pharmacyPrescriptionDetails);
        if (this.getDocMap().containsKey(docName) && this.getDocMap().get(docName).size() == 1)
            return this.getDocMap().get(docName).get(0);
        return "";
    }

//    private String generateCarePersonaId(String entityName) {
//        long existingId = 1000000L;
//        String nextId = "";
//        boolean hasNewCarePersonaId = false;
//        while (!hasNewCarePersonaId) {
//            try {
//                DBDax dbDax = this.getDaxDao().findEntityByName(entityName);
//                if (dbDax != null) {
//                    if (dbDax.getUniqueId() != null && !dbDax.getUniqueId().isEmpty())
//                        existingId = Long.parseLong(dbDax.getUniqueId());
//                } else {
//                    dbDax = new DBDax();
//                    dbDax.setEntity_name(entityName);
//                }
//                nextId = String.valueOf(existingId + 1);
//                dbDax.setUniqueId(nextId);
//                this.getDaxDao().save(dbDax);
//                hasNewCarePersonaId = true;
//            } catch (Exception ignored) {
//            }
//        }
//        return nextId;
//    }

    private String generateDeepLink(PharmacyPrescriptionDetails pharmacyPresDetails, String carePersonaId, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        String mobileNo = pharmacyPresDetails.getCustomerDetails().getMobileNo();
        String deeplinkGenerationRequest = getRequestBody(mobileNo, carePersonaId);

        otherDetails.put("mobileNo", mobileNo);
        otherDetails.put("deeplinkGenerationRequest", deeplinkGenerationRequest);

        String deeplink = this.getServiceUtil().generateDeeplink(this.getDeeplinkGenerationApiUrl(), this.getDeeplinkGenerationApiKey(), deeplinkGenerationRequest, logMetaData);
        if (deeplink.isEmpty())
            otherDetails.put("isDeeplinkEmpty", true);
        otherDetails.put("deeplink", deeplink);

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Care persona deeplink generation status: " + (deeplink.isEmpty() ? "Fail" : "Success"), Level.INFO, otherDetails);
        return deeplink;
    }

    private String getRequestBody(String mobileNo, String carePersonaId) {
        DeeplinkRequestData deeplinkRequestData = new DeeplinkRequestData();
        deeplinkRequestData.setPid(this.getDeeplinkPid());
        deeplinkRequestData.setC(this.getDeeplinkC());
        deeplinkRequestData.setIs_retargeting(this.isDeeplinkIsRetargeting());
        deeplinkRequestData.setDeep_link_value(this.getDeeplinkValue());
        deeplinkRequestData.setAf_dp(this.getDeeplinkAfDP());
        deeplinkRequestData.setAf_ios_url(this.getDeeplinkAfIosUrl());
        deeplinkRequestData.setDeferredDeepLink(this.isDeferredDeeplink());
        deeplinkRequestData.setMobileNumber(mobileNo);
        deeplinkRequestData.setCarePersonaId(carePersonaId);

        DeeplinkGenerationRequest deeplinkGenerationRequest = new DeeplinkGenerationRequest();
        deeplinkGenerationRequest.setTtl(this.getDeeplinkTtl());
        deeplinkGenerationRequest.setData(deeplinkRequestData);

        return this.getGson().toJson(deeplinkGenerationRequest);
    }

    private void logConfigValues(Map<String, Object> logMetaData) {
        Map<String, Object> otherValues = new HashMap<>();
        otherValues.put("deeplinkGenerationApiUrl", deeplinkGenerationApiUrl);
        otherValues.put("deeplinkPid", deeplinkPid);
        otherValues.put("deeplinkC", deeplinkC);
        otherValues.put("deeplinkIsRetargeting", deeplinkIsRetargeting);
        otherValues.put("deeplinkValue", deeplinkValue);
        otherValues.put("deeplinkAfDP", deeplinkAfDP);
        otherValues.put("deeplinkAfIosUrl", deeplinkAfIosUrl);
        otherValues.put("deeplinkTtl", deeplinkTtl);
        otherValues.put("isDeferredDeeplink", isDeferredDeeplink);

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Care persona deeplink configurations are logged", Level.INFO, otherValues);
    }
}
