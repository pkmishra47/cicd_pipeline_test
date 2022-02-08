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
import org.apache.nifi.processors.daxoperation.dao.DietPlanDao;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.DBDietPlan;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.dm.DietPlan;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProcessDietPlans extends AbstractProcessor {
    private static final String processorName = "ProcessDietPlans";
    private static final Charset charset = Charset.forName("UTF-8");
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


    private SiteDetails siteDetails = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private DietPlanDao dietPlanDao = null;
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile inputFF = session.get();
        FFInput ffinput = new FFInput();
        int totalProcessed = 0, successCount = 0;
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        List<DietPlan> failedDietPlanlist = new ArrayList<>();

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

            List<DietPlan> dietPlanList = ffinput.dietPlan;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("%s Diet Plans : %s", siteDetails.getSiteName(), dietPlanList.size()), Level.INFO, null);

            totalProcessed = dietPlanList.size();
            while (!dietPlanList.isEmpty()) {
                DietPlan dbCore = dietPlanList.get(0);

                try {
                    String uhid = dbCore.getUHID();
                    String siteId = getSiteKeyFromUhid(uhid);
                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);
                    if (dbUser == null) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("DietPlan: Unable to find UHID: %s in site: %s ", uhid, siteId), Level.INFO, null);
                        failedDietPlanlist.add(dbCore);
                        dietPlanList.remove(dbCore);
                        continue;
                    }

                    if (dbCore.getFIRSTROWVALUE() != null && dbCore.getROWVALUE() != null) {
                        DBDietPlan dbDietPlan = dbUser.getDietPlan();
                        if (dbDietPlan == null) {
                            dbDietPlan = new DBDietPlan();
                            dbUser.setDietPlan(dbDietPlan);
                            dietPlanDao.save(dbDietPlan);
                            userDao.save(dbUser);
                        }

                        Object food = dbCore.getFIRSTROWVALUE();
                        Object value = dbCore.getROWVALUE();
                        if (food != null && value != null) {
                            dbDietPlan.getFooditems().put(food.toString().replace(".", ":"), value.toString());
                            dietPlanDao.save(dbDietPlan);
                            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("dietplan : Saved diet plan food: " + food.toString() + " ,value: " + value.toString() + " for Uhid %s", uhid), Level.INFO, null);
                        }
                    } else {
                        if (dbCore.getPARAMETER_NAME() == null) {
                            dietPlanList.remove(dbCore);
                            continue;
                        }

                        DBDietPlan dbDietPlan = dbUser.getDietPlan();
                        if (dbDietPlan == null) {
                            dbDietPlan = new DBDietPlan();
                            try {
                                dbDietPlan.setUhid(uhid);
                                dbDietPlan.setDate(dateformat.parse(getDateWithoutGMT(dbCore.getCREATED_AT())));
                            } catch (Exception exception) {
                                this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, String.format("DietPlan: Could not parse CREATED_AT %s for UHID %s", dbCore.getCREATED_AT(), uhid), Level.INFO, null);
                            }

                            dbUser.setDietPlan(dbDietPlan);
                            dietPlanDao.save(dbDietPlan);
                            userDao.save(dbUser);
                        }

                        String paramName = dbCore.getPARAMETER_NAME();
                        String value = null;
                        if (dbCore.getPARAMETER_VAL() != null)
                            value = dbCore.getPARAMETER_VAL();
                        else if (dbCore.getPARAMETER_DETIALVAL() != null)
                            value = dbCore.getPARAMETER_DETIALVAL();
                        if (value == null) {
                            dietPlanList.remove(dbCore);
                            continue;
                        }
                        if (paramName.equals("AGE"))
                            dbDietPlan.setAge(value);
                        else if (paramName.equals("DIETICIAN"))
                            dbDietPlan.setDietician(value);
                        else if (paramName.equals("COMMENTS"))
                            dbDietPlan.setComments(value);
                        else if (paramName.equals("DIAGNOSIS"))
                            dbDietPlan.setDiagnosis(value);
                        else if (paramName.equals("PROTEIN"))
                            dbDietPlan.setProtein(value);
                        else if (paramName.equals("FAT"))
                            dbDietPlan.setFat(value);
                        else if (paramName.equals("ENERGY"))
                            dbDietPlan.setEnergy(value);
                        else if (paramName.equals("CARBOHYDRATES"))
                            dbDietPlan.setCarbohydrates(value);
                        else if (paramName.equals("BMI"))
                            dbDietPlan.setBmi(value);
                        else if (paramName.equals("CONSULTANT"))
                            dbDietPlan.setConsultant(value);
                        else if (paramName.equals("WT")) {
                            String w = dbDietPlan.getWeight();
                            if (w == null) w = "";
                            String[] parts = w.split(" ");
                            if (parts.length == 2)
                                dbDietPlan.setWeight(value + " " + parts[1]);
                            else
                                dbDietPlan.setWeight(value);
                        } else if (paramName.equals("WEIGHT")) {
                            String w = dbDietPlan.getWeight();
                            if (w == null) w = "";
                            String[] parts = w.split(" ");
                            if (parts.length == 2)
                                dbDietPlan.setWeight(parts[0] + " " + value);
                            else
                                dbDietPlan.setWeight(" " + value);
                        } else if (paramName.equals("HT")) {
                            String h = dbDietPlan.getHeight();
                            if (h == null) h = "";
                            String[] parts = h.split(" ");
                            if (parts.length == 2)
                                dbDietPlan.setHeight(value + " " + parts[1]);
                            else
                                dbDietPlan.setHeight(value);
                        } else if (paramName.equals("HEIGHT")) {
                            String h = dbDietPlan.getHeight();
                            if (h == null) h = "";
                            String[] parts = h.split(" ");
                            if (parts.length == 2)
                                dbDietPlan.setHeight(parts[0] + " " + value);
                            else
                                dbDietPlan.setHeight(" " + value);
                        }

                        dietPlanDao.save(dbDietPlan);
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("dietplan : Saved diet plan Data for Uhid %s", uhid), Level.INFO, null);
                    }
                    dietPlanList.remove(dbCore);
                    successCount++;
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));
                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedDietPlanlist.add(dbCore);
                    dietPlanList.remove(dbCore);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedDietPlanlist.add(dbCore);
                    dietPlanList.remove(dbCore);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedDietPlanlist.isEmpty()) {
                ffinput.dietPlan = failedDietPlanlist;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.dietPlan = dietPlanList;
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
                return null;
            }
            SiteDetails uhidSite = getSiteBasedOnPrefix(parts[0]);
            if (uhidSite == null) {
                if (siteDetails.isDebug()) {

                }
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

    private void initializeDaos(MongoClient client) {
        this.identityDao = new IdentityDao(client);
        this.userDao = new UserDao(client);
        this.dietPlanDao = new DietPlanDao(client);
    }
}