package org.apache.nifi.processors.daxoperation;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.gson.Gson;
import com.mongodb.*;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import org.apache.commons.codec.binary.Base64;
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
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.slf4j.event.Level;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Pradeep Mishra
 */
@Tags({"daxoperations", "dataman", "smsreport", "whatsapp"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class SMSReportProcessor extends AbstractProcessor {
    private static final String processorName = "SMSReportProcessor";
    private static final Charset charset = StandardCharsets.UTF_8;
    private MongoClient client = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBUtil dbUtil = null;
    private static Gson gson = null;
    private SMSDao smsDao;

    private MongoClientService mongoClientService;
    private int reportForDays;
    private String smsPurpose;
    private String reportName;
    private String sendGridApiKey;
    private String toEmailIds;
    private String ccEmailIds;
    private String reportType;

    public String getProcessorName() {
        return SMSReportProcessor.processorName;
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
        if (SMSReportProcessor.gson == null)
            SMSReportProcessor.gson = GsonUtil.getGson();
        return SMSReportProcessor.gson;
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

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public int getReportForDays() {
        return this.reportForDays;
    }

    public void setReportForDays(int reportForDays) {
        this.reportForDays = reportForDays;
    }

    public String getSmsPurpose() {
        return this.smsPurpose;
    }

    public void setSmsPurpose(String smsPurpose) {
        this.smsPurpose = smsPurpose;
    }

    public String getSendGridApiKey() {
        return this.sendGridApiKey;
    }

    public String getToEmailIds() {
        return this.toEmailIds;
    }

    public String getCcEmailIds() {
        return this.ccEmailIds;
    }

    public String getReportType() {
        return this.reportType;
    }

    public String getReportName() {
        return this.reportName;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    public static final PropertyDescriptor SMS_PURPOSE = new PropertyDescriptor
            .Builder()
            .name("SMS_PURPOSE")
            .displayName("SMS_PURPOSE")
            .description("sms_purpose type for fetching specific type of sms being sent. Eg. 'LABTEST-INTIMATION'")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor REPORT_NAME = new PropertyDescriptor
            .Builder()
            .name("REPORT_NAME")
            .displayName("REPORT_NAME")
            .description("please provide report name for the report being generated. Eg. 'LabTest'")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor REPORT_TYPE = new PropertyDescriptor
            .Builder()
            .name("REPORT_TYPE")
            .displayName("REPORT_TYPE")
            .description("please select report type. Eg. SMS, WHATSAPP or BOTH")
            .required(true)
            .allowableValues("SMS", "WHATSAPP", "BOTH")
            .build();

    public static final PropertyDescriptor REPORT_FOR_DAYS = new PropertyDescriptor
            .Builder()
            .name("REPORT_FOR_DAYS")
            .displayName("REPORT_FOR_DAYS")
            .description("REPORT FOR DAYS")
            .required(true)
            .addValidator(StandardValidators.INTEGER_VALIDATOR)
            .build();

    public static final PropertyDescriptor SENDGRID_API_KEY = new PropertyDescriptor
            .Builder()
            .name("SENDGRID_API_KEY")
            .displayName("SENDGRID_API_KEY")
            .description("SENDGRID API KEY")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor TO_EMAIL_IDS = new PropertyDescriptor
            .Builder()
            .name("TO_EMAIL_IDS")
            .displayName("TO_EMAIL_IDS")
            .description("email ids to whom report mail has to be sent. Please separate email ids by comma (,)")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor CC_EMAIL_IDS = new PropertyDescriptor
            .Builder()
            .name("CC_EMAIL_IDS")
            .displayName("CC_EMAIL_IDS")
            .description("Cc email ids to whom report mail has to be sent. Please separate email ids by comma (,)")
            .required(false)
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, SMS_PURPOSE, REPORT_NAME, REPORT_TYPE, REPORT_FOR_DAYS, SENDGRID_API_KEY, TO_EMAIL_IDS, CC_EMAIL_IDS);
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
        reportForDays = context.getProperty(REPORT_FOR_DAYS).asInteger();
        smsPurpose = context.getProperty(SMS_PURPOSE).toString();
        reportName = context.getProperty(REPORT_NAME).toString();
        sendGridApiKey = context.getProperty(SENDGRID_API_KEY).toString();
        toEmailIds = context.getProperty(TO_EMAIL_IDS).toString();
        ccEmailIds = context.getProperty(CC_EMAIL_IDS).toString();
        reportType = context.getProperty(REPORT_TYPE).toString();
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile flowFile = session.create();
        long startTime;

        try {
            logMetaData.put("processor_name", this.getProcessorName());
            startTime = this.getDateUtil().getEpochTimeInSecond();

            String[] headerFields = getSMSReportCsvHeaderFields();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
            CSVWriter csvWriter = new CSVWriter(streamWriter);
            csvWriter.writeNext(headerFields);

            client = this.getMongoClientService().getMongoClient();
            initializeDaos(client);
            Date date = getNDaysBackDate(reportForDays);
            logMetaData.put("report_start_date", date);
            logMetaData.put("report_end_date", new Date());
            List<DBsms> smsObjs = this.getSmsDao().findDBSMSToIntimateByDateRangeV2(date.getTime(), this.getSmsPurpose(), 0);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "SMS Report : DBSms Object Count " + smsObjs.size(), Level.INFO, null);
            setCsvContent(smsObjs, reportForDays, csvWriter, logMetaData);
            sendMail(stream, logMetaData);
        } catch (Exception ex) {
            markProcessorFailure(session, flowFile, logMetaData, null, Utility.stringifyException(ex));
            return;
        }

        long endTime = this.getDateUtil().getEpochTimeInSecond();
        logMetaData.put("execution_duration", endTime - startTime);
        session.transfer(flowFile, REL_SUCCESS);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "SMS Report Generated Successfully", Level.INFO, null);
    }

    private Date getNDaysBackDate(int reportForDays) {
        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.MILLISECOND, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.add(Calendar.DATE, -reportForDays);
        return cal.getTime();
    }

    private void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    private String[] getSMSReportCsvHeaderFields() {
        String[] headerFields = new String[4];
        headerFields[0] = "Name";
        headerFields[1] = "UHID";
        headerFields[2] = "Date of trigger";
        headerFields[3] = "Contact Number";

        return headerFields;
    }

    private void setCsvContent(List<DBsms> smsObjs, int reportForDays, CSVWriter csvWriter, Map<String, Object> logMetaData) throws IOException {
        int count = 0;
        String[] resultFields = new String[4];

        if (smsObjs != null && smsObjs.size() > 0) {
            for (DBsms dBsms : smsObjs) {
                if (this.getReportType().equals("SMS") && dBsms.isWhatsapp())
                    continue;

                if (this.getReportType().equals("WHATSAPP") && !dBsms.isWhatsapp())
                    continue;

                try {
                    long mobileNumber = Long.parseLong(dBsms.getMobileNumber());
                    List<String> invalidMobileNumbers = List.of("6000000000", "7000000000", "8000000000", "9000000000", "6666666666", "7777777777", "8888888888", "9999999991", "9999999992", "9999999993", "9999999994", "9999999995", "9999999996", "9999999997", "9999999998", "9999999999");
                    if (mobileNumber < Long.parseLong("6000000000") || mobileNumber > Long.parseLong("9999999999") || invalidMobileNumbers.contains(dBsms.getMobileNumber()))
                        continue;
                } catch (Exception ignored) {
                    continue;
                }

                resultFields[0] = dBsms.getPatientName();
                resultFields[1] = dBsms.getUhid();
                resultFields[2] = dBsms.getSendAt() != null ? dBsms.getSendAt().toString() : null;
                resultFields[3] = dBsms.getMobileNumber();
                csvWriter.writeNext(resultFields);
                count++;
            }
        }
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Total rows fetched for csv creation: " + count, Level.INFO, null);
        csvWriter.close();
    }

    private void sendMail(ByteArrayOutputStream stream, Map<String, Object> logMetaData) {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Sending report...", Level.INFO, null);

        try {

            String mailsubject = "";
            String reportType = this.getReportType().equals("BOTH") ? " " : " " + this.getReportType().toLowerCase() + " ";
            mailsubject = this.getReportName().trim() + reportType + "report for last " + this.getReportForDays() + " days ";

            Email from = new Email("no-reply-prescriptions@apollo247.com");
            Personalization personalization = new Personalization();

            if (this.getToEmailIds() != null && !this.getToEmailIds().isEmpty()) {
                for (String email : this.getToEmailIds().split(",")) {
                    personalization.addTo(new Email(email.trim()));
                }
            }

            if (this.getCcEmailIds() != null && !this.getCcEmailIds().isEmpty()) {
                for (String email : this.getCcEmailIds().split(",")) {
                    personalization.addCc(new Email(email.trim()));
                }
            }

            Content mailcontent = new Content("text/plain", "Please find the attached report.");
            Mail mail = new Mail();
            mail.addPersonalization(personalization);
            mail.addContent(mailcontent);
            mail.setFrom(from);
            mail.setSubject(mailsubject);
            Attachments attachments = new Attachments();
            if (stream != null) {
                Base64 x = new Base64();
                String csvString = x.encodeAsString(stream.toByteArray());
                attachments.setContent(csvString);
                attachments.setFilename(this.getReportName().trim() + ".csv");
                mail.addAttachments(attachments);
            }

            MailSettings mailSettings = new MailSettings();
            Setting sandBoxMode = new Setting();
            sandBoxMode.setEnable(true);
            mailSettings.setSandboxMode(sandBoxMode);

            SendGrid sg = new SendGrid(this.getSendGridApiKey());
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            request.setBody(mail.build());

            Response response = sg.api(request);
            Map<String, Object> otherDetails = new HashMap<>();
            otherDetails.put("responseCode", response.getStatusCode());
            otherDetails.put("responseBody", response.getBody());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "THE RESPONSE CODE IS : " + response.getStatusCode(), Level.INFO, otherDetails);
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
    }

    private void initializeDaos(MongoClient client) {
        this.smsDao = new SMSDao(client);
    }
}
