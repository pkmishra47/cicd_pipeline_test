package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GetDataFromSiteDB extends AbstractProcessor {
    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
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
    public static final PropertyDescriptor TYPE = new PropertyDescriptor
            .Builder().name("TYPE")
            .displayName("TYPE")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .description("Collection type")
            .build();
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("marks process successful when processor achieves success condition.")
            .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("marks process failure when processor achieves failure condition.")
            .build();
    private static final String processorName = "GetDataFromSiteDB";

    private static final Charset charset = StandardCharsets.UTF_8;

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    private static Gson gson = null;
    protected MongoClientService mongoClientService;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Integer mongoDbQueryLimit = 5000;
    private boolean debugOn = false;
    private String collectionType;
    private DBCollection dbCollection = null;
    private Map<String, Object> logMetaData;

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();

        return this.dateUtil;
    }

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE, QUERY_LIMIT, DEBUG, TYPE);
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
        Integer lim = context.getProperty(QUERY_LIMIT).asInteger();
        if (lim != null) {
            mongoDbQueryLimit = lim;
        }

        if (context.getProperty(DEBUG).evaluateAttributeExpressions().getValue() != null)
            debugOn = Boolean.parseBoolean(context.getProperty(DEBUG).evaluateAttributeExpressions().getValue());

        collectionType = context.getProperty(TYPE).toString();
    }


    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("triggger ygh"), Level.INFO, null);
        gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();
        try {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("try ygh"), Level.INFO, null);
            this.mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
            String ffContent = readFlowFileContent(flowFile, session);
            FFInput ffinput = gson.fromJson(ffContent, FFInput.class);

            siteDetails = ffinput.siteDetails;
            siteMasterMap = ffinput.siteMasterMap;

            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            MongoClient client = this.mongoClientService.getMongoClient();
            DB appDb = client.getDB(siteDetails.getSiteDb());
            if (collectionType.equals("pharmacy_bills")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("pharmacy_bills "), Level.INFO, null);
                dbCollection = appDb.getCollection("pharmacy_bills");
                processPharmacyBills(session, flowFile);
            } else if (collectionType.equals("consultation")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("consultation "), Level.INFO, null);
                dbCollection = appDb.getCollection("consultation");
                processConsultation(session, flowFile);
            } else if (collectionType.equals("tool_data")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("tool_data "), Level.INFO, null);
                dbCollection = appDb.getCollection("tool_data");
                processToolData(session, flowFile);
            } else if (collectionType.equals("prohealth")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("prohealth "), Level.INFO, null);
                dbCollection = appDb.getCollection("prohealth");
                processProhealthData(session, flowFile);
            } else if (collectionType.equals("clinics_prohealth")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("clinics_prohealth "), Level.INFO, null);
                dbCollection = appDb.getCollection("clinics_prohealth");
                processClinicsProhealthData(session, flowFile);
            } else if (collectionType.equals("homecare")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("homecare "), Level.INFO, null);
                dbCollection = appDb.getCollection("homecare");
                processHomeHealthVisitData(session, flowFile);
            } else if (collectionType.equals("dietplancore")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("dietplancore "), Level.INFO, null);
                dbCollection = appDb.getCollection("dietplancore");
                processdietplancoreData(session, flowFile);
            } else if (collectionType.equals("dietplandetails")) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("dietplandetails "), Level.INFO, null);
                dbCollection = appDb.getCollection("dietplandetails");
                processdietplancoreData(session, flowFile);
            } else {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("Improper Collection type found %s ..... returning ", collectionType), Level.INFO, null);
                return;
            }
        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("Exception %s ", Utility.stringifyException(ex)), Level.ERROR, null);
            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
    }

    private void processPharmacyBills(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process pharmacy_bills"), Level.INFO, null);
        List<PharmacyBill> pharmacyBillList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                PharmacyBill pharmacyBill = gson.fromJson(gson.toJson(dbOrig), PharmacyBill.class);
                pharmacyBillList.add(pharmacyBill);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.pharmacyBill = pharmacyBillList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    private void processConsultation(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process consultation"), Level.INFO, null);
        List<Consultation> consultationList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                Consultation consultation = gson.fromJson(gson.toJson(dbOrig), Consultation.class);
                consultationList.add(consultation);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.consultation = consultationList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    private void processToolData(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process tool_data"), Level.INFO, null);
        List<Tool> toolList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                Tool tool = gson.fromJson(gson.toJson(dbOrig), Tool.class);
                toolList.add(tool);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.tool = toolList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    private void processProhealthData(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process prohealth"), Level.INFO, null);
        List<ProHealth> toolList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                ProHealth tool = gson.fromJson(gson.toJson(dbOrig), ProHealth.class);
                toolList.add(tool);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.prohealth = toolList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    private void processClinicsProhealthData(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process ClinicsProhealth"), Level.INFO, null);
        List<ClinicProhealth> toolList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                ClinicProhealth tool = gson.fromJson(gson.toJson(dbOrig), ClinicProhealth.class);
                toolList.add(tool);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.clinicprohealth = toolList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    private void processHomeHealthVisitData(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process HomeHealthVisit"), Level.INFO, null);
        List<HomehealthVisit> toolList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                HomehealthVisit tool = gson.fromJson(gson.toJson(dbOrig), HomehealthVisit.class);
                toolList.add(tool);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.homehealthVisit = toolList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    private void processdietplancoreData(ProcessSession session, FlowFile flowFile) throws Exception {
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format("process dietplancore"), Level.INFO, null);
        List<DietPlan> toolList = new ArrayList<>();
        try (DBCursor cur = dbCollection.find().limit(mongoDbQueryLimit)) {
            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();

                DietPlan tool = gson.fromJson(gson.toJson(dbOrig), DietPlan.class);
                toolList.add(tool);
                dbCollection.remove(dbOrig);
            }
        }

        FFInput ffInput = new FFInput();
        ffInput.siteApiKey = siteDetails.getSiteKey();
        ffInput.dietPlan = toolList;

        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" session is null " + (session == null)), Level.INFO, null);

        FlowFile output = getFlowFile(session, flowFile, gson.toJson(ffInput));
        session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(ffInput.siteApiKey) != null ? siteMasterMap.get(ffInput.siteApiKey).getUhidPrefix() : ""));
        session.putAttribute(output, "siteApiKey", ffInput.siteApiKey);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                String.format(" output is null " + (output == null)), Level.INFO, null);
        session.transfer(output, REL_SUCCESS);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
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
