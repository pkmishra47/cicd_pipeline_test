package org.apache.nifi.processors.daxoperation.utils;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.nifi.processors.daxoperation.dao.SMSDao;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SMSService {
    private static Logger log = LoggerFactory.getLogger(SMSService.class);

    private SMSDao smsDao;
    private ThreadPoolExecutor threadPool = null;
    private String smsURL;
    private String userName;
    private String key;
    private String senderId;
    private String defaultRecipient;
    private String campaign;
    private String routeid;
    private String type;

    private MongoClient client;
    private DBsms dbSMS;

    public void setClient(MongoClient cl){
        this.client = cl;
    }

    public String getDefaultRecipient() {
        return defaultRecipient;
    }
    public void setDefaultRecipient(String defaultRecipient) {
        this.defaultRecipient = defaultRecipient;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getRouteid() {
        return routeid;
    }
    public void setRouteid(String routeid) {
        this.routeid = routeid;
    }
    public String getCampaign() {
        return campaign;
    }
    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }
    public String getSmsURL() {
        return smsURL;
    }
    public void setSmsURL(String smsURL) {
        this.smsURL = smsURL;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2500);

    public SMSService() {
        log.info("PHR | SMSService | Instantiating the SMSService");
        threadPool = new ThreadPoolExecutor(100, 300, 100, TimeUnit.MINUTES, queue);
    }

    public void sendActivationCode(String mobileNumber,String activationCode,String smsId, List<String> entitys) {
        threadPool.execute(new ActivationDetails(this, mobileNumber,activationCode,smsId, entitys));
    }

    public void sendLabResultDownloadCode(String mobileNumber,String activationCode,String smsId) {
        threadPool.execute(new LabResultDownload(this, mobileNumber,activationCode,smsId));
    }

    public void sendUserIdRecoveryDetails(String mobileNumber,String content,String smsId) {
        threadPool.execute(new RecoveryDetails(this, mobileNumber,smsId,content));
    }

    public void sendPasswordResetCode(String mobileNumber,String passwordResetCode,String smsId) {
        threadPool.execute(new PasswordResetDetails(this, mobileNumber,passwordResetCode,smsId));
    }

    public void sendDeleteConfirmCode(String mobileNumber,String deleteConfirmCode,String smsId) {
        threadPool.execute(new DeleteConfirmDetails(this, mobileNumber,deleteConfirmCode,smsId));
    }

    public void sendPostActivationMessage(String mobileNumber,String smsPostText, String smsId) {
        threadPool.execute(new PostActivationDetails(this, mobileNumber,smsPostText,smsId));
    }

    public void sendLinkingActivationCode(String mobileNumber,String linkingActivationCode,String smsId) {
        threadPool.execute(new LinkingActivationDetails(this, mobileNumber,linkingActivationCode,smsId));
    }

    public void sendReminderNotification(String mobileNumber,String reminderName,long reminderDue,String smsId) {
        threadPool.execute(new ReminderNotificationDetails(this, mobileNumber,reminderName,reminderDue,smsId));
    }

    public void sendIntimation(String mobileNumber,String smsId,String content, DBsms dbSMS) {
        threadPool.execute(new IntimationDetails(this, mobileNumber,smsId,content, dbSMS));
    }

    public void sendMobileAppDownloadLink(String mobileNumber,String smsId,String downloadLink) {
        threadPool.execute(new MobileAppDownloadLink(this, mobileNumber,smsId,downloadLink));
    }

    public void send(String to, String content, String smsId, DBsms sms) {
        String sendResult = "No Response";
        try {
            if (smsDao == null){
                smsDao = new SMSDao(client);
            }
            DBsms dbSMS = sms;
//            if(dbSMS.getSendResult() != null)
//                return;  //This SMS has already been sent.


            if ( getDefaultRecipient() != null && getDefaultRecipient().trim().length() != 0)
                to = getDefaultRecipient();


            List<NameValuePair> formParams = new ArrayList<NameValuePair>();

            if(dbSMS.getSmsPurpose().equals("OTP") || dbSMS.getSmsPurpose().equals("ACTIVATION")){

                formParams.add(new BasicNameValuePair("enterpriseid", "apollotp"));
                formParams.add(new BasicNameValuePair("subEnterpriseid", "apollotp"));
                formParams.add(new BasicNameValuePair("pusheid", "apollotp"));
                formParams.add(new BasicNameValuePair("pushepwd", "apollotp24"));
                formParams.add(new BasicNameValuePair("msisdn", to ));
                //	formParams.add(new BasicNameValuePair("sender", "ASKOTP"));
                formParams.add(new BasicNameValuePair("sender", "APOLLO"));
                formParams.add(new BasicNameValuePair("msgtext", content));


                UrlEncodedFormEntity formEntitySMS = new UrlEncodedFormEntity(formParams, "UTF-8");
                //		HttpPost smsHttpPostsms = new HttpPost("https://otp2.maccesssmspush.com/OTP_ACL_Web/OtpRequestListener");
                HttpPost smsHttpPostsms = new HttpPost("https://otp2.aclgateway.com/OTP_ACL_Web/OtpRequestListener");
                smsHttpPostsms.setEntity(formEntitySMS);

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse responsesms = httpClient.execute(smsHttpPostsms);

                String statusCodesms = String.valueOf(responsesms.getStatusLine().getStatusCode());

                HttpEntity entitysms = responsesms.getEntity();
                if (entitysms != null)
                    sendResult = EntityUtils.toString(entitysms);

                dbSMS.setSendResult(sendResult);
                dbSMS.setSendAt(new Date());
                smsDao.save(dbSMS, WriteConcern.FSYNC_SAFE);

                //We need to shutdown the connection Manager, not to cause a leak
                httpClient.getConnectionManager().shutdown();

                log.info("SMS send TO: " + to + " SMSId: " + smsId + " Result is : "+ sendResult + " Sent Status: " + statusCodesms);

                if(dbSMS.isWhatsapp()){

                    List<NameValuePair> formParamsWhatsApp = new ArrayList<NameValuePair>();

                    formParamsWhatsApp.add(new BasicNameValuePair("secretKey", "40f90a0f20a4b8a31ec6e44a97e7f641774d38ed6919cd72fdaf24c1c8758ef1b4f39d7ce6a2b385"));
                    formParamsWhatsApp.add(new BasicNameValuePair("phone", "91"+to ));
                    formParamsWhatsApp.add(new BasicNameValuePair("templateName", "apollootptemplate"));
                    formParamsWhatsApp.add(new BasicNameValuePair("otp", dbSMS.getAccessCode() ));

                    UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParamsWhatsApp, "UTF-8");
                    HttpPost smsHttpPost = new HttpPost("https://app.adohm.com/api/v2/open_api/otp/apollo/whatsapp");

                    smsHttpPost.setEntity(formEntity);

                    HttpClient clientmain = new DefaultHttpClient();

                    SSLContext ctx = SSLContext.getInstance("TLS");
                    X509TrustManager tm = new X509TrustManager() {

                        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        }

                        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    };
                    ctx.init(null, new TrustManager[]{tm}, null);
                    SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    ClientConnectionManager ccm = clientmain.getConnectionManager();
                    SchemeRegistry sr = ccm.getSchemeRegistry();
                    sr.register(new Scheme("https", 443, ssf));

                    HttpClient client = new DefaultHttpClient(ccm, clientmain.getParams());

                    HttpResponse response = client.execute(smsHttpPost);

                    String statusCode = String.valueOf(response.getStatusLine().getStatusCode());

                    HttpEntity entity = response.getEntity();
                    if (entity != null)
                        sendResult = EntityUtils.toString(entity);

                    dbSMS.setWhatsAppSendResult(sendResult);
                    smsDao.save(dbSMS, WriteConcern.FSYNC_SAFE);

                    client.getConnectionManager().shutdown();

                    log.info("WhatsApp OTP send TO: " + to + " SMSId: " + smsId + " Result is : "+ sendResult + " Sent Status: " + statusCode);


                }

            }else{
                formParams.add(new BasicNameValuePair("userId", "apolloalrt"));
                formParams.add(new BasicNameValuePair("pass", "apolloalrt30"));
                formParams.add(new BasicNameValuePair("appid", "apolloalrt"));
                formParams.add(new BasicNameValuePair("subappid", "apolloalrt"));
                formParams.add(new BasicNameValuePair("contenttype", "1"));
                formParams.add(new BasicNameValuePair("to", to ));
                formParams.add(new BasicNameValuePair("from", "APOLLO"));
                formParams.add(new BasicNameValuePair("text", content));
                formParams.add(new BasicNameValuePair("selfid", "true"));
                formParams.add(new BasicNameValuePair("alert", "1"));
                formParams.add(new BasicNameValuePair("dlrreq", "true"));
                formParams.add(new BasicNameValuePair("intflag", "false"));

                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
                HttpPost smsHttpPost = new HttpPost(getSmsURL());
                smsHttpPost.setEntity(formEntity);

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(smsHttpPost);

                String statusCode = String.valueOf(response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                if (entity != null)
                    sendResult = EntityUtils.toString(entity);

                dbSMS.setSendResult(sendResult);
                dbSMS.setSendAt(new Date());
                smsDao.save(dbSMS, WriteConcern.FSYNC_SAFE);

                //We need to shutdown the connection Manager, not to cause a leak
                httpClient.getConnectionManager().shutdown();

                log.info("SMS send TO: " + to + " SMSId: " + smsId + " Result is : "+ sendResult + " Sent Status: " + statusCode);
            }

            if(dbSMS.isWhatsapp() && !dbSMS.getSmsPurpose().equals("OTP") ){

                List<NameValuePair> formParamsWhatsApp = new ArrayList<NameValuePair>();

                formParamsWhatsApp.add(new BasicNameValuePair("secretKey", "40f90a0f20a4b8a31ec6e44a97e7f641774d38ed6919cd72fdaf24c1c8758ef1b4f39d7ce6a2b385"));
                formParamsWhatsApp.add(new BasicNameValuePair("phone", "91"+to ));
                formParamsWhatsApp.add(new BasicNameValuePair("templateName", "apolloupdatetemplate05"));
                formParamsWhatsApp.add(new BasicNameValuePair("p1", dbSMS.getPatientName() ));
                formParamsWhatsApp.add(new BasicNameValuePair("p2", dbSMS.getUhid()));
                formParamsWhatsApp.add(new BasicNameValuePair("p3", "https://apolloprism.com/#downloadpdf/id="+dbSMS.getDownloadId()));

                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParamsWhatsApp, "UTF-8");
                HttpPost smsHttpPost = new HttpPost("https://app.adohm.com/api/v2/open_api/whatsApp/notification/apollo");

                smsHttpPost.setEntity(formEntity);

                HttpClient clientmain = new DefaultHttpClient();

                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {

                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                ctx.init(null, new TrustManager[]{tm}, null);
                SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                ClientConnectionManager ccm = clientmain.getConnectionManager();
                SchemeRegistry sr = ccm.getSchemeRegistry();
                sr.register(new Scheme("https", 443, ssf));

                HttpClient client = new DefaultHttpClient(ccm, clientmain.getParams());

                HttpResponse response = client.execute(smsHttpPost);

                String statusCode = String.valueOf(response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                if (entity != null)
                    sendResult = EntityUtils.toString(entity);

                client.getConnectionManager().shutdown();

                dbSMS.setWhatsAppSendResult(sendResult);
                smsDao.save(dbSMS, WriteConcern.FSYNC_SAFE);

                log.info("WhatsApp send TO: " + to + " SMSId: " + smsId + " Result is : "+ sendResult + " Sent Status: " + statusCode);
            }

        } catch(Exception e) {
            log.error("PHR | SMSService | send | Error ",e);
        }
    }

//    public static void main(String[] args) {
//        log.info("Coming into SMS Service");
//
//        CSVReader reader;
//
//        String to = "9959956684";
//        String content = "Your Apollo PHR account User ID has been disabled due to an internal error. We apologize for the inconvenience & request you to re-register at -phr.apolloclinic.com/signup.html to continue using our services.";
//        String sendResult = "";
//        try {
//            reader = new CSVReader(new FileReader("phone.csv"));
//            String [] nextLine;
//            while ((nextLine = reader.readNext()) != null) {
//                log.info("Sending SMS to : {}", nextLine[0]);
//                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
//
//                formParams.add(new BasicNameValuePair("userId", "apolloalrt"));
//                formParams.add(new BasicNameValuePair("pass", "apolloalrt30"));
//                formParams.add(new BasicNameValuePair("appid", "apolloalrt"));
//                formParams.add(new BasicNameValuePair("subappid", "apolloalrt"));
//                formParams.add(new BasicNameValuePair("contenttype", "1"));
//                formParams.add(new BasicNameValuePair("to", to ));
//                formParams.add(new BasicNameValuePair("from", "APOLLO"));
//                formParams.add(new BasicNameValuePair("text", content));
//                formParams.add(new BasicNameValuePair("selfid", "true"));
//                formParams.add(new BasicNameValuePair("alert", "1"));
//                formParams.add(new BasicNameValuePair("dlrreq", "true"));
//                formParams.add(new BasicNameValuePair("intflag", "false"));
//
//                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
//                HttpPost smsHttpPost = new HttpPost("https://push3.maccesssmspush.com/servlet/com.aclwireless.pushconnectivity.listeners.TextListener");
//                smsHttpPost.setEntity(formEntity);
//
//                HttpClient httpClient = new DefaultHttpClient();
//                HttpResponse response = httpClient.execute(smsHttpPost);
//
//                String statusCode = String.valueOf(response.getStatusLine().getStatusCode());
//
//                HttpEntity entity = response.getEntity();
//                if (entity != null)
//                    sendResult = EntityUtils.toString(entity);
//
//                //We need to shutdown the connection Manager, not to cause a leak
//                httpClient.getConnectionManager().shutdown();
//
//                log.info("SMS send TO: " + to + " SMSId: " + nextLine[0] + " Results are: "+ sendResult + " Sent Status: " + statusCode);
//            }
//            reader.close();
//        }
//        catch(Exception ex) {
//            log.error("THe exception is: " , ex);
//            return;
//        }
//
//        log.info("Exiting SMS Service");
//    }
}

class ActivationDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(ActivationDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String activationCode;
    private String smsId;
    private List<String> entitys;

    public ActivationDetails(SMSService service,String mobileNumber,String activationCode,String smsId, List<String> entitys) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.activationCode = activationCode;
        this.entitys = entitys;
    }

    @Override
    public void run() {
        StringBuilder content = new StringBuilder();
        if(entitys.contains("Clinic")) {
            content.append("Dear Customer, Welcome to \"The Apollo Clinic\". Please use this activation code ");
            content.append(activationCode);
            content.append(' ');
            content.append("to login/activate your PHR account. For any help visit www.apolloclinic.com/phr/");
        } else {
            content.append("Dear Patient, Please use this activation code ");
            content.append(activationCode);
            content.append(" to login/activate your PHR account");
        }

        log.info("ActivationDetails To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}


class LabResultDownload implements Runnable {
    private static Logger log = LoggerFactory.getLogger(LabResultDownload.class);

    private SMSService service;
    private String mobileNumber;
    private String activationCode;
    private String smsId;
    //private List<String> entitys;

    public LabResultDownload(SMSService service,String mobileNumber,String activationCode,String smsId) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.activationCode = activationCode;
    }

    @Override
    public void run() {
        StringBuilder content = new StringBuilder();

        content.append("Dear Patient, Please use this code ");
        content.append(activationCode);
        content.append(" to download your lab results");

        log.info("OTP details To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}


class PostActivationDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(PasswordResetDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String postActivationMessage;
    private String smsId;

    public PostActivationDetails(SMSService service,String mobileNumber,String postActivationMessage,String smsId) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.postActivationMessage = postActivationMessage;
    }
    @Override
    public void run() {
        StringBuilder content = new StringBuilder();
        content.append(postActivationMessage);

        log.info("Post Activation message is To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}


class PasswordResetDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(PasswordResetDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String passwordResetCode;
    private String smsId;

    public PasswordResetDetails(SMSService service,String mobileNumber,String passwordResetCode,String smsId) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.passwordResetCode = passwordResetCode;
    }

    @Override
    public void run() {
        StringBuilder content = new StringBuilder();
        content.append("Dear Patient, Please use this reset code ");
        content.append(passwordResetCode);
        content.append(" to reset your PHR Account password");

        log.info("PasswordResetDetails To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}

class DeleteConfirmDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(DeleteConfirmDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String deleleConfirmCode;
    private String smsId;

    public DeleteConfirmDetails(SMSService service,String mobileNumber,String deleleConfirmCode,String smsId) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.deleleConfirmCode = deleleConfirmCode;
    }

    @Override
    public void run() {
        StringBuilder content = new StringBuilder();
        content.append("Dear Patient, Please use this delete confirmation code ");
        content.append(deleleConfirmCode);
        content.append(" to delete your account");

        log.info("DeleteConfirmDetails To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}

class LinkingActivationDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(LinkingActivationDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String linkingActivationCode;
    private String smsId;

    public LinkingActivationDetails(SMSService service,String mobileNumber,String linkingActivationCode,String smsId) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.linkingActivationCode = linkingActivationCode;
    }

    @Override
    public void run() {
        StringBuilder content = new StringBuilder();
        content.append("Dear Patient, Please use this linking access code ");
        content.append(linkingActivationCode);
        content.append(" to link your PHR account to another account");

        log.info("LinkingActivationDetails To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}

class ReminderNotificationDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(ReminderNotificationDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String reminderName;
    private long reminderDue;
    private String smsId;

    public ReminderNotificationDetails(SMSService service,String mobileNumber,String reminderName,long reminderDue,String smsId) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.reminderName = reminderName;
        this.reminderDue  = reminderDue;
    }

    @Override
    public void run() {
        SimpleDateFormat dateformat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        StringBuilder content = new StringBuilder();
        content.append("Dear Patient,");
        content.append(" Reminder Alert - Apollo Prism");
        content.append(" Reminder Name - "+reminderName);
        content.append(" Reminder Due  - "+dateformat.format(new Date(reminderDue)));

        log.info("ReminderNotificationDetails To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(), smsId);
    }
}

class IntimationDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(IntimationDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String smsId;
    private String content;
    private DBsms dbSMS;

    public IntimationDetails(SMSService service,String mobileNumber,String smsId,String content, DBsms dbSMS) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.content    = content;
        this.dbSMS = dbSMS;
    }

    @Override
    public void run() {
        log.info("IntimationDetails To: {},  {}", mobileNumber, content);
        service.send(mobileNumber, content, smsId, dbSMS);
    }
}

class RecoveryDetails implements Runnable {
    private static Logger log = LoggerFactory.getLogger(RecoveryDetails.class);

    private SMSService service;
    private String mobileNumber;
    private String smsId;
    private String content;

    public RecoveryDetails(SMSService service,String mobileNumber,String smsId,String content) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.content    = content;
    }

    @Override
    public void run() {

        log.info("RecoveryDetails To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content,smsId);
    }
}

class MobileAppDownloadLink implements Runnable {
    private static Logger log = LoggerFactory.getLogger(MobileAppDownloadLink.class);

    private SMSService service;
    private String mobileNumber;
    private String smsId;
    private String downloadLink;

    public MobileAppDownloadLink(SMSService service,String mobileNumber,String smsId,String downloadLink) {
        this.service      = service;
        this.mobileNumber = mobileNumber;
        this.smsId        = smsId;
        this.downloadLink = downloadLink;
    }

    @Override
    public void run() {
        StringBuilder content = new StringBuilder();
        content.append("Click the link ");
        content.append(downloadLink);
        content.append(" to download Ask Apollo PHR Mobile application ");

        log.info("MobileAppDownloadLink To: {},  {}", mobileNumber, content);
//        service.send(mobileNumber, content.toString(),smsId);
    }
}

