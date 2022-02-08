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
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processors.daxoperation.dm.*;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.models.dax.TestData;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Tags({"daxoperation", "dataman", "cleanup", "tests"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class CleanupTests extends AbstractProcessor {
    private static final String processorName = "CleanupTests";
    private static final Charset charset = StandardCharsets.UTF_8;
    private SimpleDateFormat dateformat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private LogUtil logUtil = null;

    private static Gson gson = null;

    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private MongoClient client = null;

    protected MongoClientService mongoClientService;

    public String getProcessorName() {
        return CleanupTests.processorName;
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
        if (CleanupTests.gson == null) {
            CleanupTests.gson = GsonUtil.getGson();
        }
        return CleanupTests.gson;
    }

    public MongoClient getMongoClient() {
        return this.client;
    }

    public void setMongoClient(MongoClient client) {
        this.client = client;
    }

    public SiteDetails getSiteDetails() {
        return this.siteDetails;
    }

    public void setSiteDetails(SiteDetails siteDetails) {
        this.siteDetails = siteDetails;
    }

    public Map<String, SiteDetails> getSiteMasterMap() {
        return this.siteMasterMap;
    }

    public void setSiteMasterMap(Map<String, SiteDetails> sitemasterMap) {
        this.siteMasterMap = sitemasterMap;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
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
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE);
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
        setMongoClientService(context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class));
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        Date today = new Date();
        int count = 0;

        DB appDb = null;
        DBCollection dbTests = null;
        DBCursor cur = null;

        try {
            logMetaData.put("processor_name", getProcessorName());

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            FFInput ffinput = getGson().fromJson(ffContent, FFInput.class);

            setSiteDetails(ffinput.siteDetails);
            setSiteMasterMap(ffinput.siteMasterMap);

            logMetaData.put(Constants.SITE_NAME, getSiteDetails().getSiteName());

            MongoClient mongoClient = getMongoClientService().getMongoClient();
            setMongoClient(mongoClient);

            appDb = getMongoClient().getDB(siteDetails.getSiteDb());
            dbTests = appDb.getCollection(SiteCollections.special_result.name());
            cur = dbTests.find();

            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("CleanupTests: %s Before cleaning Tests : %s", siteDetails.getSiteName(), dbTests.count()), Level.INFO, null);

            while (cur.hasNext()) {
                DBObject dbOrig = cur.next();
                DBObject dbTest = dbOrig;
                if(dbOrig.get(TestData.special_result.name()) != null)
                    dbTest = (DBObject)dbOrig.get(TestData.special_result.name());

                Date testDate = null;
                String crtTime = null;

                try {
                    if(dbTest.get(TestData.created_at.name()) != null) {
                        crtTime = dbTest.get(TestData.created_at.name()).toString();
                    }
                    if(dbTest.get(TestData.received_on.name()) != null && dbTest.get(TestData.received_on.name()).toString().length() != 0) {
                        crtTime = dbTest.get(TestData.received_on.name()).toString();
                    }
                    testDate = dateformat.parse(getDateWithoutGMT(crtTime));
                } catch (Exception e) {
                    getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("CleanupTests: Error while parsing date : %s", Utility.stringifyException(e)), Level.INFO, null);
                }

                if(testDate != null && daysBetween(testDate, today) < 25) {
                    continue;
                }
                
                dbTests.remove(dbOrig);
                count++;
            }
        } catch (Exception ex) {
            markProcessorFailure(session, inputFF, logMetaData, null, String.format("Exception while cleaning Tests : %s", Utility.stringifyException(ex)));
            return;
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        logMetaData.put("deleted", count);
        session.remove(inputFF);
        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    public long daysBetween(Date d1, Date d2) {
		return Math.abs((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}

    public String getDateWithoutGMT(String dt) {
		int pos = dt.indexOf('T'); 
		if( pos == -1) {
			dt = dt.replace(' ', 'T');			
		}
		
		pos = dt.indexOf('+');
		if(pos != -1)
			dt = dt.substring(0,dt.indexOf('+'));
		
		if(dt.endsWith(".0"))
			dt = dt.substring(0, dt.length()-2);

		//remove milliseconds
		pos = dt.indexOf('.');
		if(pos != -1)
			dt = dt.substring(0,dt.indexOf('.'));
		
		return (dt);
	}
}