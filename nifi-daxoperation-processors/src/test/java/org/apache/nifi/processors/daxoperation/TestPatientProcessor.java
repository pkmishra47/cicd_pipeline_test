/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.daxoperation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.apache.nifi.processors.daxoperation.utils.FlowFileAttributes;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class TestPatientProcessor {

//    private TestRunner testRunner;
//    private static final Charset charset = Charset.forName("UTF-8");
//
//    @Before
//    public void init() {
//        testRunner = TestRunners.newTestRunner(PatientProcessor.class);
//    }
//
//
////    @Test
//    public void testProcessorEndToEnd() throws InitializationException, SQLException, InterruptedException, JsonProcessingException {
//        Map<String, String> attributes = new HashMap<>();
//        attributes.put("execution.id", "1");
//
//        Map<String, String> flowfileContent = new HashMap<>();
//        flowfileContent.put(FlowFileAttributes.EXECUTION_ID, "\"1\"");
//        flowfileContent.put(FlowFileAttributes.SITE_DETAILS, getCustomSiteDetails());
//        flowfileContent.put(FlowFileAttributes.SITE_MAP_DETAILS, getCustomSiteMasterMap());
//        String content = flowfileContent.toString();
//        testRunner.enqueue(flowfileContent.toString(), attributes);
//        setContextParameters();
//
//        testRunner.run();
//    }
//
//    private void setContextParameters() throws InitializationException, SQLException {
//        testRunner.setProperty(PatientProcessor.MONGO_CONNECTION_URL, "mongodb://admin:adgjmptw@localhost/admin");
//    }
//
//    private String getCustomSiteDetails() {
//        return "{\n" +
//                "    \"siteName\": \"Vishakapatnam\",\n" +
//                "    \"siteKey\": \"73a3c3ab89a04edea4cd1dce83560fb4\",\n" +
//                "    \"siteType\": \"old\",\n" +
//                "    \"siteDb\": \"Vishakapatnam\",\n" +
//                "    \"entityName\": \"Apollo\",\n" +
//                "    \"testBlackList\": \"\",\n" +
//                "    \"hcuBlackList\": \"\",\n" +
//                "    \"uhidPrefix\": \"CVIS\",\n" +
//                "    \"locationId\": \"40109\",\n" +
//                "    \"debug\": false,\n" +
//                "    \"sms\": true,\n" +
//                "    \"blackListTests\": null,\n" +
//                "    \"blackListHCUs\": null,\n" +
//                "    \"stats\": {\n" +
//                "      \"dbSiteKey\": \"73a3c3ab89a04edea4cd1dce83560fb4\",\n" +
//                "      \"statDate\": \"Jan 12, 2021, 1:39:14 PM\",\n" +
//                "      \"patientsImported\": 0,\n" +
//                "      \"sugarPatientsImported\": 0,\n" +
//                "      \"lastUhid\": null,\n" +
//                "      \"lastRegistrationDate\": \"Jan 1, 1970, 5:30:00 AM\",\n" +
//                "      \"testsImported\": 0,\n" +
//                "      \"lastOrderId\": null,\n" +
//                "      \"lastBillId\": null,\n" +
//                "      \"lastTestImportDate\": \"Jan 1, 1970, 5:30:00 AM\",\n" +
//                "      \"lastResultImportDate\": \"Jan 1, 1970, 5:30:00 AM\",\n" +
//                "      \"proceduresImported\": 0,\n" +
//                "      \"prescriptionsImported\": 0,\n" +
//                "      \"dischargeSummarysImported\": 0,\n" +
//                "      \"billsImported\": 0,\n" +
//                "      \"healthChecksImported\": 0,\n" +
//                "      \"activatedPatients\": 0,\n" +
//                "      \"otpLogins\": 0,\n" +
//                "      \"googleLogins\": 0,\n" +
//                "      \"rawData\": {}\n" +
//                "    }\n" +
//                "  }";
//    }
//
//    private String getCustomSiteMasterMap() {
//        return "{" +
//                "\"73a3c3ab89a04edea4cd1dce83560fb4\": {\n" +
//                "      \"siteName\": \"Vishakapatnam\",\n" +
//                "      \"siteKey\": \"73a3c3ab89a04edea4cd1dce83560fb4\",\n" +
//                "      \"siteType\": \"old\",\n" +
//                "      \"siteDb\": \"Vishakapatnam\",\n" +
//                "      \"entityName\": \"Apollo\",\n" +
//                "      \"testBlackList\": \"\",\n" +
//                "      \"hcuBlackList\": \"\",\n" +
//                "      \"uhidPrefix\": \"CVIS\",\n" +
//                "      \"locationId\": \"40109\",\n" +
//                "      \"debug\": false,\n" +
//                "      \"sms\": true,\n" +
//                "      \"blackListTests\": null,\n" +
//                "      \"blackListHCUs\": null,\n" +
//                "      \"stats\": {\n" +
//                "        \"dbSiteKey\": \"73a3c3ab89a04edea4cd1dce83560fb4\",\n" +
//                "        \"statDate\": \"Jan 12, 2021, 1:39:14 PM\",\n" +
//                "        \"patientsImported\": 0,\n" +
//                "        \"sugarPatientsImported\": 0,\n" +
//                "        \"lastUhid\": null,\n" +
//                "        \"lastRegistrationDate\": \"Jan 1, 1970, 5:30:00 AM\",\n" +
//                "        \"testsImported\": 0,\n" +
//                "        \"lastOrderId\": null,\n" +
//                "        \"lastBillId\": null,\n" +
//                "        \"lastTestImportDate\": \"Jan 1, 1970, 5:30:00 AM\",\n" +
//                "        \"lastResultImportDate\": \"Jan 1, 1970, 5:30:00 AM\",\n" +
//                "        \"proceduresImported\": 0,\n" +
//                "        \"prescriptionsImported\": 0,\n" +
//                "        \"dischargeSummarysImported\": 0,\n" +
//                "        \"billsImported\": 0,\n" +
//                "        \"healthChecksImported\": 0,\n" +
//                "        \"activatedPatients\": 0,\n" +
//                "        \"otpLogins\": 0,\n" +
//                "        \"googleLogins\": 0,\n" +
//                "        \"rawData\": {}\n" +
//                "      }\n" +
//                "    }" +
//                "}";
//    }
}