package org.apache.nifi.processors.daxoperation;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.GsonUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.apache.nifi.processors.daxoperation.utils.Utility;
import org.apache.nifi.stream.io.StreamUtils;
import org.bson.types.ObjectId;
import org.slf4j.event.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SegregateData extends AbstractProcessor {
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder().name("Database Connection Pooling Service")
            .displayName("Database Connection Pooling Service").description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class).build();
    private static final String processorName = "SegregateData";
    private static final Charset charset = Charset.forName("UTF-8");
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
            .description("Default : 10")
            .build();

    public static final PropertyDescriptor DEBUG = new PropertyDescriptor
            .Builder().name("DEBUG")
            .displayName("DEBUG")
            .required(true)
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .description("for debug purpose")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private Map<String, DB> siteDbs = new HashMap<String, DB>();
    private MongoClient mongoDb = null;
    private Map<String, Object> logMetaData;
    protected MongoClientService mongoClientService;

    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private int queryLimit = 10;
    private boolean isdebug = false;

    private List<FFInput> segregatedOutput;
    private Map<String, FFInput> segMap;

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

    @Override
    protected void init(final ProcessorInitializationContext context) {
        List<PropertyDescriptor> initDescriptors = new ArrayList<>();
        initDescriptors.add(MONGODB_CLIENT_SERVICE);
        initDescriptors.add(QUERY_LIMIT);
        initDescriptors.add(DEBUG);
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
//        loadContextProperties(context);
//        dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {

        logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.create();

        DB appDb = null;
        String curId = "";
        String prevId = "";

        try {
            mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);

            isdebug = context.getProperty(DEBUG).asBoolean();
            Integer ql = context.getProperty(QUERY_LIMIT).asInteger();
            if (ql != null) {
                queryLimit = ql;
            }
            segMap = new HashMap<>();
            mongoDb = mongoClientService.getMongoClient();
            siteMasterMap = SiteMaster.loadSiteMasterMap(mongoDb, logMetaData, getLogUtil());
            appDb = mongoDb.getDB("HHAppData");
            ObjectId startId = getStartingId(appDb);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Starting to segregation process from: " + startId, Level.INFO, null);
            BasicDBObject query = new BasicDBObject();
            if (startId != null) {
                query.put("_id", new BasicDBObject("$gt", startId));
                prevId = startId.toString();
            }

            DBCollection coll = appDb.getCollection("ImportData");
            try (DBCursor cur = coll.find(query).limit(queryLimit)) {
                int count = 0;
                while (cur.hasNext()) {
                    DBObject obj = cur.next();
                    curId = obj.get("_id").toString();
                    logMetaData.put("curObjId", curId);
                    if (obj.get("siteApiKey") == null)
                        continue;
                    String siteApiKey = obj.get("siteApiKey").toString();
                    String tableName = obj.get("tableName").toString();
                    logMetaData.put("agentSiteApiKey", siteApiKey);
                    logMetaData.put("tableName", tableName);
                    Object dt = obj.get("data");
                    if (dt == null)
                        continue;

                    String data = decompress(dt.toString());
                    JsonElement json = new JsonParser().parse(data);
                    if (json.isJsonArray())
                        dumpArray(siteApiKey, tableName, (JsonArray) json, data);
                    else
                        dumpData(siteApiKey, tableName, (JsonObject) json, data);

                    prevId = curId;
                    count++;
                }
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "processed : " + count, Level.INFO, null);
                for (FFInput f : segMap.values()) {
                    FlowFile output = getFlowFile(session, flowFile, gson.toJson(f));
                    session.putAttribute(output, "uhidPrefix", (siteMasterMap.get(f.siteApiKey) != null ? siteMasterMap.get(f.siteApiKey).getUhidPrefix() : ""));
                    session.putAttribute(output, "siteApiKey", f.siteApiKey);
                    session.transfer(output, REL_SUCCESS);
                }
            }
        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.INFO, null);

            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
            session.transfer(output, REL_FAILURE);
        } finally {
            //Save the starting Id for the next iteration.
            saveStartingId(appDb, prevId);
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

    private String decompress(String data) {
        try {
            byte[] base64 = Base64.decodeBase64(data);
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(base64));
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buf)) > 0)
                out.write(buf, 0, len);

            gzipInputStream.close();
            out.close();

            return StringUtils.newStringUtf8(out.toByteArray());
        } catch (Exception ex) {
//            log.error("Exception while decompresing data: " + data, ex);
        }

        return data;
    }

    private ObjectId getStartingId(DB appDb) {
        try {
            DBCollection coll = appDb.getCollection("DataMan");
            DBObject data = coll.findOne();
            if (data == null)
                return null;
            String id = data.get("ImportDataId").toString();
            if (id != null && !id.isEmpty())
                return new ObjectId(id);
        } catch (Exception ex) {
//            log.error("Exception while getting starting Id", ex);
        }

        return null;
    }

    private void saveStartingId(DB appDb, String curId) {
        DBCollection coll = appDb.getCollection("DataMan");
        DBObject data = coll.findOne();
        if (data == null) {
            data = new BasicDBObject();
        }
        data.put("ImportDataId", curId);
        coll.save(data);
//        log.info("Processed until: " + curId);
//        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
//                "processed : " + count, Level.INFO, null);
    }

    private String getLocationSiteApiKey(String locationId) {
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getLocationId() != null &&
                    siteDetails.getLocationId().equals(locationId))
                return siteDetails.getSiteKey();
        }

        return null;
    }

    private String getSiteBasedOnPrefix(String sitePrefix) {
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getUhidPrefix() != null &&
                    siteDetails.getUhidPrefix().equals(sitePrefix))
                return siteDetails.getSiteKey();
            ;
        }

        return null;
    }


    private void dumpArray(String siteApiKey, String tableName, JsonArray jArray, String data) throws Exception {
        Iterator<JsonElement> iterator = jArray.iterator();
        while (iterator.hasNext()) {
            JsonObject jsonObj = (JsonObject) iterator.next();
            dumpData(siteApiKey, tableName, jsonObj, data);
        }
    }

    private void dumpData(String siteApiKey, String tableName, JsonObject jsonObj, String data) throws Exception {
        String curSiteKey = siteApiKey;
        String locId = null;

        if (jsonObj.get("locationId") != null)
            locId = jsonObj.get("locationId").getAsString();

        if (locId != null) {
            curSiteKey = getLocationSiteApiKey(locId);
            if (curSiteKey == null) {
                try {
                    throw new Exception("Could not find the site for locationId: " + locId);
                } catch (Exception ex) {
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, ex.getMessage(), Level.WARN, null);
                    return;
                }
            }
        }
        if (this.isdebug) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "dumpData data " + GsonUtil.getGson().toJson(jsonObj), Level.INFO, null);
        }

        if (tableName.equals("discharge_summary")) {

            try {
                if (jsonObj.get("fileName") != null) {
                    String fileName = jsonObj.get("fileName").getAsString();
                    String[] parts = fileName.split("\\.");
                    if (parts.length > 3) {
//                        log.info("Parsing file Name: " + fileName);
                        String uhid = parts[1] + "." + parts[2];

                        if (uhid.indexOf('.') != -1) {
                            String[] part = uhid.split("\\.");
                            if (part.length > 2) {
//                                log.error("Imporper | SegregationJob UHID: {}, unable to split it.", uhid);
                            } else {
                                if (part.length > 1) {
                                    String uhidSiteKey = getSiteBasedOnPrefix(part[0]);
                                    if (uhidSiteKey != null) {
                                        curSiteKey = uhidSiteKey;
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.WARN, null);
            }
        }

        if (tableName.equals("bill_group")) {

            try {
                if (jsonObj.get("fileName") != null) {
                    String fileName = jsonObj.get("fileName").getAsString();
                    String[] parts = fileName.split("_");
                    if (!(parts.length < 6)) {

                        String uhid = parts[4];

                        if (uhid.indexOf('.') != -1) {
                            String[] part = uhid.split("\\.");
                            if (part.length > 2) {
//                                log.error("Imporper | SegregationJob UHID: {}, unable to split it.", uhid);
                            } else {
                                if (part.length > 1) {
                                    String uhidSiteKey = getSiteBasedOnPrefix(part[0]);
                                    if (uhidSiteKey != null) {
                                        curSiteKey = uhidSiteKey;
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.WARN, null);
            }
        }

        SiteDetails siteDetails = siteMasterMap.get(curSiteKey);
        if (siteDetails == null) {
            throw new Exception("Could not find the siteDetails for site Api Key: " + curSiteKey);
        }
        String siteDbName = siteDetails.getSiteDb();
        DB siteDb = getSiteDB(siteDbName);
        DBCollection coll = siteDb.getCollection(tableName);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL,
                "curSiteKey: " + curSiteKey + "\n" +
                        "siteDbName done: " + siteDbName + "\n" +
                        "coll got from table name: " + tableName, Level.INFO, null);

        DBObject dbObj = getDBObject(jsonObj);
        putIntoSegMap(curSiteKey, jsonObj, tableName);

        if (tableName.equals("HeartBeat")) {
            Map<String, Object> heartBeatDetails = new HashMap<>();
            String agentName = siteMasterMap.get(siteApiKey).getSiteName();
            heartBeatDetails.put("agentKey", siteApiKey);
            heartBeatDetails.put("agentName", agentName);
            heartBeatDetails.put("lastSyncTimeReceived", dbObj.get("date"));
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Agent " + agentName + " last synced time : " + dbObj.get("date"), Level.INFO, heartBeatDetails);
        }

        //2021-10-18: Stopping writing objects to sitedbs in new realtime flow
//        if (!realtimeLiveProcessors.contains(tableName))
//            coll.save(dbObj);
    }

    private void putIntoSegMap(String curSiteKey, JsonObject jsonObj, String tableName) {
        FFInput ff = new FFInput();
        ff.siteApiKey = curSiteKey;

        if (segMap.get(curSiteKey) != null) {
            ff.patients = Objects.requireNonNullElseGet(segMap.get(curSiteKey).patients, ArrayList::new);
            ff.specialResults = Objects.requireNonNullElseGet(segMap.get(curSiteKey).specialResults, ArrayList::new);
            ff.dischargeSummaries = Objects.requireNonNullElseGet(segMap.get(curSiteKey).dischargeSummaries, ArrayList::new);
            ff.prescriptions = Objects.requireNonNullElseGet(segMap.get(curSiteKey).prescriptions, ArrayList::new);
            ff.pharmacyBill = Objects.requireNonNullElseGet(segMap.get(curSiteKey).pharmacyBill, ArrayList::new);
            ff.consultation = Objects.requireNonNullElseGet(segMap.get(curSiteKey).consultation, ArrayList::new);
            ff.tool = Objects.requireNonNullElseGet(segMap.get(curSiteKey).tool, ArrayList::new);
            ff.prohealth = Objects.requireNonNullElseGet(segMap.get(curSiteKey).prohealth, ArrayList::new);
            ff.clinicprohealth = Objects.requireNonNullElseGet(segMap.get(curSiteKey).clinicprohealth, ArrayList::new);
            ff.homehealthVisit = Objects.requireNonNullElseGet(segMap.get(curSiteKey).homehealthVisit, ArrayList::new);
            ff.dietPlan = Objects.requireNonNullElseGet(segMap.get(curSiteKey).dietPlan, ArrayList::new);
        } else {
            ff.patients = new ArrayList<>();
            ff.specialResults = new ArrayList<>();
            ff.dischargeSummaries = new ArrayList<>();
            ff.prescriptions = new ArrayList<>();
            ff.pharmacyBill = new ArrayList<>();
            ff.consultation = new ArrayList<>();
            ff.tool = new ArrayList<>();
            ff.prohealth = new ArrayList<>();
            ff.clinicprohealth = new ArrayList<>();
            ff.homehealthVisit = new ArrayList<>();
            ff.dietPlan = new ArrayList<>();
        }

        switch (tableName) {
            case "patient":
                ff.patients.add(GsonUtil.getGson().fromJson(jsonObj, Patient.class));
                break;
            case "special_result":
                ff.specialResults.add(GsonUtil.getGson().fromJson(jsonObj, SpecialResult.class));
                break;
            case "discharge_summary":
                String fileName = jsonObj.get("fileName").getAsString();
                String[] parts = fileName.split("\\.");
                if (parts.length != 0) {
                    if (parts[0].contains("OP") || parts[0].contains("EP")) {
                        ff.prescriptions.add(GsonUtil.getGson().fromJson(jsonObj, DischargeSummary.class));
                    } else if (parts[0].contains("IP")) {
                        ff.dischargeSummaries.add(GsonUtil.getGson().fromJson(jsonObj, DischargeSummary.class));
                    } else {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                "Invalid file Name for Discharge summary " + fileName, Level.INFO, null);
                    }
                }
                break;
            case "pharmacy_bills":
                ff.pharmacyBill.add(GsonUtil.getGson().fromJson(jsonObj, PharmacyBill.class));
                break;
            case "consultation":
                ff.consultation.add(GsonUtil.getGson().fromJson(jsonObj, Consultation.class));
                break;
            case "tool_data":
                ff.tool.add(GsonUtil.getGson().fromJson(jsonObj, Tool.class));
                break;
            case "prohealth":
                ff.prohealth.add(GsonUtil.getGson().fromJson(jsonObj, ProHealth.class));
                break;
            case "clinics_prohealth":
                ff.clinicprohealth.add(GsonUtil.getGson().fromJson(jsonObj, ClinicProhealth.class));
                break;
            case "homecare":
                ff.homehealthVisit.add(GsonUtil.getGson().fromJson(jsonObj, HomehealthVisit.class));
                break;
            case "dietplancore":
                ff.dietPlan.add(GsonUtil.getGson().fromJson(jsonObj, DietPlan.class));
                break;
            case "dietplandetails":
                ff.dietPlan.add(GsonUtil.getGson().fromJson(jsonObj, DietPlan.class));
                break;
        }

        segMap.put(curSiteKey, ff);
    }

    private DB getSiteDB(String siteKey) {
        if (siteDbs.containsKey(siteKey))
            return siteDbs.get(siteKey);

        DB siteDb = mongoDb.getDB(siteKey);
        siteDbs.put(siteKey, siteDb);
        return siteDb;
    }

    private DBObject getDBObject(JsonElement jsonElement) {
        DBObject dbObject = null;
        if (jsonElement.isJsonPrimitive()) {
            dbObject = new BasicDBObject();
            dbObject.put("data", jsonElement.getAsString());
        } else if (jsonElement.isJsonObject()) {
            BasicDBObject dbObj = new BasicDBObject();
            for (Map.Entry<String, JsonElement> kp : jsonElement.getAsJsonObject().entrySet()) {
                JsonElement curJE = kp.getValue();
                if (!curJE.isJsonPrimitive()) {
                    dbObj.put(kp.getKey(), getDBObject(curJE));
                } else {
                    dbObj.put(kp.getKey(), curJE.getAsString());
                }
            }
            dbObject = dbObj;
        } else if (jsonElement.isJsonArray()) {
            BasicDBList dbList = new BasicDBList();
            JsonArray jArray = jsonElement.getAsJsonArray();
            Iterator<JsonElement> iterator = jArray.iterator();
            while (iterator.hasNext()) {
                JsonElement jEle = (JsonElement) iterator.next();
                dbList.add(getDBObject(jEle));
            }
            dbObject = dbList;
        }

        return dbObject;
    }

}
