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
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.ConsultationDao;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.DBAttachement;
import org.apache.nifi.processors.daxoperation.dbo.DBConsultation;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.dm.DaxAPIResponse;
import org.apache.nifi.processors.daxoperation.dm.OPConsultation;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.mongodb.morphia.query.Query;
import org.slf4j.event.Level;
import org.apache.nifi.processors.daxoperation.dm.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProcessOPConsultation extends AbstractProcessor {
    private static final String processorName = "ProcessOPConsultation";
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, Object> logMetaData = new HashMap<>();
    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private String azureConnStr = "";
    private String azureFileShare = "";
    private String azureBaseDirName = "";
    protected MongoClientService mongoClientService;
    private MongoClient client = null;
    private DBUtil dbUtil = null;
    private boolean hasSendSMS = false;

    private IdentityDao identityDao = null;
    private ConsultationDao consultationDao = null;
    private UserDao userDao = null;

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

    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship")
            .build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship")
            .build();
    static final Relationship REL_SMS_TRIGGER = new Relationship.Builder().name("sms_trigger")
            .description("On success processing, sms details as flowfile content will be routed to 'sms_trigger' relationship")
            .build();
    static final Relationship REL_PRISM_API_TRIGGER = new Relationship.Builder().name("prism_api_trigger")
            .description("On success processing, prism api call flow will be triggered by sending details through flowfile in 'prism_api_trigger' relationship")
            .build();

    private static final Charset charset = StandardCharsets.UTF_8;

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

    public MongoClient getMongoClient() {
        return this.client;
    }

    public void setMongoClient(MongoClient client) {
        this.client = client;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public SiteDetails getSiteDetails() {
        return this.siteDetails;
    }

    public void setSiteDetails(SiteDetails siteDetails) {
        this.siteDetails = siteDetails;
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, AZURE_CONNECTION_STR, AZURE_FILE_SHARE, AZURE_BASE_DIRECTORY_NAME, SEND_SMS);
        this.relationships = Set.of(REL_SUCCESS, REL_FAILURE, REL_SMS_TRIGGER, REL_PRISM_API_TRIGGER);
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
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        logMetaData.put("processor_name", processorName);
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FlowFile outputSMSFF = null;
        FlowFile outputResponseFF = null;
        DaxAPIResponse daxAPIResponse = new DaxAPIResponse();
        String docType = "";

        try {
            MongoClient mongoClient = this.getMongoClientService().getMongoClient();
            this.setMongoClient(mongoClient);

            String ffContent = Utility.readFlowFileContent(inputFF, session);
            OPConsultation opc = gson.fromJson(ffContent, OPConsultation.class);
            logMetaData.put("uhid", opc.UHID);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Processing consultation with encounterId " + opc.EncounterId, Level.INFO, null);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Body: \n" + gson.toJson(opc), Level.INFO, null);

            identityDao = new IdentityDao(client);
            consultationDao = new ConsultationDao(client);
            userDao = new UserDao(client);

            this.siteMasterMap = SiteMaster.loadSiteMasterMap(client, logMetaData, logUtil);
            String fileName = opc.UHID + "_" + opc.DocumentType + "_" + opc.EncounterId + "_" + opc.EventTime + ".pdf";
            String uhid = opc.UHID;
            String siteId = getSiteKeyFromUhid(uhid);
            docType = opc.DocumentType;

            DBUser dbUser = identityDao.findByUhid(uhid, siteId);

            if (dbUser != null) {
                DBConsultation dbConsultation = findConsultation(dbUser, opc.DoctorName, opc.DocumentType, opc.EventTime);
                boolean isNewDoc = false;
                if (dbConsultation == null) {
                    isNewDoc = true;
                    dbConsultation = new DBConsultation();
                } else
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Already exists consultation with id " + dbConsultation.getId().toString(), Level.INFO, null);

                dbConsultation.setDocumentType(opc.DocumentType);
                dbConsultation.setUhid(opc.UHID);
                dbConsultation.setMobileNumber(opc.MobileNumber);

                Date consultedDateTime = new Date(opc.EventTime);
                dbConsultation.setConsultedtime(consultedDateTime);
                dbConsultation.setDoctor_name(opc.DoctorName);
                dbConsultation.setDepartment(opc.Department);
                dbConsultation.setEncounterId(opc.EncounterId);
                dbConsultation.setAppointmentid(opc.EncounterId);

                if (opc.Content != null) {
                    Attachment attach = new Attachment();
                    attach.setContent(Base64.decodeBase64(opc.Content));
                    attach.setFileName(fileName);

                    this.setDbUtil(new DBUtil(this.getMongoClient(), this.getAzureConnStr(), this.getAzureFileShare(), this.getAzureBaseDirName(), logMetaData));
                    if (!isNewDoc)
                        removeExistingAttachment(opc.UHID, dbConsultation.getAttachement().get(0));

                    DBAttachement dbAttach = this.getDbUtil().writeDBAttachment(opc.UHID, attach);
                    List<DBAttachement> dbAttachmentList = new ArrayList<>();
                    dbAttachmentList.add(dbAttach);
                    dbConsultation.setAttachement(dbAttachmentList);
                }

                consultationDao.save(dbConsultation);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Consultation saved for encounterId %s", opc.EncounterId), Level.INFO, null);
                dbUser.getConsultations().add(dbConsultation);
                userDao.save(dbUser);

                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "User Dao Saved with consultation", Level.INFO, null);

                if (hasSendSMS && this.getSiteDetails().isSms() && docType.equals("Op prescription")) {
                    String strOpConsultationSmsObjFFContent = getOpConsultationSmsObjFFContent(dbUser, uhid, siteId);
                    outputSMSFF = getFlowFile(session, inputFF, strOpConsultationSmsObjFFContent);
                    session.transfer(outputSMSFF, REL_SMS_TRIGGER);
                }

                String strPrismRequestDetails = getDetailsForPrismAPIRequest(dbConsultation.getId().toString());
                FlowFile prismAPITriggerFlowFile = getFlowFile(session, inputFF, strPrismRequestDetails);
                session.transfer(prismAPITriggerFlowFile, REL_PRISM_API_TRIGGER);

                String strDaxAPIResponse = getSuccessResponseObj();
                outputResponseFF = getFlowFile(session, inputFF, strDaxAPIResponse);
                session.transfer(outputResponseFF, REL_SUCCESS);
            } else
                throw new Exception("Unable to find uhid " + opc.UHID);
        } catch (Exception ex) {
            String strDaxAPIResponse = getFailResponseObj(ex.getMessage());
            inputFF = updateFlowFile(session, inputFF, strDaxAPIResponse);
            session.putAttribute(inputFF, "exception", ex.getMessage());
            session.transfer(inputFF, REL_FAILURE);
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(ex), Level.INFO, null);
            return;
        }

        session.remove(inputFF);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private FlowFile updateFlowFile(ProcessSession session, FlowFile ff, String data) {
        ff = session.write(ff, out -> out.write(data.getBytes(charset)));
        return ff;
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, out -> out.write(data.getBytes(charset)));
        return outputFF;
    }

    String getSiteKeyFromUhid(String uhid) {
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
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getUhidPrefix() != null && siteDetails.getUhidPrefix().equals(sitePrefix)) {
                this.setSiteDetails(siteDetails);
                return siteDetails;
            }
        }
        return null;
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

    private String getOpConsultationSmsObjFFContent(DBUser dbUser, String uhid, String siteID) {
        DBsms dbSms = new DBsms();
        dbSms.setSmsPurpose("OPCONSULTATION-INTIMATION");
        dbSms.setMobileNumber(dbUser.getMobileNumber());
        dbSms.setUhid(uhid);
        dbSms.setSiteKey(siteID);
        dbSms.setSendAt(new Date());

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = this.getSiteDetails().getSiteKey();
        ffInput.sms = dbSms;
        return GsonUtil.getGson().toJson(ffInput);
    }

    private String getSuccessResponseObj() {
        DaxAPIResponse daxAPIResponse = new DaxAPIResponse();
        daxAPIResponse.setStatus("SUCCESS");
        daxAPIResponse.setErrorMsg("Data processed successfully.");
        daxAPIResponse.setErrorCode(0);
        return GsonUtil.getGson().toJson(daxAPIResponse);
    }

    private String getFailResponseObj(String errorMsg) {
        DaxAPIResponse daxAPIResponse = new DaxAPIResponse();
        daxAPIResponse.setStatus("FAIL");
        daxAPIResponse.setErrorMsg(errorMsg);
        daxAPIResponse.setErrorCode(101);
        return GsonUtil.getGson().toJson(daxAPIResponse);
    }

    private String getDetailsForPrismAPIRequest(String consultationId) {
        return "{\"consultationId\":" + consultationId + "}";
    }

    private void removeExistingAttachment(String uhid, DBAttachement dbAttachement) {
        if (dbAttachement.getFileAttached() != null)
            this.getDbUtil().deleteFile(dbAttachement.getFileAttached().toString());
        else if (dbAttachement.getAzurePath() != null)
            this.getDbUtil().deleteAzureFile(uhid, dbAttachement.getFileName());
    }

    public DBConsultation findConsultation(DBUser dbUser, String doctorName, String documentType, Long eventTime) {
        List<DBConsultation> dbConsultations = dbUser.getConsultations();
        for (DBConsultation consultation : dbConsultations) {
            Date consultedTime = new Date(eventTime);
            if (consultation.getDoctor_name().equals(doctorName) && consultation.getDocumentType().equals(documentType) && consultation.getConsultedtime().equals(consultedTime))
                return consultation;
        }
        return null;
    }
}