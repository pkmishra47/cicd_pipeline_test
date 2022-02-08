package org.apache.nifi.processors.daxoperation;

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
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.bo.ToolData;
import org.apache.nifi.processors.daxoperation.bo.ToolParameters;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.ResourceMasterDao;
import org.apache.nifi.processors.daxoperation.dao.ToolsDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.DBTool;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
import org.apache.nifi.processors.daxoperation.dm.Tool;
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

public class ProcessTools extends AbstractProcessor {
    private static final String processorName = "ProcessTools";
    private static final Charset charset = StandardCharsets.UTF_8;
    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();

    private SiteStats siteStats = null;
    private Map<String, SiteDetails> siteMasterMap = null;
    private SiteDetails siteDetails = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private ToolsDao toolsDao = null;
    private ResourceMasterDao resourceMasterDao = null;
    protected MongoClientService mongoClientService;
    private MongoClient mongoClient = null;

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

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
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

    public SiteStats getSiteStats() {
        return siteStats;
    }

    public void setSiteStats(SiteStats siteStats) {
        this.siteStats = siteStats;
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
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        List<Tool> failedToolList = new ArrayList<>();

        try {

            if (inputFF == null) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            ffinput = gson.fromJson(ffContent, FFInput.class);

            MongoClient mongoClient = getMongoClientService().getMongoClient();
            setMongoClient(mongoClient);

            setSiteMasterMap(SiteMaster.loadSiteMasterMap(mongoClient, logMetaData, this.getLogUtil()));
            SiteDetails siteDetails = getSiteDetailsForSiteApiKey(ffinput.siteApiKey);
            this.setSiteDetails(siteDetails);
            if (siteDetails == null)
                throw new Exception("invalid siteDetails for " + ffinput.siteApiKey);

            List<Tool> toolList = ffinput.tool;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            totalProcessed = toolList.size();
            while (!toolList.isEmpty()) {
                Tool dbTool = toolList.get(0);
                Map<String, String> vitals = new HashMap<String, String>();

                try {
                    if (dbTool.getTool_data() != null)
                        dbTool = dbTool.getTool_data();

                    String uhid = dbTool.getUhid();
                    logMetaData.put(Constants.UHID, uhid);
                    String temperature = null;
                    if (dbTool.getTemperature() != null) {
                        temperature = dbTool.getTemperature();
                        vitals.put("Temperature", temperature);
                    }

                    String pulse = null;
                    if (dbTool.getPulse() != null) {
                        pulse = dbTool.getPulse();
                        vitals.put("Resting Heart Rate", pulse);
                    }

                    String bp = null;
                    if (dbTool.getBp() != null) {
                        bp = dbTool.getBp();
                        vitals.put("Blood Pressure", bp);
                    }

                    String height = null;
                    if (dbTool.getHeight() != null) {
                        height = dbTool.getHeight();
                        vitals.put("height", height);
                    }

                    String weight = null;
                    if (dbTool.getWeight() != null) {
                        weight = dbTool.getWeight();
                        vitals.put("weight", weight);
                    }

                    String bmi = null;
                    if (dbTool.getBmi() != null) {
                        bmi = dbTool.getBmi();
                        vitals.put("Bmi", bmi);
                    }

                    String spo2 = null;
                    if (dbTool.getSpo2() != null) {
                        spo2 = dbTool.getSpo2();
                        vitals.put("Pulse OxyMeter", spo2);
                    }

                    String siteId = getSiteKeyFromUhid(uhid);

                    if (siteId == null || siteId.trim().length() == 0) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("PHR | DataMan | oldProcesorJob | processTools | SiteName " + siteDetails.getSiteName() + " Uhid " + uhid + " Site Id is Null "), Level.INFO, null);
                        toolList.remove(dbTool);
                        continue;
                    }

                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                    if (dbUser == null) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Tools: Unable to find UHID: %s in site: %s", uhid, siteId), Level.INFO, null);
                        failedToolList.add(dbTool);
                        toolList.remove(dbTool);
                        continue; //This user is not yet imported.
                    }

                    for (Map.Entry<String, String> entry : vitals.entrySet()) {
                        ToolData toolData = new ToolData();

                        if (entry.getValue() == null)
                            continue;
                        DBTool Tool = dbUser.getTool(entry.getKey());
                        List<ToolParameters> toolParameterList = new ArrayList<ToolParameters>();
                        String toolName = entry.getKey();
                        if (toolName.equals("Blood Pressure")) {
                            toolData.setId(null);
                            ToolParameters toolParameter = new ToolParameters();
                            toolParameter.setTestName("Systolic");
                            if (entry.getValue().length() < 2)
                                continue;
                            String values[] = entry.getValue().split("/");
                            toolParameter.setTestValue(values[0]);
                            toolParameter.setUnits("mmHG");
                            toolParameter.setParamType("input");
                            toolParameterList.add(toolParameter);

                            ToolParameters toolParameterd = new ToolParameters();
                            toolParameterd.setTestName("Diastolic");
                            toolParameterd.setTestValue(values[1]);
                            toolParameterd.setUnits("mmHG");
                            toolParameterd.setParamType("input");
                            toolParameterList.add(toolParameterd);
                            toolData.setToolParameters(toolParameterList);
                            toolData.setSource("Blood Pressure");
                            Date date = new Date();
                            toolData.setDateAndTime(date);
                            toolData.setCommentsList(null);
                        }

                        if (toolName.equals("Temperature")) {
                            toolData.setId(null);
                            ToolParameters toolParameter = new ToolParameters();
                            toolParameter.setTestName("Temperature");
                            float temp = Float.valueOf(entry.getValue());
                            float tempc = ((temp - 32) * 5) / 9;
                            toolParameter.setTestValue(Float.toString(tempc));
                            toolParameter.setUnits("celsius");
                            toolParameter.setParamType("input");
                            toolParameterList.add(toolParameter);
                            toolData.setToolParameters(toolParameterList);

                            toolData.setSource("Temperature");
                            Date date = new Date();
                            toolData.setDateAndTime(date);
                            toolData.setCommentsList(null);
                        }

                        if (toolName.equals("Resting Heart Rate")) {
                            toolData.setId(null);
                            ToolParameters toolParameter = new ToolParameters();
                            toolParameter.setTestName("Beats per minute");
                            toolParameter.setTestValue(entry.getValue());
                            toolParameter.setUnits(null);
                            toolParameter.setParamType("input");
                            toolParameterList.add(toolParameter);
                            toolData.setToolParameters(toolParameterList);

                            toolData.setSource("Resting Heart Rate");
                            Date date = new Date();
                            toolData.setDateAndTime(date);
                            toolData.setCommentsList(null);
                        }

                        if (toolName.equals("Bmi")) {
                            toolData.setId(null);
                            ToolParameters toolParameter = new ToolParameters();
                            toolParameter.setTestName("Weight");
                            toolParameter.setTestValue(vitals.get("weight"));
                            toolParameter.setUnits("kg");
                            toolParameter.setParamType("input");
                            toolParameterList.add(toolParameter);

                            ToolParameters toolParameterHt = new ToolParameters();
                            toolParameterHt.setTestName("Height");
                            toolParameterHt.setTestValue(vitals.get("height"));
                            toolParameterHt.setUnits("cm");
                            toolParameterHt.setParamType("input");
                            toolParameterList.add(toolParameterHt);

                            ToolParameters toolParameterBmi = new ToolParameters();
                            toolParameterBmi.setTestName("Bmi");
                            toolParameterBmi.setTestValue(entry.getValue());
                            toolParameterBmi.setParamType("result");
                            toolParameterList.add(toolParameterBmi);
                            toolData.setToolParameters(toolParameterList);
                            toolData.setSource("Bmi");
                            Date date = new Date();
                            toolData.setDateAndTime(date);
                            toolData.setCommentsList(null);
                        }

                        if (toolName.equals("Pulse OxyMeter")) {
                            toolData.setId(null);
                            ToolParameters toolParameter = new ToolParameters();
                            toolParameter.setTestName("Beats per minute");
                            toolParameter.setTestValue(vitals.get("Resting Heart Rate"));
                            toolParameter.setUnits("bpm");
                            toolParameter.setParamType("input");
                            toolParameterList.add(toolParameter);

                            ToolParameters toolParameterHt = new ToolParameters();
                            toolParameterHt.setTestName("Oxygen Level");
                            toolParameterHt.setTestValue(entry.getValue());
                            toolParameterHt.setUnits("%");
                            toolParameterHt.setParamType("input");
                            toolParameterList.add(toolParameterHt);

                            toolData.setToolParameters(toolParameterList);
                            toolData.setSource("Pulse OxyMeter");
                            Date date = new Date();
                            toolData.setDateAndTime(date);
                            toolData.setCommentsList(null);
                        }

                        DBTool udbTool = dbUser.getTool(toolName);
                        if (udbTool == null) {
                            List<String> tools = resourceMasterDao.getResourceDetails("Tool");
                            Collections.sort(tools);

                            boolean found = Collections.binarySearch(tools, toolName) >= 0;
                            if (!found)
                                continue;

                            Tool = new DBTool();
                            Tool.setToolName(entry.getKey());
                            Tool.setCreationDate(new Date());
                            Tool.activate();
                            dbUser.addTool(toolName, Tool);
                            toolsDao.save(Tool);
                            userDao.save(dbUser);
                        }

                        ToolData newToolData = Tool.saveData(toolName, toolData);
                        toolsDao.save(Tool);
                    }

                    successCount++;
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                    toolList.remove(dbTool);
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedToolList.add(dbTool);
                    toolList.remove(dbTool);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedToolList.add(dbTool);
                    toolList.remove(dbTool);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedToolList.isEmpty()) {
                ffinput.tool = failedToolList;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.tool = toolList;

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

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    private void setFlowFileDate(FlowFile inputFF, ProcessSession session) {
        String createdEpoch = inputFF.getAttribute(FlowFileAttributes.CREATED_EPOCH);
        if (createdEpoch == null)
            session.putAttribute(inputFF, FlowFileAttributes.CREATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
        session.putAttribute(inputFF, FlowFileAttributes.UPDATED_EPOCH, this.getDateUtil().getCurrentEpochInMillis().toString());
    }

    private void log_site_processing_time(Long duration, int totalProcessed, int successCount, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("site_name", this.siteDetails.getSiteName());
        otherDetails.put("site_key", this.siteDetails.getSiteKey());
        otherDetails.put("total_processed", totalProcessed);
        otherDetails.put("success_count", successCount);
        otherDetails.put("failed_count", totalProcessed - successCount);
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, this.siteDetails.getSiteName() + " processed successfully.", Level.INFO, otherDetails);
    }

    private SiteDetails getSiteDetailsForSiteApiKey(String siteApiKey) {
        if (this.getSiteMasterMap().get(siteApiKey) != null)
            return this.getSiteMasterMap().get(siteApiKey);
        return null;
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
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.toolsDao = new ToolsDao(client);
        this.resourceMasterDao = new ResourceMasterDao(client);
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }

    protected String getSiteNameFromUhid(String uhid) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                if (siteDetails.isDebug()) {
//                    log.error("Could not find the site for UHID: {}", uhid);
                }

                return null;
            }
            return uhidSite.getSiteName();
        }

        return siteDetails.getSiteName();
    }

    String getSiteKeyFromUhid(String uhid) {
        if (uhid.indexOf('.') != -1) {
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
//                log.error("Imporper UHID: {}, unable to split it.", uhid);
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                if (siteDetails.isDebug()) {

                }
//                    log.error("Could not find the site for UHID: {}", uhid);
                return null;
            }
            return uhidSite.getSiteKey();
        }

        return siteDetails.getSiteKey();
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

    long getDaysBetweenTwoDates(long firstDate, long secondDate) {
        Calendar firstCalendar = Calendar.getInstance();
        firstCalendar.setTime(new Date(firstDate));

        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTime(new Date(secondDate));

        long diffTime = secondCalendar.getTimeInMillis() - firstCalendar.getTimeInMillis();
        long diffDays = diffTime / (24 * 60 * 60 * 1000);

        return diffDays;
    }

    protected String getDateWithoutGMT(String dt) {
        int pos = dt.indexOf('T');
        if (pos == -1) {
            dt = dt.replace(' ', 'T');
        }

        pos = dt.indexOf('+');
        if (pos != -1)
            dt = dt.substring(0, dt.indexOf('+'));

        if (dt.endsWith(".0"))
            dt = dt.substring(0, dt.length() - 2);

        //remove milliseconds
        pos = dt.indexOf('.');
        if (pos != -1)
            dt = dt.substring(0, dt.indexOf('.'));

        return (dt);
    }

    protected String getSiteNameFromLocId(String locId) {

        SiteDetails uhidSite = getSiteNameOnLocId(locId);
        if (uhidSite == null) {
            if (siteDetails.isDebug())
                System.out.println("Could not find the site for Location Id: " + locId);
            return null;
        }
        return uhidSite.getSiteName();

    }

    public SiteDetails getSiteNameOnLocId(String locId) {
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getLocationId() != null &&
                    siteDetails.getLocationId().equals(locId))
                return siteDetails;
        }

        return null;
    }
}