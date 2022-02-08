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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.DBEntity;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.models.WhatsAppInput;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.bson.types.ObjectId;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProcessSms extends AbstractProcessor {
    private static final String processorName = "ProcessSms";
    private static final Charset charset = StandardCharsets.UTF_8;
    Map<String, Object> logMetaData;
    private DateUtil dateUtil = null;
    private SiteDetails siteDetails = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private DBsms dbsms = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private LogUtil logUtil = null;
    private SMSDao smsDao;
    private IdentityDao identityDao = null;
    private EntityDao entityDao = null;
    protected MongoClientService mongoClientService;
    String downloadPdfUrl = "", smsApiUrl = "";

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();

        return this.dateUtil;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
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

    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor DOWNLOAD_PDF_URL = new PropertyDescriptor
            .Builder().name("DOWNLOAD_PDF_URL")
            .displayName("DOWNLOAD_PDF_URL")
            .description("url to download pdf")
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

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, DOWNLOAD_PDF_URL, SMSAPIURL);
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
        downloadPdfUrl = context.getProperty(DOWNLOAD_PDF_URL).evaluateAttributeExpressions().getValue();
        smsApiUrl = context.getProperty(SMSAPIURL).evaluateAttributeExpressions().getValue();
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();
        long startTime = this.getDateUtil().getEpochTimeInSecond();

        if (flowFile == null) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("No flow file received, returning ......"), Level.INFO, null);
            return;
        }

        MongoClient client = this.getMongoClientService().getMongoClient();

        try {
            String ffContent = readFlowFileContent(flowFile, session);
            FFInput ffinput = gson.fromJson(ffContent, FFInput.class);

            this.setSiteMasterMap(SiteMaster.loadSiteMasterMap(client, logMetaData, this.getLogUtil()));
            SiteDetails siteDetails = getSiteDetailsForSiteApiKey(ffinput.siteApiKey);
            this.setSiteDetails(siteDetails);

            logMetaData.put("uhid", ffinput.sms.getUhid());
            logMetaData.put("sms_purpose", ffinput.sms.getSmsPurpose());
            logMetaData.put("mobile_number", ffinput.sms.getMobileNumber());

            dbsms = ffinput.sms;
            smsDao = new SMSDao(client);
            identityDao = new IdentityDao(client);
            entityDao = new EntityDao(client);

            if (dbsms != null) {
                String uhid = dbsms.getUhid();
                String siteId = getSiteKeyFromUhid(uhid);
                String siteName = getSiteNameFromUhid(uhid);
                DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                if (dbUser != null) {
                    dbsms.setDbUser(dbUser);
                } else {
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("Unable to find user..., skipping user object in sms object"), Level.INFO, null);
                }
                sendSMS(dbsms.getSmsPurpose(), smsApiUrl);
            }
            FlowFile output = getFlowFile(session, flowFile, ffContent);
            session.transfer(output, REL_SUCCESS);
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(e), Level.INFO, null);
            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(e));
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
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

        return null;
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

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, out -> out.write(data.getBytes(charset)));
        return outputFF;
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        if (inputFF == null) {
            return null;
        }
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    public void sendSMS(String intimationType, String smsApiUrl) {
        try {
            String content = null;
            String pName = dbsms.getPatientName() != null && !dbsms.getPatientName().equals("TO_BE_UPDATED_FROM_HOSPITAL") ? dbsms.getPatientName() : " ";
            if (intimationType.equals("BILLS-INTIMATION-DOWNLOAD")) {
                if (dbsms.getDownloadId() != null)
                    content = String.format("Namaste %s %s. To download bills :  https://apolloprism.com/#downloadbillpdf/id=%s", pName, dbsms.getUhid(), dbsms.getDownloadId());
            } else if (dbsms.getSmsPurpose().equals("LABTEST-INTIMATION-DOWNLOAD")) {
                content = String.format("Namaste %s %s. To download latest reports :  https://apolloprism.com/#downloadodpdf/id=%s", pName, dbsms.getUhid(), dbsms.getDownloadId());
            } else if (dbsms.getSmsPurpose().equals("LABTEST-INTIMATION-VISIT")) {
                content = " Thanks for visiting Apollo Hospitals!To download latest reports visit: apolloprism.com or download Apollo247 app : https://ap247.onelink.me/JG0h/hv";
            } else if (dbsms.getSmsPurpose().equals("HEALTHCHECK-INTIMATION")) {
                content = "Namaste,Your health check summary has been updated online.To access reports, visit: apolloprism.com or downld Apollo247 app:https://ap247.onelink.me/JG0h/hcs - Apollo";
            } else if (intimationType.equals("OPCONSULTATION-INTIMATION")) {
                String entityName = this.getSiteDetails().getEntityName();
                DBEntity dbEntity = entityDao.getEntity(entityName);
                content = "Dear Patient, Apollo Hospitals has added a prescription to your PHR ! You can view it at Apollo247 app/web at https://apollo247.onelink.me/MGY5/4911bf96 or visit us at apolloprism.com  - Apollo";
                if (dbEntity != null && dbEntity.getSmsPrescription() != null && !dbEntity.getSmsPrescription().isEmpty())
                    content = dbEntity.getSmsPrescription();
            }

            if (content != null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("Sending SMS with content %s", content), Level.INFO, null);
                send(dbsms.getMobileNumber(), content, String.valueOf(dbsms.getId()), dbsms, smsApiUrl);
            }
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
    }

    public void send(String to, String content, String smsId, DBsms dbSMS, String smsApiUrl) throws Exception {
        String sendResult = "No Response";
        dbSMS.setId(new ObjectId());

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
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

        sendResult = sendSmsRequest(formParams, smsApiUrl);
        logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("sendResult " + sendResult), Level.INFO, null);
        dbSMS.setSendResult(sendResult);
        dbSMS.setSendAt(new Date());
        smsDao.save(dbSMS);
        logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("SMS send TO: " + to + " SMSId: " + smsId + " Result is : " + sendResult + " Sent Status: "), Level.INFO, null);

        if (dbSMS.isWhatsapp() && !dbSMS.getSmsPurpose().equals("OTP")) {
            String pName = dbSMS.getPatientName() != null ? dbSMS.getPatientName() : " ";
            sendResult = sendWhatsAppMsg(to, pName, dbSMS.getUhid(), "https://apolloprism.com/#downloadpdf/id=" + dbSMS.getDownloadId(), "https://api.adohm.com/public/whatsapp/apollo/labreports", null);
            dbSMS.setWhatsAppSendResult(sendResult);
            smsDao.save(dbSMS);
        }
    }

    public String sendSmsRequest(List<NameValuePair> formParams, String url) {
        String sendResult = "No Response";
        int timeout = 60;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();

        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        try {
            UrlEncodedFormEntity formEntitySMS = new UrlEncodedFormEntity(formParams, "UTF-8");
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(formEntitySMS);

            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, String.format("calling sms api url " + url), Level.INFO, null);
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null)
                sendResult = EntityUtils.toString(respEntity);
        } catch (Exception e) {
            sendResult = e.getMessage();
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
        } catch (Exception e) {
            sendResult = e.getMessage();
        }
        return sendResult;
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
    }
}