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
import org.apache.nifi.processors.daxoperation.dao.PharmacyBillsDao;
import org.apache.nifi.processors.daxoperation.dbo.DBPharmacyBillItems;
import org.apache.nifi.processors.daxoperation.dbo.DBPharmacyBills;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.dm.PharmacyBill;
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

public class ProcessPharmacyBills extends AbstractProcessor {
    private static final String processorName = "ProcessPharmacyBills";
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

    private SiteDetails siteDetails = null;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private Map<String, SiteDetails> siteMasterMap = null;
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private SimpleDateFormat pgDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected MongoClientService mongoClientService;
    private MongoClient mongoClient = null;
    private PharmacyBillsDao pharmacyBillsDao = null;

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
        List<PharmacyBill> failedPharmacyBillsList = new ArrayList<>();

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

            List<PharmacyBill> pharmacyBillsList = ffinput.pharmacyBill;
            logMetaData.put(FlowFileAttributes.EXECUTION_ID, ffinput.executionID);
            logMetaData.put(Constants.SITE_NAME, siteDetails.getSiteName());
            logMetaData.put(Constants.ENTITY_NAME, siteDetails.getEntityName());
            initializeDaos(mongoClient);

            totalProcessed = pharmacyBillsList.size();
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("%s Pharmacy Bills: %s", siteDetails.getSiteName(), pharmacyBillsList.size()), Level.INFO, null);

            while (!pharmacyBillsList.isEmpty()) {
                PharmacyBill dbPharmacyBill = pharmacyBillsList.get(0);

                try {
                    if (dbPharmacyBill.getPharmacyBill() != null)
                        dbPharmacyBill = dbPharmacyBill.getPharmacyBill();

                    String billno = "";
                    if (dbPharmacyBill.getBillno() != null)
                        billno = dbPharmacyBill.getBillno();
                    logMetaData.put("billno", billno);

                    String siteid = "";
                    if (dbPharmacyBill.getSiteid() != null)
                        siteid = dbPharmacyBill.getSiteid();

                    String mobileno = "";
                    if (dbPharmacyBill.getMobileno() != null)
                        mobileno = dbPharmacyBill.getMobileno();

                    logMetaData.put("mobileNo", mobileno);

                    Date billdatetime = null;
                    if (dbPharmacyBill.getBilldatetime() != null)
                        try {
                            billdatetime = dateformat.parse(getDateWithoutGMT(dbPharmacyBill.getBilldatetime()));
                        } catch (Exception exception) {
                            try {
                                billdatetime = pgDateFormat.parse(dbPharmacyBill.getBilldatetime());
                            } catch (Exception ex) {
                                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Could not parse billdatetime date %s for mobile %s", dbPharmacyBill.getBilldatetime(), mobileno), Level.WARN, null);
                            }
                        }

                    String itemname = "";
                    if (dbPharmacyBill.getItemname() != null)
                        itemname = dbPharmacyBill.getItemname();

                    Double saleqty = 0.0;
                    try {
                        if (dbPharmacyBill.getSaleqty() != null)
                            saleqty = Double.parseDouble((dbPharmacyBill.getSaleqty()));
                    } catch (Exception e) {
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to parse saleqty %s", e.getMessage()), Level.WARN, null);
                    }

                    DBPharmacyBills dbBills = pharmacyBillsDao.findBillsByBillIdAndLocationId(billno, siteid);
                    DBPharmacyBillItems billItem = null;
                    boolean found = false;

                    if (dbBills != null) {
                        for (DBPharmacyBillItems item : dbBills.getLineItems()) {
                            if (dbBills.getBilldatetime().equals(billdatetime))
                                if (item.getItem_name().equals(itemname))
                                    if (item.getSaleqty() == saleqty) {
                                        found = true;
                                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("found pharmacy bills with bill no : " + billno + "  .. removing received object from processing list..."), Level.INFO, null);
                                        break;
                                    }
                        }
                    }

                    if (!found) {
                        String customername = "";
                        if (dbPharmacyBill.getCustomername() != null)
                            customername = dbPharmacyBill.getCustomername();

                        String partnertracking = "";
                        if (dbPharmacyBill.getPartnertracking() != null)
                            partnertracking = dbPharmacyBill.getPartnertracking();

                        String partnerid = "";
                        if (dbPharmacyBill.getPartnerid() != null)
                            partnerid = dbPharmacyBill.getPartnerid();

                        String sitename = "";
                        if (dbPharmacyBill.getSitename() != null)
                            sitename = dbPharmacyBill.getSitename();

                        String sitelocation = "";
                        if (dbPharmacyBill.getSitelocation() != null)
                            sitelocation = dbPharmacyBill.getSitelocation();

                        String itemid = "";
                        if (dbPharmacyBill.getItemid() != null)
                            itemid = dbPharmacyBill.getItemid();

                        String itemtype = "";
                        if (dbPharmacyBill.getItemtype() != null)
                            itemtype = dbPharmacyBill.getItemtype();

                        String state = "";
                        if (dbPharmacyBill.getState() != null)
                            state = dbPharmacyBill.getState();

                        String city = "";
                        if (dbPharmacyBill.getCity() != null)
                            city = dbPharmacyBill.getCity();

                        String region = "";
                        if (dbPharmacyBill.getRegion() != null)
                            region = dbPharmacyBill.getRegion();

                        String address = "";
                        if (dbPharmacyBill.getAddress() != null)
                            address = dbPharmacyBill.getAddress();

                        Double mrp = 0.0;
                        try {
                            if (dbPharmacyBill.getMrp() != null)
                                mrp = Double.parseDouble((dbPharmacyBill.getMrp()));
                        } catch (Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to parse mrp %s", e.getMessage()), Level.WARN, null);
                        }

                        Double totmrpval = 0.0;
                        try {
                            if (dbPharmacyBill.getTotmrpval() != null)
                                totmrpval = Double.parseDouble((dbPharmacyBill.getTotmrpval()));
                        } catch (Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to parse totmrpval %s", e.getMessage()), Level.WARN, null);
                        }

                        Double discamt = 0.0;
                        try {
                            if (dbPharmacyBill.getDiscamt() != null)
                                discamt = Double.parseDouble((dbPharmacyBill.getDiscamt()));
                        } catch (Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to parse discamt %s", e.getMessage()), Level.WARN, null);
                        }
                        Double giftamt = 0.0;
                        try {
                            if (dbPharmacyBill.getGiftamt() != null)
                                giftamt = Double.parseDouble((dbPharmacyBill.getGiftamt()));
                        } catch (Exception e) {
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Unable to parse giftamt %s", e.getMessage()), Level.WARN, null);
                        }

                        if (dbBills == null) {
                            dbBills = new DBPharmacyBills();
                            dbBills.setAddress(address);
                            dbBills.setBill_no(billno);
                            dbBills.setBilldatetime(billdatetime);
                            dbBills.setCity(city);
                            dbBills.setCustomer_name(customername);
                            dbBills.setMobileno(mobileno);
                            dbBills.setPartner_id(partnerid);
                            dbBills.setPartner_tracking(partnerid);
                            dbBills.setRegion(region);
                            dbBills.setSite_id(siteid);
                            dbBills.setSite_location(sitelocation);
                            dbBills.setSite_name(sitename);
                            dbBills.setState(state);
                            pharmacyBillsDao.save(dbBills);
                            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "new pharmacy bills got added for mobileno: " + mobileno + " ,billno: " + billno + " ,siteid: " + siteid + " ,billdatetime: " + billdatetime + " ,itemname: " + itemname + " ,saleqty: " + saleqty, Level.INFO, null);
                        }

                        billItem = new DBPharmacyBillItems();
                        billItem.setDiscamt(discamt);
                        billItem.setGiftamt(giftamt);
                        billItem.setItem_name(itemname);
                        billItem.setItemid(itemid);
                        billItem.setMrp(mrp);
                        billItem.setSaleqty(saleqty.intValue());
                        billItem.setTotalmrp(totmrpval);
                        dbBills.getLineItems().add(billItem);
                        pharmacyBillsDao.save(dbBills);
                        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "processed pharmacy bills for mobileno: " + mobileno + " ,billno: " + billno + " ,siteid: " + siteid + " ,billdatetime: " + billdatetime + " ,itemname: " + itemname + " ,saleqty: " + saleqty, Level.INFO, null);
                    }
                    pharmacyBillsList.remove(dbPharmacyBill);
                    successCount++;
                    logMetaData.put("dax_turn_around_time", Utility.CalculateDaxTurnAroundTimeInSec(Long.parseLong(ffinput.executionID)));

                } catch (ConcurrentModificationException | DuplicateKeyException curEx) {
                    failedPharmacyBillsList.add(dbPharmacyBill);
                    pharmacyBillsList.remove(dbPharmacyBill);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(curEx), Level.WARN, null);
                } catch (Exception ex) {
                    failedPharmacyBillsList.add(dbPharmacyBill);
                    pharmacyBillsList.remove(dbPharmacyBill);
                    this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(ex), Level.ERROR, null);
                }
            }

            long endTime = this.getDateUtil().getEpochTimeInSecond();
            log_site_processing_time(endTime - startTime, totalProcessed, successCount, logMetaData);

            if (!failedPharmacyBillsList.isEmpty()) {
                ffinput.pharmacyBill = failedPharmacyBillsList;
                String flowFileContent = gson.toJson(ffinput);
                setFlowFileDate(inputFF, session);
                session.write(inputFF, out -> out.write(flowFileContent.getBytes(charset)));
                session.transfer(inputFF, REL_FAILURE);
                return;
            } else
                ffinput.pharmacyBill = pharmacyBillsList;

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
                return null;
            }
            return uhidSite.getSiteName();
        }

        return siteDetails.getSiteName();
    }

    private void initializeDaos(MongoClient client) {
        this.pharmacyBillsDao = new PharmacyBillsDao(client);
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