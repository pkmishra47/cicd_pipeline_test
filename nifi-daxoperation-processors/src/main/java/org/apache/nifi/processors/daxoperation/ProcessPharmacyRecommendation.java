package org.apache.nifi.processors.daxoperation;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.MongoClientService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processors.daxoperation.dao.PharmacyRecommendationsDao;
import org.apache.nifi.processors.daxoperation.dbo.DBPharmacyRecommendation;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.PharmacyRecommendation;
import org.apache.nifi.processors.daxoperation.dm.SKURecommended;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "operations", "dataman", "pharmacy", "recommendation"})
@CapabilityDescription("")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class ProcessPharmacyRecommendation extends AbstractProcessor {

    private static final String processorName = "ProcessPharmacyRecommendation";

    private static final Charset charset = Charset.forName("UTF-8");
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

    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    protected MongoClientService mongoClientService;
    private MongoClient mongoClient = null;
    private PharmacyRecommendationsDao pharmacyRecommendationsDao = null;

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
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        ObjectMapper mapper = new ObjectMapper();
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        List<PharmacyRecommendation> failedPharmacyRecommendationList = new ArrayList<>();

        try {
            if (inputFF == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            ffinput = gson.fromJson(ffContent, FFInput.class);

            MongoClient mongoClient = getMongoClientService().getMongoClient();
            setMongoClient(mongoClient);

            List<PharmacyRecommendation> pharmacyRecommendationsList = ffinput.pharmacyRecommendations;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            initializeDaos(mongoClient);

            totalProcessed = pharmacyRecommendationsList.size();

            while (!pharmacyRecommendationsList.isEmpty()) {
                PharmacyRecommendation inputPharmacyRecommendation = pharmacyRecommendationsList.get(0);

                try {
                    String mobileno = "";
                    if (inputPharmacyRecommendation.getMOBILENO() != null & !inputPharmacyRecommendation.getMOBILENO().equals("")) {
                        mobileno = inputPharmacyRecommendation.getMOBILENO();
                    } else {
                       continue; 
                    }

                    logMetaData.put("mobileNo", mobileno);

                    List<String> skuIDs = new ArrayList<>();

                    if (inputPharmacyRecommendation.getITEMS() != null) {
                        List items = gson.fromJson(inputPharmacyRecommendation.getITEMS(), List.class);
                        for (Object recommended : items) {
                            SKURecommended item = mapper.convertValue(recommended, SKURecommended.class);
                            skuIDs.add(item.itemid);
                        }
                    }

                    // if (skuIDs.size()==0) {
                    //     continue;
                    // }

                    DBPharmacyRecommendation dbPharmacyRecommendation = this.pharmacyRecommendationsDao.findRecommendationsByMobileno(mobileno);

                    if (dbPharmacyRecommendation == null) {
                        dbPharmacyRecommendation = new DBPharmacyRecommendation();
                        dbPharmacyRecommendation.setMobileNumber(mobileno);
                    }

                    dbPharmacyRecommendation.setPharmacyItems(skuIDs);
                    this.pharmacyRecommendationsDao.save(dbPharmacyRecommendation);

                    successCount++;
                    pharmacyRecommendationsList.remove(inputPharmacyRecommendation);

                } catch (ConcurrentModificationException curEx) {
                    failedPharmacyRecommendationList.add(inputPharmacyRecommendation);
                    pharmacyRecommendationsList.remove(inputPharmacyRecommendation);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (DuplicateKeyException dupEx) {
                    failedPharmacyRecommendationList.add(inputPharmacyRecommendation);
                    pharmacyRecommendationsList.remove(inputPharmacyRecommendation);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(dupEx), Level.ERROR, null);
                } catch (Exception ex) {
                    failedPharmacyRecommendationList.add(inputPharmacyRecommendation);
                    pharmacyRecommendationsList.remove(inputPharmacyRecommendation);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            logMetaData.put("totalProcessed", totalProcessed);
            logMetaData.put("successCount", successCount);
            logMetaData.put("failedCount", totalProcessed-successCount);

            if (!failedPharmacyRecommendationList.isEmpty()) {
                ffinput.pharmacyRecommendations = failedPharmacyRecommendationList;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.pharmacyRecommendations = pharmacyRecommendationsList;

        } catch (Exception ex) {
            setFlowFileDate(inputFF, session);
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }

        String flowFileContent = gson.toJson(ffinput);
        session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
        session.transfer(inputFF, REL_SUCCESS);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
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

    private void initializeDaos(MongoClient client) {
        this.pharmacyRecommendationsDao = new PharmacyRecommendationsDao(client);
    }
}