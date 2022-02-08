package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import com.mongodb.DB;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.GsonUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.util.*;

public class ProcessDischargeSummarysDep extends AbstractProcessor {
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder().name("Database Connection Pooling Service")
            .displayName("Database Connection Pooling Service").description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class).build();
    private static final String processorName = "ProcessDischargeSummarys";
    private static final Charset charset = Charset.forName("UTF-8");
    private static final PropertyDescriptor MONGO_CONNECTION_URL = new PropertyDescriptor
            .Builder().name("MONGO_CONNECTION_URL").displayName("MONGO CONNECTION URL").
            description("mongodb://{{AppServerUser}}:{{AppServerPassword}}@{{AppServer}}/admin")
            .required(true).addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    DB siteDb = null;
    SiteDetails siteDetails = null;
    SiteStats siteStats = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
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
        FlowFile flowFile = session.create();

//        DBCollection dbFiles = null;
//        IdentityDao identityDao = new IdentityDao();
//        UserDao userDao = new UserDao();
//        HospitalizationDao hospitalizationDao = new HospitalizationDao();
//        PrescriptionDao prescriptionDao = new PrescriptionDao();
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                "hello Guys from Process Discharge Summary", Level.INFO, null);

        try{
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(siteMasterMap));
            session.transfer(output, REL_SUCCESS);
        }
        catch (Exception ex){

        }
//        try{
//            String dbUrl = context.getProperty(MONGO_CONNECTION_URL).evaluateAttributeExpressions().getValue();
//            MongoClient client = new MongoClient(new MongoClientURI(dbUrl));
//            DB appDb = client.getDB("HHAppData");
////        NotificationDao notificationDao = new NotificationDao();
//            dbFiles = siteDb.getCollection("discharge_summary");
////        log.info("{} Discharge Summarys: {}", siteDetails.getSiteName(), dbFiles.count());
//        }
//        catch (Exception ex){
//            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
//                    Utility.stringifyException(ex), Level.INFO, null);
//        }


//        try(DBCursor cur = dbFiles.find()) {
//
//
//            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
//                    "hello Guys from Process Discharge Summary", Level.INFO, null);
//            while (cur.hasNext()) {
//                DBObject dbOrig = cur.next();
//                DBObject dbFile = dbOrig;
//                String fileName = dbFile.get("fileName").toString();
//                String[] parts = fileName.split("\\.");
//                if (parts.length == 0) {
////                    log.info("Unable to parse file Name: " + fileName);
//                    dbFiles.remove(dbOrig);
//                    continue;
//                }
//
//                if (parts[0].contains("IP")) {
//
//                    //       	log.info("Processing IP");
//                    String uhid = parts[1] + "." + parts[2];
//                    String siteId = getSiteKeyFromUhid(uhid);
//                    String siteName = getSiteNameFromUhid(uhid);
//                    DBUser dbUser = identityDao.findByUhid(uhid, siteId);
//                    if (dbUser == null) {
//                        continue; //This user is not yet imported.
//                    }
//
//                    boolean found = false;
//                    Date dDischarge = new Date();
//
//                    List<DBAttachement> hospitalizationFiles = new ArrayList<>();
//                    for (DBHospitalization hosp : dbUser.getHospitilization()) {
//                        if (hosp.getDateOfDischarge().equals(dDischarge)) {
//                            found = true;
//                            break;
//                        }
//
//                        hospitalizationFiles = hosp.getHospitlizationFiles();
//                        for (DBAttachement temp : hospitalizationFiles) {
//                            if (temp.getFileName().equals(fileName)) {
//                                found = true;
//                                break;
//                            }
//                        }
//
//                    }
//
//                    if (!found) {
//                        DBHospitalization dbHosp = new DBHospitalization();
//                        dbHosp.setDateOfDischarge(dDischarge);
//                        dbHosp.setDoctorName(parts[3].replace("_", " "));
//                        dbHosp.setHospitalName(siteName);
//                        dbHosp.setSource(siteDetails.getSiteKey());
//                        /** Attachment attach = new Attachment();
//                         attach.setFileName(fileName);
//                         attach.setMimeType(dbFile.get("fileType").toString());
//                         attach.setContent(Base64.decodeBase64(dbFile.get("content").toString()));
//
//                         DBAttachement dbAttach = DBUtil.writeDBAttachement(attach);
//                         dbHosp.getHospitlizationFiles().add(dbAttach);
//
//                         */
//                        hospitalizationDao.save(dbHosp);
//                        dbUser.getHospitilization().add(dbHosp);
//                        userDao.save(dbUser);
//
//
//                        siteStats.setDischargeSummarysImported(siteStats.getDischargeSummarysImported() + 1);
////                        notificationDao.addNotification(dbUser, "DischargeSummary -"+dbHosp.getDateOfDischarge()+" is getting imported",
////                                NotificationLevel.System,NotificationType.DischargeSummary,siteDetails.getSiteKey(),null, uhid);
//                        //Send the SMS, inviting the user to Prism.
//
//                        try {
//                            long delayDays = getDaysBetweenTwoDates(dDischarge.getTime(), new Date().getTime());
//                            if (delayDays <= 7 && siteDetails.isSms()) {
//                                DBsms dbSms = new DBsms();
//                                dbSms.setSmsPurpose("DISCHARGE-INTIMATION");
//                                dbSms.setMobileNumber(dbUser.getMobileNumber());
//                                dbSms.setDbUser(dbUser);
//                                dbSms.setUhid(uhid);
//                                dbSms.setSiteKey(siteDetails.getSiteKey());
//                                dbSms.setSendAt(new Date());
//                                SMSDao smsDao = new SMSDao();
//                                smsDao.save(dbSms);
//                            }
//                        } catch (Exception e) {
////                            log.error("PHR | OldProcessorJob | Sending Discharge Summary Intimation SMS | Error ",e);
//                        }
//                    }
//                    dbFiles.remove(dbOrig);
//                }
//            }
//
//
//            FlowFile output = getFlowFile(session, flowFile, "Success");
//            session.transfer(output, REL_SUCCESS);
//        } catch (Exception ex) {
//            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
//                    Utility.stringifyException(ex), Level.INFO, null);
//            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex));
//            session.transfer(output, REL_FAILURE);
//        }
//        finally {
////            if (cur != null)
////                cur.close();
//        }
//        log.info("{} Discharge Summarys Added: {}", siteDetails.getSiteName(), siteStats.getDischargeSummarysImported());


    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data) {
        FlowFile outputFF = session.create(ff);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
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

    public SiteDetails getSiteBasedOnPrefix(String sitePrefix) {
        for (String siteKey : siteMasterMap.keySet()) {
            SiteDetails siteDetails = siteMasterMap.get(siteKey);
            if (siteDetails.getUhidPrefix() != null &&
                    siteDetails.getUhidPrefix().equals(sitePrefix))
                return siteDetails;
        }

        return null;
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

    protected String getSiteNameFromUhid(String uhid) {
        //For Medmantra a UHID also has a location prefix,
        //which indicates from which site this user originally came from.
        //There for we can not search based on which site has sent this lab result.
        if (uhid.indexOf('.') != -1) {
            //We have a '.' in UHID. We use this information
            //to split UHID into locationId & UHID.
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                //We can not have Splits more then 2.
//                log.error("Imporper UHID: {}, unable to split it.", uhid);
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
        //For Medmantra a UHID also has a location prefix,
        //which indicates from which site this user originally came from.
        //There for we can not search based on which site has sent this lab result.
        if (uhid.indexOf('.') != -1) {
            //We have a '.' in UHID. We use this information
            //to split UHID into locationId & UHID.
            String[] parts = uhid.split("\\.");
            if (parts.length > 2) {
                //We can not have Splits more then 2.
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
}
