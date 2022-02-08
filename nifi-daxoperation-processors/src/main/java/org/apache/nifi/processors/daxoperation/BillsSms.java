package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dao.EntityDao;
import org.apache.nifi.processors.daxoperation.dao.SiteMasterDao;
import org.apache.nifi.processors.daxoperation.dbo.DBEntity;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
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
import java.util.*;

public class BillsSms extends AbstractProcessor {
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    private static final String processorName = "BillsSms";
    private static final Charset charset = Charset.forName("UTF-8");
    private static final PropertyDescriptor MONGO_CONNECTION_URL = new PropertyDescriptor
            .Builder().name("MONGO_CONNECTION_URL").displayName("MONGO CONNECTION URL").
            description("mongodb://{{AppServerUser}}:{{AppServerPassword}}@{{AppServer}}/admin")
            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    public static final PropertyDescriptor BILL_SMS_URL = new PropertyDescriptor
            .Builder().name("BILL_SMS_URL")
            .displayName("BILL_SMS_URL")
            .description("provide apollo247 webhook url to push new uhid info")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    private DateUtil dateUtil = null;
    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private DBsms dbsms = null;
    private SiteMasterDao siteMasterDao = null;
    private EntityDao entityDao = null;
    private SMSService smsService = null;
    private Map<String, SiteMaster> siteMap = new HashMap<String, SiteMaster>();
    private Map<String, DBEntity> entitys = new HashMap<String, DBEntity>();
    //    private SiteMasterDao siteMasterDao = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    Map<String, Object> logMetaData;

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();

        return this.dateUtil;
    }

    private LogUtil logUtil = null;
    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public SMSService getSmsService() {
        if (this.smsService == null) {
            this.smsService = new SMSService();

        }

        return this.smsService;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    @Override
    protected void init(final ProcessorInitializationContext context) {
        List<PropertyDescriptor> initDescriptors = new ArrayList<>();
        initDescriptors.add(MONGO_CONNECTION_URL);
        initDescriptors.add(BILL_SMS_URL);
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
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();

        String dbUrl = context.getProperty(MONGO_CONNECTION_URL).evaluateAttributeExpressions().getValue();
        String smsUrl = context.getProperty(BILL_SMS_URL).evaluateAttributeExpressions().getValue();
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        try (MongoClient client = new MongoClient(new MongoClientURI(dbUrl))) {
            String ffContent = readFlowFileContent(flowFile, session);
            FFInput ffinput = gson.fromJson(ffContent, FFInput.class);

            siteDetails = ffinput.siteDetails;
            siteMasterMap = ffinput.siteMasterMap;
            dbsms = ffinput.sms;
            siteStats = siteDetails.getStats();

            siteMasterDao = new SiteMasterDao(client);
            entityDao = new EntityDao(client);
            if (dbsms != null) {
                sendSMS(dbsms, "Bills", smsUrl);
            }
        }
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

    public void sendSMS(DBsms dbSMS, String intimationType, String smsUrl) {
        try {
            SiteMaster dbSite;
            dbSite = siteMasterDao.findDBSiteBySiteKey(dbSMS.getSiteKey());

            String content = null;
            if (intimationType.equals("Bills")) {
                if (dbSMS.getDownloadId() != null) {
                    content = "Namaste " + dbSMS.getPatientName() + " " + dbSMS.getUhid() + " To download bills : " + smsUrl + dbSMS.getDownloadId() + " or download Apollo247app:https://ap247.onelink.me/JG0h/bill";
                }
            }
            if (null != content) {
                smsService.sendIntimation(dbSMS.getMobileNumber(), String.valueOf(dbSMS.getId()), content, dbSMS);
            }
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
//            log.error("PHR | DataMan | sendSMS " + intimationType + " | SMSId: {}", dbSMS.getId(), e);
        }

//        log.info("PHR | DataMan | SMS | Exiting from sendSMS function");
    }
}
