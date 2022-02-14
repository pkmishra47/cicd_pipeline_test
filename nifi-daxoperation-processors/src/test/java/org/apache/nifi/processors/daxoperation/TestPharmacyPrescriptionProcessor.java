package org.apache.nifi.processors.daxoperation;
//
//import com.mongodb.MongoClient;
//import org.apache.nifi.controller.MongoClientService;
//import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
//import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
//import org.apache.nifi.processors.daxoperation.dbo.DBIdentity;
//import org.apache.nifi.processors.daxoperation.utils.LogUtil;
//import org.apache.nifi.reporting.InitializationException;
//import org.apache.nifi.util.TestRunner;
//import org.apache.nifi.util.TestRunners;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
public class TestPharmacyPrescriptionProcessor {
//    private TestRunner testRunner;
//    private static final Charset charset = StandardCharsets.UTF_8;
//
//    @Before
//    public void init() {
//        testRunner = TestRunners.newTestRunner(PharmacyPrescriptionProcessor.class);
//    }
//
//    private void setContextParameters() throws InitializationException, SQLException {
//        testRunner.setProperty(PharmacyPrescriptionProcessor.DOCMAPPING_CSV_PATH, "csv_dummy_path");
//
//        MongoClient mockMongoClient = mock(MongoClient.class);
//        MongoClientService mockMongoClientService = mock(MongoClientService.class);
//        when(mockMongoClientService.getMongoClient()).thenReturn(mockMongoClient);
//        when(mockMongoClientService.getIdentifier()).thenReturn("mockdbcp");
//        testRunner.addControllerService("mockdbcp", mockMongoClientService, new HashMap<>());
//        testRunner.setProperty(PharmacyPrescriptionProcessor.MONGODB_CLIENT_SERVICE, "mockdbcp");
//        testRunner.enableControllerService(mockMongoClientService);
//    }
//
//    @Test
//    public void testProcessor_with_empty_request() throws SQLException, InitializationException {
//        setContextParameters();
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//
//        String content = "";
//        testRunner.enqueue(content);
//        testFailureCondition();
//    }
//
//    @Test
//    public void testProcessor_when_all_details_exists() throws SQLException, InitializationException {
//        setContextParameters();
//
//        List<DBIdentity> dbIdentityList = new ArrayList<>();
//        DBIdentity dbIdentity = new DBIdentity();
//        dbIdentity.setUhid("AC01.0004273338");
//        dbIdentityList.add(dbIdentity);
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//        IdentityDao mockIdentityDao = getMockIdentityDao();
//        when(mockIdentityDao.findDBIdentityByMobileNumber(any())).thenReturn(dbIdentityList);
//
//        testFailureCondition();
//    }
//
//    @Test
//    public void testProcessor_when_uhid_not_exists_but_mobileno_exists() throws SQLException, InitializationException {
//        setContextParameters();
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//
//        testFailureCondition();
//    }
//
//    @Test
//    public void testProcessor_when_uhid_and_mobileno_not_exists() throws SQLException, InitializationException {
//        setContextParameters();
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//
//        testFailureCondition();
//    }
//
//    @Test
//    public void testProcessor_when_orderdate_not_exists() throws SQLException, InitializationException {
//        setContextParameters();
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//
//        testFailureCondition();
//    }
//
//    @Test
//    public void testProcessor_when_doctorname_not_exists() throws SQLException, InitializationException {
//        setContextParameters();
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//
//        testFailureCondition();
//    }
//
//    @Test
//    public void testProcessor_when_medicine_details_not_exists() throws SQLException, InitializationException {
//        setContextParameters();
//
//        LogUtil mockLogUtil = getMockLogUtil();
//        doNothing().when(mockLogUtil).logMessage(any(), any(), any(), any(), any(), any());
//
//        testFailureCondition();
//    }
//
//    private void testFailureCondition() {
//        testRunner.run();
//        testRunner.assertTransferCount(PharmacyPrescriptionProcessor.REL_SUCCESS, 0);
//        testRunner.assertTransferCount(PharmacyPrescriptionProcessor.REL_FAILURE, 1);
//    }
//
//    private void testSuccessCondition() {
//        testRunner.run();
//        testRunner.assertTransferCount(PharmacyPrescriptionProcessor.REL_SUCCESS, 1);
//        testRunner.assertTransferCount(PharmacyPrescriptionProcessor.REL_FAILURE, 0);
//    }
//
//    private LogUtil getMockLogUtil() {
//        LogUtil logUtil = mock(LogUtil.class);
//        ((PharmacyPrescriptionProcessor) testRunner.getProcessor()).setLogUtil(logUtil);
//        return logUtil;
//    }
//
//    private IdentityDao getMockIdentityDao() {
//        IdentityDao identityDao = mock(IdentityDao.class);
//        ((PharmacyPrescriptionProcessor) testRunner.getProcessor()).setIdentityDao(identityDao);
//        return identityDao;
//    }
//
//    private String request_with_all_details() {
//        return "{\n" +
//                "   \"OrderId\":\"7710101\",\n" +
//                "   \"ShopId\":10101,\n" +
//                "   \"OrderDate\":\"07-07-2020 14:13:23\",\n" +
//                "   \"DoctorName\":\"Dr. Subbramania\",\n" +
//                "   \"UserId\":\"10101\",\n" +
//                "   \"CustomerDetails\":{\n" +
//                "      \"MobileNo\":\"9647576573\",\n" +
//                "      \"Comm_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"Del_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"FirstName\":\"RIYAJ\",\n" +
//                "      \"LastName\":\"\",\n" +
//                "      \"Uhid\":\"AC01.0004273338\",\n" +
//                "      \"City\":\"Chennai - Main, Greams Road\",\n" +
//                "      \"PostCode\":600012,\n" +
//                "      \"MailId\":\"\",\n" +
//                "      \"Age\":0,\n" +
//                "      \"CardNo\":\"\",\n" +
//                "      \"PatientName\":\"RIYAJ\",\n" +
//                "      \"Latitude\":0,\n" +
//                "      \"Longitude\":0\n" +
//                "   },\n" +
//                "   \"ItemDetails\":[\n" +
//                "      {\n" +
//                "         \"ItemID\":\"CAL0003\",\n" +
//                "         \"ItemName\":\"CALAPTIN 120 S.R TABS\",\n" +
//                "         \"Qty\":8,\n" +
//                "         \"Mou\":15,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":15,\n" +
//                "         \"Status\":true\n" +
//                "      },\n" +
//                "      {\n" +
//                "         \"ItemID\":\"PRE0086\",\n" +
//                "         \"ItemName\":\"PREDMET 16MG TAB\",\n" +
//                "         \"Qty\":9,\n" +
//                "         \"Mou\":15,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":15,\n" +
//                "         \"Status\":true\n" +
//                "      },\n" +
//                "            {\n" +
//                "         \"ItemID\":\"PRE0087\",\n" +
//                "         \"ItemName\":\"PARACETAMOL TAB\",\n" +
//                "         \"Qty\":5,\n" +
//                "         \"Mou\":10,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":9,\n" +
//                "         \"Status\":true\n" +
//                "      }\n" +
//                "   ]\n" +
//                "}";
//    }
//
//    private String request_when_uhid_not_exists_but_mobileNo_exists() {
//        return "{\n" +
//                "   \"OrderId\":\"7710101\",\n" +
//                "   \"ShopId\":10101,\n" +
//                "   \"OrderDate\":\"07-07-2020 14:13:23\",\n" +
//                "   \"DoctorName\":\"Dr. Subbramania\",\n" +
//                "   \"UserId\":\"10101\",\n" +
//                "   \"CustomerDetails\":{\n" +
//                "      \"MobileNo\":\"9647576573\",\n" +
//                "      \"Comm_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"Del_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"FirstName\":\"RIYAJ\",\n" +
//                "      \"LastName\":\"\",\n" +
//                "      \"Uhid\":\"\",\n" +
//                "      \"City\":\"Chennai - Main, Greams Road\",\n" +
//                "      \"PostCode\":600012,\n" +
//                "      \"MailId\":\"\",\n" +
//                "      \"Age\":0,\n" +
//                "      \"CardNo\":\"\",\n" +
//                "      \"PatientName\":\"RIYAJ\",\n" +
//                "      \"Latitude\":0,\n" +
//                "      \"Longitude\":0\n" +
//                "   },\n" +
//                "   \"ItemDetails\":[\n" +
//                "      {\n" +
//                "         \"ItemID\":\"CAL0003\",\n" +
//                "         \"ItemName\":\"CALAPTIN 120 S.R TABS\",\n" +
//                "         \"Qty\":8,\n" +
//                "         \"Mou\":15,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":15,\n" +
//                "         \"Status\":true\n" +
//                "      },\n" +
//                "      {\n" +
//                "         \"ItemID\":\"PRE0086\",\n" +
//                "         \"ItemName\":\"PREDMET 16MG TAB\",\n" +
//                "         \"Qty\":9,\n" +
//                "         \"Mou\":15,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":15,\n" +
//                "         \"Status\":true\n" +
//                "      },\n" +
//                "            {\n" +
//                "         \"ItemID\":\"PRE0087\",\n" +
//                "         \"ItemName\":\"PARACETAMOL TAB\",\n" +
//                "         \"Qty\":5,\n" +
//                "         \"Mou\":10,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":9,\n" +
//                "         \"Status\":true\n" +
//                "      }\n" +
//                "   ]\n" +
//                "}";
//    }
//
//    private String request_when_uhid_and_mobileNo_not_exists() {
//        return "{\n" +
//                "   \"OrderId\":\"7710101\",\n" +
//                "   \"ShopId\":10101,\n" +
//                "   \"OrderDate\":\"07-07-2020 14:13:23\",\n" +
//                "   \"DoctorName\":\"Dr. Subbramania\",\n" +
//                "   \"UserId\":\"10101\",\n" +
//                "   \"CustomerDetails\":{\n" +
//                "      \"MobileNo\":\"\",\n" +
//                "      \"Comm_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"Del_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"FirstName\":\"RIYAJ\",\n" +
//                "      \"LastName\":\"\",\n" +
//                "      \"Uhid\":\"\",\n" +
//                "      \"City\":\"Chennai - Main, Greams Road\",\n" +
//                "      \"PostCode\":600012,\n" +
//                "      \"MailId\":\"\",\n" +
//                "      \"Age\":0,\n" +
//                "      \"CardNo\":\"\",\n" +
//                "      \"PatientName\":\"RIYAJ\",\n" +
//                "      \"Latitude\":0,\n" +
//                "      \"Longitude\":0\n" +
//                "   },\n" +
//                "   \"ItemDetails\":[\n" +
//                "      {\n" +
//                "         \"ItemID\":\"CAL0003\",\n" +
//                "         \"ItemName\":\"CALAPTIN 120 S.R TABS\",\n" +
//                "         \"Qty\":8,\n" +
//                "         \"Mou\":15,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":15,\n" +
//                "         \"Status\":true\n" +
//                "      },\n" +
//                "      {\n" +
//                "         \"ItemID\":\"PRE0086\",\n" +
//                "         \"ItemName\":\"PREDMET 16MG TAB\",\n" +
//                "         \"Qty\":9,\n" +
//                "         \"Mou\":15,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":15,\n" +
//                "         \"Status\":true\n" +
//                "      },\n" +
//                "            {\n" +
//                "         \"ItemID\":\"PRE0087\",\n" +
//                "         \"ItemName\":\"PARACETAMOL TAB\",\n" +
//                "         \"Qty\":5,\n" +
//                "         \"Mou\":10,\n" +
//                "         \"Pack\":\"1\",\n" +
//                "         \"Price\":9,\n" +
//                "         \"Status\":true\n" +
//                "      }\n" +
//                "   ]\n" +
//                "}";
//    }
//
//    private String request_when_orderdate_not_exists() {
//        return "{\n" +
//                "   \"OrderId\":\"7710101\",\n" +
//                "   \"ShopId\":10101,\n" +
//                "   \"OrderDate\":\"\",\n" +
//                "   \"DoctorName\":\"Dr. Subbramania\",\n" +
//                "   \"UserId\":\"10101\",\n" +
//                "   \"CustomerDetails\":{\n" +
//                "      \"MobileNo\":\"9647576573\",\n" +
//                "      \"Comm_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"Del_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"FirstName\":\"RIYAJ\",\n" +
//                "      \"LastName\":\"\",\n" +
//                "      \"Uhid\":\"AC01.0004273338\",\n" +
//                "      \"City\":\"Chennai - Main, Greams Road\",\n" +
//                "      \"PostCode\":600012,\n" +
//                "      \"MailId\":\"\",\n" +
//                "      \"Age\":0,\n" +
//                "      \"CardNo\":\"\",\n" +
//                "      \"PatientName\":\"RIYAJ\",\n" +
//                "      \"Latitude\":0,\n" +
//                "      \"Longitude\":0\n" +
//                "   },\n" +
//                "   \"ItemDetails\":[\n" +
//                "       ]\n" +
//                "}";
//    }
//
//    private String request_when_doctorname_not_exists() {
//        return "{\n" +
//                "   \"OrderId\":\"7710101\",\n" +
//                "   \"ShopId\":10101,\n" +
//                "   \"OrderDate\":\"07-07-2020 14:13:23\",\n" +
//                "   \"DoctorName\":\"\",\n" +
//                "   \"UserId\":\"10101\",\n" +
//                "   \"CustomerDetails\":{\n" +
//                "      \"MobileNo\":\"9647576573\",\n" +
//                "      \"Comm_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"Del_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"FirstName\":\"RIYAJ\",\n" +
//                "      \"LastName\":\"\",\n" +
//                "      \"Uhid\":\"AC01.0004273338\",\n" +
//                "      \"City\":\"Chennai - Main, Greams Road\",\n" +
//                "      \"PostCode\":600012,\n" +
//                "      \"MailId\":\"\",\n" +
//                "      \"Age\":0,\n" +
//                "      \"CardNo\":\"\",\n" +
//                "      \"PatientName\":\"RIYAJ\",\n" +
//                "      \"Latitude\":0,\n" +
//                "      \"Longitude\":0\n" +
//                "   },\n" +
//                "   \"ItemDetails\":[\n" +
//                "       ]\n" +
//                "}";
//    }
//
//    private String request_when_medicince_details_not_exists() {
//        return "{\n" +
//                "   \"OrderId\":\"7710101\",\n" +
//                "   \"ShopId\":10101,\n" +
//                "   \"OrderDate\":\"07-07-2020 14:13:23\",\n" +
//                "   \"DoctorName\":\"Dr. Subbramania\",\n" +
//                "   \"UserId\":\"10101\",\n" +
//                "   \"CustomerDetails\":{\n" +
//                "      \"MobileNo\":\"9647576573\",\n" +
//                "      \"Comm_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"Del_addr\":\"93/41,PADAVTTAAMMAN KOVIL ST,KOSCAPET\",\n" +
//                "      \"FirstName\":\"RIYAJ\",\n" +
//                "      \"LastName\":\"\",\n" +
//                "      \"Uhid\":\"AC01.0004273338\",\n" +
//                "      \"City\":\"Chennai - Main, Greams Road\",\n" +
//                "      \"PostCode\":600012,\n" +
//                "      \"MailId\":\"\",\n" +
//                "      \"Age\":0,\n" +
//                "      \"CardNo\":\"\",\n" +
//                "      \"PatientName\":\"RIYAJ\",\n" +
//                "      \"Latitude\":0,\n" +
//                "      \"Longitude\":0\n" +
//                "   },\n" +
//                "   \"ItemDetails\":[\n" +
//                "       ]\n" +
//                "}";
    }
//}