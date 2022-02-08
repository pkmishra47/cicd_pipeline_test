package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.DBEntity;
import org.apache.nifi.processors.daxoperation.dbo.DBSugarInfo;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.models.WhatsAppInput;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.GsonUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.apache.nifi.processors.daxoperation.utils.Utility;
import org.apache.nifi.stream.io.StreamUtils;
import org.bson.types.ObjectId;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SendSMSJob extends AbstractProcessor {

    private static final String processorName = "SendSMSJob";
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final List<String> possibleType = Arrays.asList("INVITATION", "INVITATION-TWO",
            "LABTEST-INTIMATION", "BILLS-INTIMATION", "HEALTHCHECK-INTIMATION", "PRESCRIPTION-INTIMATION",
            "DISCHARGE-INTIMATION", "HEALTHCHECK-DELAY", "SUGARINVITATION");
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    protected MongoClientService mongoClientService;
    private static Gson gson = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    Map<String, Object> logMetaData;
    private MongoClient client;
    private SMSDao smsDao;
    private IdentityDao identityDao = null;
    private SiteMasterDao siteMasterDao = null;
    private EntityDao entityDao = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private static Map<String, SiteMaster> siteMap = new HashMap<String, SiteMaster>();
    private static Map<String, DBEntity> entitys = new HashMap<String, DBEntity>();

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public Gson getGson() {
        if (this.gson == null)
            this.gson = new Gson();
        return this.gson;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();

        return this.dateUtil;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();
    public static final PropertyDescriptor BILL_SMS_URL = new PropertyDescriptor
            .Builder().name("BILL_SMS_URL")
            .displayName("BILL_SMS_URL")
            .description("provide apollo247 webhook url to push new uhid info")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    public static final PropertyDescriptor SMSAPIURL = new PropertyDescriptor
            .Builder().name("SMSAPIURL")
            .displayName("SMSAPIURL")
            .description("")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    private static final PropertyDescriptor SMSTYPE = new PropertyDescriptor
            .Builder().name("SMSTYPE").displayName("SMSTYPE")
            .description("SMS TYPE")
            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();
    private static final PropertyDescriptor LISTTOSKIPNAME = new PropertyDescriptor
            .Builder().name("LISTTOSKIPNAME").displayName("LISTTOSKIPNAME").
            description("LIST TO SKIP NAME")
            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();
    private static final PropertyDescriptor NUMBEROFRECORDS = new PropertyDescriptor
            .Builder().name("NumberOfRecords").displayName("NumberOfRecords")
            .description("NumberOfRecords")
            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();


    @Override
    protected void init(final ProcessorInitializationContext context) {
        List<PropertyDescriptor> initDescriptors = new ArrayList<>();
        initDescriptors.add(MONGODB_CLIENT_SERVICE);
        initDescriptors.add(BILL_SMS_URL);
        initDescriptors.add(SMSAPIURL);
        initDescriptors.add(LISTTOSKIPNAME);
        initDescriptors.add(SMSTYPE);
        initDescriptors.add(NUMBEROFRECORDS);
        this.descriptors = Collections.unmodifiableList(initDescriptors);

        Set<Relationship> initRelationships = new HashSet<>();
        initRelationships.add(REL_SUCCESS);
        initRelationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(initRelationships);
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

    }


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.create();

        String smsUrl = context.getProperty(BILL_SMS_URL).evaluateAttributeExpressions().getValue();
        String smsApiUrl = context.getProperty(SMSAPIURL).evaluateAttributeExpressions().getValue();
        String smsType = context.getProperty(SMSTYPE).evaluateAttributeExpressions().getValue();
        Integer numOfRecord = Integer.parseInt(context.getProperty(NUMBEROFRECORDS).evaluateAttributeExpressions().getValue());

        if (!possibleType.contains(smsType)) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("INVALID SMS TYPE, returning ......"), Level.INFO, null);
            return;
        }
        try {
//            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "smsUrl: \n" + smsUrl + " \n,smsApiUrl: \n" + smsApiUrl, Level.INFO, null);
            this.mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
            client = this.mongoClientService.getMongoClient();
            siteMasterMap = SiteMaster.loadSiteMasterMap(client, logMetaData, this.getLogUtil());

            Calendar currentDay = Calendar.getInstance();
            currentDay.setTime(new Date());
            currentDay.set(Calendar.HOUR_OF_DAY, 0);
            currentDay.set(Calendar.MINUTE, 0);
            currentDay.set(Calendar.SECOND, 0);
            currentDay.set(Calendar.MILLISECOND, 0);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("PHR | DataMan | SMS Execute |  CurrentDate " + currentDay.getTime()), Level.INFO, null);
            currentDay.add(Calendar.DAY_OF_MONTH, -2);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("PHR | DataMan | SMS Execute |  2DayBeforeDate " + currentDay.getTime()), Level.INFO, null);
            this.smsDao = new SMSDao(this.client);
            this.identityDao = new IdentityDao(client);
            this.siteMasterDao = new SiteMasterDao(client);
            this.entityDao = new EntityDao(client);

            this.siteMasterDao.loadAll();
            List<DBsms> smsList = this.smsDao.findDBSMSToIntimateByDateRange(currentDay.getTimeInMillis(), smsType, numOfRecord);

            if (smsList == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("SMS List is null,  returning ......"), Level.ERROR, null);
                return;
            }

            logMetaData.put("smsType", smsType);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("PHR | DataMan | SMS Execute |  %s  | Size %d", smsType, smsList.size()), Level.INFO, null);

            for (DBsms dbsms : smsList) {
                if (dbsms.getDbUser() == null) {
                    String uhid = dbsms.getUhid();
                    String siteId = getSiteKeyFromUhid(uhid);
                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                    if (dbUser != null) {
                        dbsms.setDbUser(dbUser);
                    } else {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                String.format("Unable to find user of uhid %s and siteId %s..., skipping user object " +
                                        "in sms object", uhid, siteId), Level.INFO, null);
                        continue;
                    }
                }
                sendSMS(dbsms.getSmsPurpose(), smsUrl, smsApiUrl, dbsms);
            }
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("SMS Job completed for %d", smsList.size()), Level.INFO, null);
            FlowFile output = getFlowFile(session, flowFile, "");
            session.transfer(output, REL_SUCCESS);
        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.ERROR, null);
            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    public void sendSMS(String intimationType, String smsUrl, String smsApiUrl, DBsms dbSMS) throws Exception {
        if (!siteMap.containsKey(dbSMS.getSiteKey())) {
            SiteMaster dbSite = siteMasterDao.findDBSiteBySiteKey(dbSMS.getSiteKey());
            if (dbSite == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        "Could not find site master for site key: " + dbSMS.getSiteKey(), Level.INFO, null);
                return;
            }
            siteMap.put(dbSite.getSiteKey(), dbSite);
        }
        SiteMaster dbSite = siteMap.get(dbSMS.getSiteKey());


        String entityName = dbSMS.getEntityName();
        if (!entitys.containsKey(entityName)) {
            DBEntity dbEntity = entityDao.getEntity(entityName);
            if (dbEntity == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("Unable to find the Entity for entity name: %s", entityName), Level.INFO, null);
            }
            entitys.put(entityName, dbEntity);
        }

        entityName = dbSite.getEntityName();
        if (!entitys.containsKey(entityName)) {
            DBEntity dbEntity = entityDao.getEntity(entityName);
            if (dbEntity == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("Unable to find the Entity for entity name: %s", entityName), Level.INFO, null);
            }
            entitys.put(entityName, dbEntity);
        }
        DBEntity dbEntity = entitys.get(entityName);

        String content = "";
        if (intimationType.equals("INVITATION")) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation  |  smsId %s", String.valueOf(dbSMS.getId())), Level.INFO, null);
            content = dbEntity.getSmsInvitation();
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation  | CONTENT IS %s",
                            content), Level.INFO, null);
            if (content.indexOf("%s") != -1) {
                content = String.format(content, dbSite.getSiteName());
            }
        }
        if (intimationType.equals("INVITATION-TWO")) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation-Two  |  smsId %s", String.valueOf(dbSMS.getId())),
                    Level.INFO, null);
            DBEntity nDE = entitys.get(dbSMS.getEntityName());
            content = nDE.getSmsInvitationTwo();
            if (null == content) {
                content = dbEntity.getSmsInvitationTwo();
            }
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation-Two  |  content %s", content),
                    Level.INFO, null);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation-Two  |  Mobile Number %s", dbSMS.getMobileNumber()),
                    Level.INFO, null);

            if (content.indexOf("%s") != -1) {
                content = String.format(content, dbSite.getSiteName());
            }

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation-Two  |  content %s", content),
                    Level.INFO, null);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation-Two  |  smsId {}" + String.valueOf(dbSMS.getId())),
                    Level.INFO, null);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("PHR | DataMan | sendSMS |  intimation2 | Invitation-Two  |  Mobile Number {}" + dbSMS.getMobileNumber()),
                    Level.INFO, null);
        }
        if (intimationType.equals("LABTEST-INTIMATION")) {
            if (dbSMS.getDownloadId() != null) {
                content = "Namaste " + dbSMS.getPatientName() + " " + dbSMS.getUhid() + " To download latest reports : " + smsUrl + dbSMS.getDownloadId();
            }

        }
        if (intimationType.equals("BILLS-INTIMATION")) {
            if (dbSMS.getDownloadId() != null) {
                content = "Namaste " + dbSMS.getPatientName() + " " + dbSMS.getUhid() + " To download bills : " + smsUrl + dbSMS.getDownloadId() + " or download Apollo247app:https://ap247.onelink.me/JG0h/bill";
            }

        }
        if (intimationType.equals("HEALTHCHECK-INTIMATION"))
            content = dbEntity.getSmsHealthCheck();
        if (intimationType.equals("PRESCRIPTION-INTIMATION"))
            content = dbEntity.getSmsPrescription();
        if (intimationType.equals("DISCHARGE-INTIMATION"))
            content = dbEntity.getSmsDischarge();
        if (intimationType.equals("HEALTHCHECK-DELAY"))
            content = dbEntity.getSmsHealthcheckdelay();
        if (intimationType.equals("SUGARINVITATION")) {
            entityName = "Sugar";
            if (!entitys.containsKey(entityName)) {
                dbEntity = entityDao.getEntity(entityName);
                if (dbEntity == null) {
//                    log.info("Unable to find the Entity for entity name: {}", entityName);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                            String.format("Unable to find the Entity for entity name: %s", entityName),
                            Level.INFO, null);
//                    continue;
                    return;
                }
                entitys.put(entityName, dbEntity);
            }

            dbEntity = entitys.get(entityName);
            content = dbEntity.getSmsInvitation();

            String packageName = "";
            List<DBSugarInfo> sugarInfoList = dbSMS.getDbUser().getSugarInfo();
            if (sugarInfoList != null && sugarInfoList.size() != 0) {
                DBSugarInfo last = sugarInfoList.get(0);
                if (last != null && last.getBillingDate() != null) {
                    for (DBSugarInfo temp : sugarInfoList) {
                        if (temp != null && temp.getBillingDate() != null && last.getBillingDate().before(temp.getBillingDate()))
                            last = temp;
                    }
                }
                if (last != null) {
                    packageName = last.getPackageName();
                }
//                log.info("The package name sent for the user is : {}", packageName);
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("The package name sent for the user is : %s", packageName),
                        Level.INFO, null);
                // packageName = dbSMS.getDbUser().getSugarInfo().get(dbSMS.getDbUser().getSugarInfo().size()-1).getPackageName();
            }
            if (content.indexOf("%s") != -1) {
                content = String.format(content, packageName);
            }
        }
        if (null != content) {
            if (intimationType.equals("LabTest")) {
//                smsService.sendIntimation(dbSMS.getMobileNumber(), String.valueOf(dbSMS.getId()), content);
                send(dbSMS.getMobileNumber(), content, String.valueOf(dbSMS.getId()), dbSMS, smsApiUrl);
                String contentTwo = dbEntity.getSmsLabtest();
//                smsService.sendIntimation(dbSMS.getMobileNumber(), String.valueOf(dbSMS.getId()), contentTwo);
                send(dbSMS.getMobileNumber(), content, String.valueOf(dbSMS.getId()), dbSMS, smsApiUrl);
            } else {
//                smsService.sendIntimation(dbSMS.getMobileNumber(), String.valueOf(dbSMS.getId()), content);
                send(dbSMS.getMobileNumber(), content, String.valueOf(dbSMS.getId()), dbSMS, smsApiUrl);
            }
        }

    }

    public void send(String to, String content, String smsId, DBsms dbSMS, String smsApiUrl) throws Exception {
        String sendResult = "No Response";
        Gson gson = new Gson();
//        dbSMS.setId(new ObjectId());
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        if (dbSMS.getSmsPurpose().equals("OTP") || dbSMS.getSmsPurpose().equals("ACTIVATION")) {
            formParams.add(new BasicNameValuePair("enterpriseid", "apollotp"));
            formParams.add(new BasicNameValuePair("subEnterpriseid", "apollotp"));
            formParams.add(new BasicNameValuePair("pusheid", "apollotp"));
            formParams.add(new BasicNameValuePair("pushepwd", "apollotp24"));
            formParams.add(new BasicNameValuePair("msisdn", to));
            //	formParams.add(new BasicNameValuePair("sender", "ASKOTP"));
            formParams.add(new BasicNameValuePair("sender", "APOLLO"));
            formParams.add(new BasicNameValuePair("msgtext", content));
//                HttpPost smsHttpPostsms = new HttpPost("https://otp2.maccesssmspush.com/OTP_ACL_Web/OtpRequestListener");
//                HttpPost smsHttpPostsms = new HttpPost("https://otp2.aclgateway.com/OTP_ACL_Web/OtpRequestListener");
            sendResult = sendSms(formParams, "https://otp2.aclgateway.com/OTP_ACL_Web/OtpRequestListener");

            dbSMS.setSendResult(sendResult);
            dbSMS.setSendAt(new Date());
            smsDao.save(dbSMS);
//                log.info("SMS send TO: " + to + " SMSId: " + smsId + " Result is : " + sendResult + " Sent Status: " );
            if (dbSMS.isWhatsapp()) {
                sendResult = sendWhatsAppMsg(to, null, null, null, "https://api.adohm.com/public/whatsapp/apollo/labreports/otp", dbSMS.getAccessCode());

                dbSMS.setWhatsAppSendResult(sendResult);
                smsDao.save(dbSMS);
//                    log.info("WhatsApp OTP send TO: " + to + " SMSId: " + smsId + " Result is : " + sendResult + " Sent Status: " + statusCode);
            }

        } else {
            formParams.add(new BasicNameValuePair("userId", "apolloalrt"));
            formParams.add(new BasicNameValuePair("pass", "apolloalrt30"));
            formParams.add(new BasicNameValuePair("appid", "apolloalrt"));
            formParams.add(new BasicNameValuePair("subappid", "apolloalrt"));
            formParams.add(new BasicNameValuePair("contenttype", "1"));
            formParams.add(new BasicNameValuePair("to", to));
            formParams.add(new BasicNameValuePair("from", "APOLLO"));
            formParams.add(new BasicNameValuePair("text", content));
            formParams.add(new BasicNameValuePair("selfid", "true"));
            formParams.add(new BasicNameValuePair("alert", "1"));
            formParams.add(new BasicNameValuePair("dlrreq", "true"));
            formParams.add(new BasicNameValuePair("intflag", "false"));

            sendResult = sendSms(formParams, smsApiUrl);
            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("sendResult " + sendResult),
                    Level.INFO, null);
            dbSMS.setSendResult(sendResult);
            dbSMS.setSendAt(new Date());
            smsDao.save(dbSMS);
            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("SMS send TO: " + to + " SMSId: " + smsId + " Result is : " +
                            sendResult + " Sent Status: "), Level.INFO, null);
        }

        if (dbSMS.isWhatsapp() && !dbSMS.getSmsPurpose().equals("OTP")) {
            String pName = dbSMS.getPatientName() != null ? dbSMS.getPatientName() : " ";
            sendResult = sendWhatsAppMsg(to, pName, dbSMS.getUhid(),
                    "https://apolloprism.com/#downloadpdf/id=" + dbSMS.getDownloadId(), "https://api.adohm.com/public/whatsapp/apollo/labreports", null);
            dbSMS.setWhatsAppSendResult(sendResult);
            smsDao.save(dbSMS);
        }

    }

    public String sendSms(List<NameValuePair> formParams, String url) throws Exception {
        String sendResult = "No Response";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            UrlEncodedFormEntity formEntitySMS = new UrlEncodedFormEntity(formParams, "UTF-8");
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntitySMS);

            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("calling sms api url " + url),
                    Level.INFO, null);
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null)
                sendResult = EntityUtils.toString(respEntity);

        }
        return sendResult;
    }

    public String sendWhatsAppMsg(String mobileNumber, String patientName, String uhid, String url, String whataAppUrl, String otp) throws Exception {
        String sendResult = "no response";
        Gson gson = new Gson();
        int timeout = 60;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        try {
            HttpPost httpPost = new HttpPost(whataAppUrl);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.addHeader("publicauthtoken", "40f90a0f20a4b8a31ec6e44a97e7f641774d38ed6919cd72fdaf24c1c8758ef1b4f39d7ce6a2b385");

            WhatsAppInput wInput = new WhatsAppInput();
            wInput.phone = mobileNumber;

            if (otp != null) {
                wInput.otp = otp;
            } else {
                wInput.params = new ArrayList<>();
                wInput.params.add(patientName);
                wInput.params.add(uhid);
                wInput.params.add(url);
            }

            StringEntity entity = new StringEntity(gson.toJson(wInput));
            httpPost.setEntity(entity);

            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("calling whatsapp api url " + whataAppUrl), Level.INFO, null);
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null)
                sendResult = EntityUtils.toString(respEntity);

            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("whatsap sendResult " + sendResult), Level.INFO, null);
        } catch (Exception ex) {
            sendResult = ex.getMessage();
            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
        }
        return sendResult;
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
        } else {
            // 5 oct 2021 only bislas pur site does not have uhid preffix
            return "7a1ea09de84560c892b8b17b4e3cfa7dff538797";
        }
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
}
