package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.GsonUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.apache.nifi.processors.daxoperation.utils.Utility;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.util.*;

public class GetSiteMasterMap extends AbstractProcessor {
    private static final String processorName = "GetSiteMasterMap";
    protected MongoClientService mongoClientService;
    private static final Charset charset = Charset.forName("UTF-8");

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder().name("Database Connection Pooling Service")
            .displayName("Database Connection Pooling Service").description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class).build();

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBCPService dbcpService;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;

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
        mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {

        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.create();

        MongoClient client = this.getMongoClientService().getMongoClient();
        try {
            siteMasterMap = SiteMaster.loadSiteMasterMap(client, logMetaData, logUtil);
//                    loadSiteMasterMap(client, logMetaData);
//            for(String key : siteMasterMap.keySet()){
//                FFInput out = new FFInput();
//                out.siteDetails = siteMasterMap.get(key);
//                out.siteMasterMap = siteMasterMap;
//                FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out));
//                session.transfer(output, REL_SUCCESS);
//            }
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(siteMasterMap));
            session.transfer(output, REL_SUCCESS);
        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.INFO, null);

            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
    }

    public Map<String, SiteDetails> loadSiteMasterMap(MongoClient client, Map<String, Object> logMetaData) {
        DB appDb = client.getDB("HHAppData");
        Map<String, SiteDetails> mp = new HashMap<String, SiteDetails>();
        DBCollection coll = appDb.getCollection("SiteMasterDB");
        DBCursor cur = coll.find();
        while (cur.hasNext()) {
            DBObject obj = cur.next();
            String siteKey = obj.get("dbSiteKey").toString();
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Got sitekey : " + siteKey, Level.INFO, null);
            SiteDetails siteDetails = new SiteDetails(siteKey);
            siteDetails.setSiteName(obj.get("dbSiteName").toString());
            siteDetails.setSiteKey(siteKey);
            siteDetails.setSiteType(obj.get("dbSiteType").toString());
            siteDetails.setEntityName(obj.get("entityName").toString());
            Object tbl = obj.get("testBlackList");
            if (tbl != null)
                siteDetails.setTestBlackList(tbl.toString());
            Object hbl = obj.get("hcuBlackList");
            if (hbl != null)
                siteDetails.setHcuBlackList(hbl.toString());
            Object uhp = obj.get("uhidPrefix");
            if (uhp != null)
                siteDetails.setUhidPrefix(uhp.toString());

            Object locId = obj.get("locationId");
            if (locId != null)
                siteDetails.setLocationId(locId.toString());

            Object deb = obj.get("debug");
            if (deb != null)
                siteDetails.setDebug(new Boolean(deb.toString()));

            siteDetails.setSms(true);
            Object sms = obj.get("sms");
            if (sms != null)
                siteDetails.setSms(new Boolean(sms.toString()));

            //Populate the hidden tests array from the configuration.
            loadHiddenTests(siteDetails);
            mp.put(siteDetails.getSiteKey(), siteDetails);
        }

        return mp;
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    private boolean loadHiddenTests(SiteDetails siteDetails) {
        try {
            if (siteDetails.getTestBlackList() != null && siteDetails.getTestBlackList().length() != 0) {
                String[] testIds = siteDetails.getTestBlackList().split(",");
                int[] blackListTests = new int[testIds.length];
                siteDetails.setBlackListTests(testIds); // here to testIds
            }
            if (siteDetails.getBlackListHCUs() != null && siteDetails.getBlackListHCUs().length != 0) {
                String[] testIds = siteDetails.getHcuBlackList().split(",");
                int[] blackListHCUs = new int[testIds.length];
                for (int pos = 0; pos < testIds.length; pos++)
                    blackListHCUs[pos] = Integer.parseInt(testIds[pos].trim());
                Arrays.sort(blackListHCUs);
                siteDetails.setBlackListHCUs(blackListHCUs);
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
