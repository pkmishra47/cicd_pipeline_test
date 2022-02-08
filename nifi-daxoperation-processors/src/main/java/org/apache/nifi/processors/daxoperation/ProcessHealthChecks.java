package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class ProcessHealthChecks extends AbstractProcessor {
    private static final String processorName = "ProcessHealthChecks";
    private static final Charset charset = Charset.forName("UTF-8");
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    private static final PropertyDescriptor MONGO_CONNECTION_URL = new PropertyDescriptor
            .Builder().name("MONGO_CONNECTION_URL").displayName("MONGO CONNECTION URL").
            description("mongodb://{{AppServerUser}}:{{AppServerPassword}}@{{AppServer}}/admin")
            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private DBCollection dbFiles = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private NotificationDao notificationDao = null;
    private HealthCheckDao healthCheckDao = null;
    private SimpleDateFormat healthCheckDFormatDate = new SimpleDateFormat("dd-MM-yyyy");
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
        initDescriptors.add(MONGO_CONNECTION_URL);
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
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();

        String dbUrl = context.getProperty(MONGO_CONNECTION_URL).evaluateAttributeExpressions().getValue();
        long startTime = this.getDateUtil().getEpochTimeInSecond();
        try(MongoClient client = new MongoClient(new MongoClientURI(dbUrl))) {

            String ffContent = readFlowFileContent(flowFile, session);
            FFInput ffinput = gson.fromJson(ffContent, FFInput.class);

            siteDetails = ffinput.siteDetails;
            siteMasterMap = ffinput.siteMasterMap;
            siteStats = siteDetails.getStats();
//            MongoClient client = new MongoClient(new MongoClientURI(dbUrl));


            identityDao = new IdentityDao(client);
            userDao = new UserDao(client);
            healthCheckDao = new HealthCheckDao(client);
            notificationDao = new NotificationDao(client);


            DB appDb = client.getDB(siteDetails.getSiteDb());
            dbFiles = appDb.getCollection("record_group");

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("%s Health Checks: %s",siteDetails.getSiteName(), dbFiles.count() ), Level.INFO, null);

            try(DBCursor cur = dbFiles.find())	{

                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("cur has next " + cur.hasNext() ), Level.INFO, null);
                while (cur.hasNext()) {
                    DBObject dbOrig = cur.next();
                    DBObject dbFile = dbOrig;

                    String fileName = dbFile.get("fileName").toString();
                    //			log.info("Health Check File: {}", fileName);
//                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
//                            String.format("Health Check File: %s", fileName), Level.INFO, null);
                    String[] parts = fileName.split("_");
                    if(parts.length == 0)
                        throw new Exception("Unable to parse file Name: " + fileName);

                    String uhid = parts[0];
                    String siteId = getSiteKeyFromUhid(uhid);
                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);

                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                            String.format("uhid : %s, siteId : %s", uhid, siteId), Level.INFO, null);
                    if (dbUser == null) {
                        getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Health Checks: Unable to find UHID: " + uhid + " in site: " + siteId , Level.INFO, null);
					/*log.warn("Health Checks: Unable to find UHID: {} in site: {}", uhid, siteId);

					notificationDao.addNotification(dbUser, "HealthCheck Import - Unable to find Uhid  "+uhid ,
    						NotificationLevel.System,NotificationType.UhidNotFound,siteDetails.getSiteKey(),null, uhid);*/
                        continue; //This user is not yet imported.
                    }

                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                            String.format("User  found"), Level.INFO, null);
                    boolean found = false;
                    Date dHealthCheck = new Date();
                    if(parts.length == 3) {
                        dHealthCheck = healthCheckDFormatDate.parse(parts[2]);
                    } else if (parts.length == 5){
                        dHealthCheck = healthCheckDFormatDate.parse(String.format("%s-%s-%s",parts[2], parts[3], parts[4]));
                    } else {
                        throw new Exception ("Unable to get the HealthCheck date parsed: "+ fileName);
                    }

                    for(DBHealthCheck healthCheck : dbUser.getHealthChecks()) {
                        if(healthCheck.getHealthCheckDate().equals(dHealthCheck)) {
                            found = true;
                            break;
                        }
                    }

                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                            String.format("found" + found), Level.INFO, null);

                    if(!found) {
                        DBHealthCheck dbHealthCheck = new DBHealthCheck();
                        dbHealthCheck.setHealthCheckDate(dHealthCheck);
                        dbHealthCheck.setHealthCheckName(parts[1]);
                        dbHealthCheck.setSource(siteDetails.getSiteKey());

                        Attachment attachement = new Attachment();
                        attachement.setFileName(fileName);
                        attachement.setMimeType(dbFile.get("fileType").toString());
//                        attachement.setContent(Base64.decodeBase64(dbFile.ge));
                        attachement.setContent(Base64.decodeBase64(dbFile.get("content").toString()));

                        DBAttachement dbAttach = new DBUtil(client).writeDBAttachement(attachement);

                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                String.format("file attached from byte Arry "+ dbAttach.getFileAttached().toString()), Level.INFO, null);
                        dbHealthCheck.getHealthCheckFilesList().add(dbAttach);

                        healthCheckDao.save(dbHealthCheck);
                        dbUser.getHealthChecks().add(dbHealthCheck);
                        userDao.save(dbUser);

                        siteStats.setHealthChecksImported(siteStats.getHealthChecksImported()+1);
                        notificationDao.addNotification(dbUser, "HealthCheck -"+dbHealthCheck.getHealthCheckName()+" is getting imported",
                                NotificationLevel.System,NotificationType.HealthCheck,siteDetails.getSiteKey(),null, uhid);
                        //Send the SMS, inviting the user to Prism.

                        try {
                            long delayDays = getDaysBetweenTwoDates(dHealthCheck.getTime(),new Date().getTime());
                            if (delayDays <= 7 && siteDetails.isSms()) {
                                DBsms dbSms = new DBsms();
                                dbSms.setSmsPurpose("HEALTHCHECK-INTIMATION");
                                dbSms.setMobileNumber(dbUser.getMobileNumber());
                                dbSms.setDbUser(dbUser);
                                dbSms.setUhid(uhid);
                                dbSms.setSiteKey(siteDetails.getSiteKey());
                                dbSms.setSendAt(new Date());
                                SMSDao smsDao = new SMSDao(client);
                                smsDao.save(dbSms);
                            }
                        }
                        catch(Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                                    Utility.stringifyException(e), Level.INFO, null);
//                            log.error("PHR | OldProcessorJob | Sending Health Check Intimation SMS | Error ",e);
                        }
                    }

                    dbFiles.remove(dbOrig);
                }
            }

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("%s Health Checks Added: %s", siteDetails.getSiteName(), siteStats.getHealthChecksImported()), Level.INFO, null);

            long endTime = this.getDateUtil().getEpochTimeInSecond();

            log_site_processing_time(endTime - startTime, siteStats.getHealthChecksImported(), logMetaData);
            FlowFile output = getFlowFile(session, flowFile, ffContent);
            session.transfer(output, REL_SUCCESS);

        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.INFO, null);

            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
    }

    private void log_site_processing_time(Long duration, int totalProcessed, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("site_name", siteDetails.getSiteName());
        otherDetails.put("site_key", siteDetails.getSiteKey());
        otherDetails.put("total_processed", totalProcessed);
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, siteDetails.getSiteName() + " processed successfully.", Level.INFO, otherDetails);
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
            System.out.println("getSiteKeyFromUhid "+ Arrays.asList(parts));
            if (parts.length > 2) {
                System.out.println("Imporper UHID: "+ uhid+", unable to split it.");
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
}