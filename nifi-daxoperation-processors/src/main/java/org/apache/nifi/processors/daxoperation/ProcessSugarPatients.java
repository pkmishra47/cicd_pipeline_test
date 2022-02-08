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
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.dao.*;
import org.apache.nifi.processors.daxoperation.dbo.DBEntity;
import org.apache.nifi.processors.daxoperation.dbo.DBSugarInfo;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class ProcessSugarPatients extends AbstractProcessor {
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder().name("Database Connection Pooling Service")
            .displayName("Database Connection Pooling Service").description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class).build();
    private static final String processorName = "ProcessSugarPatients";
    private static final Charset charset = Charset.forName("UTF-8");
//    private static final PropertyDescriptor MONGO_CONNECTION_URL = new PropertyDescriptor
//            .Builder().name("MONGO_CONNECTION_URL").displayName("MONGO CONNECTION URL").
//            description("mongodb://{{AppServerUser}}:{{AppServerPassword}}@{{AppServer}}/admin")
//            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
//            .build();

    public static final PropertyDescriptor MONGODB_CLIENT_SERVICE = new PropertyDescriptor
            .Builder()
            .name("MongodbService")
            .displayName("MONGODB_CLIENT_Service")
            .description("provide reference to MongoDB controller Service.")
            .required(true)
            .identifiesControllerService(MongoClientService.class)
            .build();


    private SiteDetails siteDetails = null;
    private SiteStats siteStats = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private DBCPService dbcpService;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private DBCollection dbSugarPatients = null;
    private IdentityDao identityDao = null;
    private UserDao userDao = null;
    private HospitalizationDao hospitalizationDao = null;
    private PrescriptionDao prescriptionDao = null;
    private NotificationDao notificationDao = null;
    private BillDao billDao = null;
    private BillDownloadDao billDownloadDao = null;
    private SugarInfoDao sugarDao = null;
    private EntityDao entityDao = null;
    private SimpleDateFormat dateformat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat healthCheckDFormatDate = new SimpleDateFormat("dd-MM-yyyy");
    protected MongoClientService mongoClientService;
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
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();

//        String dbUrl = context.getProperty(MONGO_CONNECTION_URL).evaluateAttributeExpressions().getValue();

        try {
            this.mongoClientService = context.getProperty(MONGODB_CLIENT_SERVICE).asControllerService(MongoClientService.class);;
            String ffContent = readFlowFileContent(flowFile, session);
            FFInput ffinput = gson.fromJson(ffContent, FFInput.class);

            siteDetails = ffinput.siteDetails;
            siteMasterMap = ffinput.siteMasterMap;
            siteStats = siteDetails.getStats();
            MongoClient client = this.mongoClientService.getMongoClient();

            identityDao = new IdentityDao(client);
            userDao = new UserDao(client);
            sugarDao = new SugarInfoDao(client);
            entityDao = new EntityDao(client);


            DB appDb = client.getDB(siteDetails.getSiteDb());
            dbSugarPatients = appDb.getCollection("sugar");


            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("%s Sugar Patients %s",siteDetails.getSiteName(), dbSugarPatients.count() ), Level.INFO, null);

            try(DBCursor cur = dbSugarPatients.find())	{
                while (cur.hasNext()) {
                    DBObject dbOrig = cur.next();
                    DBObject dbPatient = dbOrig;
//                    if(siteDetails.isDebug())
//                        log.info("Processing Sugar Patient with Id: {}", dbPatient.get("_id"));

                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                            String.format("Processing Sugar Patient with Id: %s", dbPatient.get("_id")), Level.INFO, null);
                    String uhid = dbPatient.get("UHID").toString();
                    String siteId = getSiteKeyFromUhid(uhid);

                    if (siteId == null || siteId.trim().length() == 0) {
//                        log.error("PHR | DataMan | oldProcesorJob | processSugarPatient | SiteName "+siteDetails.getSiteName()+" Uhid "+uhid+" Site Id is Null ");
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                String.format("PHR | DataMan | oldProcesorJob | processTools | SiteName "+
                                        siteDetails.getSiteName()+" Uhid "+uhid+" Site Id is Null "), Level.INFO,
                                null);
                        dbSugarPatients.remove(dbOrig);
                        continue;
                        //throw new Exception("SiteId is null");
                    }

                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);

                    if (dbUser != null) {
//                        if(siteDetails.isDebug())
//                            log.info("Processing user with UHID {} for site {}", uhid, siteDetails.getSiteName());

                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                String.format("Processing user with UHID %s for site %s",
                                        uhid, siteDetails.getSiteName()), Level.INFO, null);
                        Date billingDate = null;
                        try {
                            billingDate = dateformat.parse(getDateWithoutGMT(dbPatient.get("BILLINGDATE").toString()));
                        } catch (Exception e) {
//                            log.error("Could not parse billing date {} for UHID {}", dbPatient.get("BILLINGDATE"), uhid);
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                    String.format("Could not parse billing date %s for UHID %s",
                                            dbPatient.get("BILLINGDATE"), uhid), Level.ERROR, null);
                            dbSugarPatients.remove(dbOrig);
                            continue;
                        }


                        DBSugarInfo dbSugarInfo = new DBSugarInfo();
                        List<DBSugarInfo> dbSugarInfoList = null;
                        boolean skipSms = false;
                        if(dbUser.getSugarInfo() == null) {
                            dbSugarInfoList = new ArrayList<DBSugarInfo>();
                            dbUser.setSugarInfo(dbSugarInfoList);

                        } else {
                            //	dbSugarInfoList = dbUser.getSugarInfo();
                            skipSms=true;
                        }
                        boolean isNotExist = true;
                        dbSugarInfo.setBillingDate(billingDate);
                        Object packageId = dbPatient.get("PACKAGEID");
                        if(packageId != null)
                            dbSugarInfo.setPacakgeId(packageId.toString());
                        Object packageName = dbPatient.get("PACKAGENAME");
                        if(packageName != null)
                            dbSugarInfo.setPackageName(packageName.toString());
                        Object treatingPhysician = dbPatient.get("treatingPhysician");
                        if(treatingPhysician != null)
                            dbSugarInfo.setTreatingPhysician(treatingPhysician.toString());
                        Object prefferedLanguage = dbPatient.get("preferredLanguage");
                        if(prefferedLanguage != null)
                            dbSugarInfo.setPreferredLanguage(prefferedLanguage.toString());

                        boolean isPackage = true;
                        Object dbFlag = dbPatient.get("FLAG");
                        if(dbFlag != null && dbFlag.toString().equals("NON-PACKAGE"))
                            isPackage = false;

                        if(isPackage)
                            dbSugarInfo.setPacakgeId("PKG." +dbSugarInfo.getPacakgeId());

                        dbSugarInfo.setDbUser(dbUser); // Adding User DbRef to sugar for useridmerge

                        dbUser.getEntitys().clear();
                        dbUser.getEntitys().add("Sugar");

                        if(isNotExist){
//                            log.info("PHR | DataMan | oldProcesorJob | processSugarPatient | Adding to the sugar list to patient with {}", uhid);
                            //dbSugarInfoList.add(dbSugarInfo);
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                    String.format("PHR | DataMan | oldProcesorJob | processSugarPatient | " +
                                                    "Adding to the sugar list to patient with  %s",
                                            uhid), Level.INFO, null);
                            dbUser.getSugarInfo().add(dbSugarInfo);
                        }

                        if(!dbUser.getRoles().contains("sugar")) {
                            if(dbSugarInfo.getPacakgeId() != null && !dbSugarInfo.getPacakgeId().isEmpty()) {
                                dbUser.getRoles().remove("sugarsimple");  //We should remove if exists
                                dbUser.getRoles().add("sugar");
                            }
                            else
                                dbUser.getRoles().add("sugarsimple");
                        }

                        dbUser.setUpdatedAt(new Date());
                        sugarDao.save(dbSugarInfo);
                        userDao.save(dbUser);
                        siteStats.setSugarPatientsImported(siteStats.getSugarPatientsImported() + 1);

                        // Once processed we remove this record.
                        dbSugarPatients.remove(dbOrig);

                        try {
                            if(!skipSms && siteDetails.isSms()){
                                DBsms dbSms = new DBsms();
                                dbSms.setSmsPurpose("SUGARINVITATION");
                                dbSms.setMobileNumber(dbUser.getMobileNumber());
                                dbSms.setDbUser(dbUser);
                                dbSms.setUhid(uhid);
                                dbSms.setSiteKey(siteDetails.getSiteKey());
                                dbSms.setSendAt(new Date());
                                SMSDao smsDao = new SMSDao(client);
                                Object smsId = smsDao.save(dbSms).getId();
                                //config.getSMSService().sendInvitation(user.getMobileNumber(), String.valueOf(smsId));


                                DBEntity dbEntity = entityDao.getEntity("Sugar");
                                if(dbEntity.getSmsInvitationTwo() != null) {
                                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                            String.format("Second Invitation SMS | Process SugarPatients"), Level.INFO, null);
                                    DBsms dbSmsTwo = new DBsms();
                                    dbSmsTwo.setSmsPurpose("INVITATION-TWO");
                                    dbSmsTwo.setMobileNumber(dbUser.getMobileNumber());
                                    dbSmsTwo.setDbUser(dbUser);
                                    dbSmsTwo.setUhid(uhid);
                                    dbSmsTwo.setSiteKey(siteDetails.getSiteKey());
                                    dbSmsTwo.setEntityName("Sugar");

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(new Date());
                                    cal.add(Calendar.DATE, dbEntity.getSmsDelayDays());
                                    Date nextDate = cal.getTime();
                                    dbSmsTwo.setSendAt(nextDate);
                                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                            String.format("Second Invitation SMS | Process SugarPatients  | Next Date %s",
                                                    nextDate), Level.INFO, null);
                                    SMSDao smsDaoTwo = new SMSDao(client);
                                    smsDaoTwo.save(dbSmsTwo).getId();
                                }
                            }
                        } catch(Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                    String.format("PHR | OldProcessorJob | Sending Invitation SMS | Error %s",
                                            Utility.stringifyException(e)), Level.INFO, null);
                        }

                        NotificationDao notificationDao = new NotificationDao(client);
                        notificationDao.addNotification(dbUser, "UHID - "+uhid+" from "+siteDetails.getSiteName()+" - Imported Sugar Patient",
                                NotificationLevel.System, NotificationType.Activation,siteId,null, uhid);
                    } else {
                        if(siteDetails.isDebug())
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                                    String.format("Sugar User does not Exists uhid: %s and siteKey: %s",
                                            uhid, siteDetails.getSiteKey()), Level.INFO, null);
                    }

                }
            }
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

    protected String getSiteNameFromLocId(String locId) {

        SiteDetails uhidSite = getSiteNameOnLocId(locId);
        if(uhidSite == null) {
            if(siteDetails.isDebug())
                System.out.println("Could not find the site for Location Id: "+ locId);
            return null;
        }
        return uhidSite.getSiteName();

    }
    public SiteDetails getSiteNameOnLocId(String locId) {
        for(String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if(siteDetails.getLocationId() != null &&
                    siteDetails.getLocationId().equals(locId))
                return siteDetails;
        }

        return null;
    }
}