package org.apache.nifi.processors.daxoperation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mongodb.MongoClient;

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
import org.apache.nifi.processors.daxoperation.dao.CarePersonaOCRDao;
import org.apache.nifi.processors.daxoperation.dao.SmartScanDao;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.models.TextractModel;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.GsonUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.apache.nifi.processors.daxoperation.utils.OCRUtil;
import org.apache.nifi.processors.daxoperation.utils.ServiceUtil;
import org.apache.nifi.processors.daxoperation.utils.Utility;
import org.apache.nifi.stream.io.StreamUtils;
import org.json.JSONObject;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tags({"daxoperations", "smartscan", "mongo", "fetch"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class FetchSmartScanTextractObject extends AbstractProcessor {
    private static final String processorName = "FetchSmartScanTextractObject";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private ServiceUtil serviceUtil = null;
    private DateUtil dateUtil = null;
    private static Gson gson = null;

    private MongoClient mongoClient = null;
    protected MongoClientService mongoClientService;
    private SmartScanDao smartScanDao = null;
    private CarePersonaOCRDao carePersonaOCRDao = null;

    public String getProcessorName() {
        return FetchSmartScanTextractObject.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public ServiceUtil getServiceUtil() {
        if (this.serviceUtil == null)
            this.serviceUtil = new ServiceUtil();
        return this.serviceUtil;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public Gson getGson() {
        if (FetchSmartScanTextractObject.gson == null) {
            FetchSmartScanTextractObject.gson = GsonUtil.getGson();
        }
        return FetchSmartScanTextractObject.gson;
    }

    public void setServiceUtil(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public MongoClientService getMongoClientService() {
        return this.mongoClientService;
    }

    public void setMongoClientService(MongoClientService mongoClientService) {
        this.mongoClientService = mongoClientService;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();

    static final Relationship REL_HANDWRITTEN = new Relationship.Builder()
            .name("HANDWRITTEN PRESCRIPTIONS")
            .description("Marks process successful and all handwritten prescriptions are redirected to this relationship")
            .build();

    static final Relationship REL_PRINTTED = new Relationship.Builder()
            .name("PRINTED PRESCRIPTIONS")
            .description("Marks process successful and all printed prescriptions are redirected to this relationship")
            .build();

    static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("marks process failure when processor achieves failure condition.")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(MONGODB_CLIENT_SERVICE);
        this.relationships = Set.of(REL_HANDWRITTEN, REL_PRINTTED, REL_FAILURE);
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
        ObjectMapper oMapper = new ObjectMapper();
        long startTime = this.getDateUtil().getCurrentEpochInMillis();
        String collection = "";
        String objecID = "";
        String type = "recommendation";
        Map<String, Object> flowFileContent = new HashMap<>();

        try {
            logMetaData.put("processor_name", this.getProcessorName());

            if (inputFF == null)
                throw new Exception("Exception occurred while processing");

            String ffContent = readFlowFileContent(inputFF, session);
            JSONObject jsonContent = new JSONObject(ffContent);

            objecID = (String) jsonContent.get("id");
            collection = (String) jsonContent.get("collection");

            if (objecID == null) {
                throw new Exception("id not provided");
            }

            if (collection == null) {
                throw new Exception("collection not provided");
            }

            if (jsonContent.get("type") != null && jsonContent.get("type").toString().equals("topScores"))
                type = "topScores";

            logMetaData.put("objectID", objecID);
            logMetaData.put("collection", collection);

            MongoClient mongoClient = getMongoClientService().getMongoClient();
            setMongoClient(mongoClient);
            initializeDaos(mongoClient);

            Object dataObject = null;

            if (collection.equals("smartscan")) {
                dataObject = this.smartScanDao.getById(objecID);
            } else if (collection.equals("carepersonaocr")) {
                dataObject = this.carePersonaOCRDao.getById(objecID);
            } else {
                throw new Exception("Invalid collection details provided");
            }

            if (dataObject == null) {
                throw new Exception("Invalid id provided");
            }

            Map<String, Object> dMap = oMapper.convertValue(dataObject, Map.class);

            // Classifying prescription based on whole line
            // If atleast one word is handwritten then whole line is considered as handwritten
            List<String> allLineIds = new ArrayList<>();
            Map<String, Map<String, Object>> data = new HashMap<>();
            Map<String, Object> rawLineCoordinates = new HashMap<>();

            Map<String, Object> textractJson = getGson().fromJson((String) dMap.get("textract"), Map.class);
            List<Map<String, Object>> contentExtracted = oMapper.convertValue(textractJson.get("blocks"), List.class);

            for (Integer i = 0; i < contentExtracted.size(); i++) {
                TextractModel block = oMapper.convertValue(contentExtracted.get(i), TextractModel.class);
                Map<String, Object> blockData = new HashMap<>();
                String id = block.id;
                String blockType = block.blockType;

                if (blockType != null && blockType.equals("LINE")) {
                    allLineIds.add(id);
                    blockData.put("CHILD", (List) block.relationships.get(0).get("ids"));
                    rawLineCoordinates.put(block.text, (Map) block.geometry.get("boundingBox"));
                }
                blockData.put("TYPE", block.textType);
                blockData.put("TEXT", block.text);
                data.put(id, blockData);
            }

            OCRUtil ocrUtil = new OCRUtil();
            Integer linesHandwrittenCount = ocrUtil.getHandWrittenLinesCount(allLineIds, data);

            String textType = "PRINTED";
            if (linesHandwrittenCount > 2) {
                textType = "HANDWRITING";
            }

            flowFileContent.put("rawLineCoordinates", rawLineCoordinates);
            flowFileContent.put("fileName", objecID);
            flowFileContent.put("OCRDATA", contentExtracted);
            flowFileContent.put("requestType", null);
            flowFileContent.put("documentType", textType);
            flowFileContent.put("type", type);

            inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(flowFileContent));
            session.putAttribute(inputFF, "fileName", objecID);

            if (textType.equals("PRINTED")) {
                session.transfer(inputFF, REL_PRINTTED);
            } else {
                session.transfer(inputFF, REL_HANDWRITTEN);
            }

            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Extracted Mongo ObjectId: %s", objecID), Level.INFO, null);
            log_site_processing_time(startTime, objecID, logMetaData);
        } catch (Exception ex) {
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }
    }

    private void log_site_processing_time(long startTime, String objectID, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        long endTime = this.getDateUtil().getCurrentEpochInMillis();
        long duration = endTime - startTime;
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, objectID + ", execution_duration: " + duration, Level.INFO, otherDetails);
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    private void initializeDaos(MongoClient client) {
        this.smartScanDao = new SmartScanDao(client);
        this.carePersonaOCRDao = new CarePersonaOCRDao(client);
    }

    private FlowFile updateFlowFile(ProcessSession session, FlowFile ff, String data) {
        ff = session.write(ff, out -> out.write(data.getBytes(charset)));
        return ff;
    }
}

