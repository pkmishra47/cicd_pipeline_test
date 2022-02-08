package org.apache.nifi.processors.daxoperation.utils;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.reflect.TypeToken;
import com.mongodb.*;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processors.daxoperation.dao.IdentityDao;
import org.apache.nifi.processors.daxoperation.dao.UserDao;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.dm.SiteStats;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utility {
    private static final Charset charset = StandardCharsets.UTF_8;

    public static String stringifyException(Throwable e) {
        if (e != null) {
            try {
                StringWriter stm = new StringWriter();
                PrintWriter wrt = new PrintWriter(stm);
                e.printStackTrace(wrt);
                wrt.close();
                return stm.toString();
            } catch (Exception ex) {
                return "Error dumping exception";
            }
        } else {
            return "Null exception argument";
        }
    }

    public static void isNullOrEmpty(Object obj, String objName) throws Exception {
        if (obj != null) {
            if (obj instanceof String) {
                if (((String) obj).length() > 0) {
                    return;
                } else {
                    throw new IllegalArgumentException("[Missing Properties] : " + objName);
                }
            }
        } else {
            throw new IllegalArgumentException("[Missing Properties] : " + objName);
        }
    }

    public static String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
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

    public static String getClusterName(String locId) {
        Map<String, Set> clusterLocIdMapping = GsonUtil.getGson().fromJson(Constants.CLUSTER_LOCID_MAP, new TypeToken<HashMap<String, Set>>() {
        }.getType());

        for (String key : clusterLocIdMapping.keySet()) {
            if (clusterLocIdMapping.get(key).contains(locId))
                return key;
        }
        return "";
    }

    public static void saveSiteStats(MongoClient client, SiteStats siteStats) {
        DB mainDb = client.getDB("HHAppData");
        DBCollection coll = mainDb.getCollection("SiteStats");

        DBObject dbSite = new BasicDBObject();
        dbSite.put("siteKey", siteStats.getDbSiteKey());
        dbSite.put("statDate", siteStats.getStatDate());
        dbSite.put("patients", siteStats.getPatientsImported());
        dbSite.put("lastUhid", siteStats.getLastUhid());
        dbSite.put("lastRegistrationDate", siteStats.getLastRegistrationDate());
        dbSite.put("tests", siteStats.getTestsImported());
        dbSite.put("lastOrderId", siteStats.getLastOrderId());
        dbSite.put("lastBillId", siteStats.getLastBillId());
        dbSite.put("lastTestImportDate", siteStats.getLastTestImportDate());
        dbSite.put("lastResultImportDate", siteStats.getLastResultImportDate());
        dbSite.put("billsImported", siteStats.getBillsImported());
        dbSite.put("dischargeSummaryImported", siteStats.getDischargeSummarysImported());
        dbSite.put("prescriptionsImported", siteStats.getPrescriptionsImported());
        dbSite.put("healthchecksImported", siteStats.getHealthChecksImported());

        Map<String, Integer> rawData = siteStats.getRawData();
        for (String statId : rawData.keySet()) {
            dbSite.put("RAW_" + statId, rawData.get(statId));
        }

        coll.save(dbSite);
    }

    public static DBUser createUserWithPlaceHolder(String uhid, String siteId, String firstName, String mobileNumber, SiteDetails siteDetails, IdentityDao identityDao, UserDao userDao) {
        DBUser user = new DBUser();
        user.setMobileNumber(mobileNumber);

        DBUserBasicInfo basicInfo = new DBUserBasicInfo();
        user.setUserBasicInfo(basicInfo);

        DBUserContactInfo contactInfo = new DBUserContactInfo();
        user.setUserContactInfo(contactInfo);

        DBUserPreferenceInfo preferenceInfo = new DBUserPreferenceInfo();
        preferenceInfo.setSmsAlert("YES");
        preferenceInfo.setEmailAlert("YES");
        user.setUserPreferenceInfo(preferenceInfo);

        Date currentDate = new Date();
        user.setFirstName(firstName);
        user.setDateImported(currentDate);
        user.setUpdatedAt(currentDate);
        user.setStatus(DBUser.UserStatus.NOT_ACTIVATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("user");
        user.setRoles(userRoles);
        user.getEntitys().add(siteDetails.getEntityName());
        userDao.save(user);

        DBIdentity identity = new DBIdentity();
        identity.setUhid(uhid);
        identity.setSiteKey(siteId);
        identity.setMobileNumber(mobileNumber);
        identity.setUserId(uhid.toLowerCase() + " " + siteId);
        identity.setDbUser(user);
        identityDao.save(identity);
        user.setUpdateIdentityObject(true);
        userDao.save(user);
        return user;
    }

    public static long CalculateDaxTurnAroundTimeInSec(long epochTime) {
        return ((new Date()).getTime() - epochTime) / 1000;
    }

    public static ArrayList<String> getRecommendationRankList() {
        ArrayList<String> recommendations = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            if (i == 1)
                recommendations.add("PRIMARY");
            else if (i == 2)
                recommendations.add("SECONDARY");
            else if (i == 3)
                recommendations.add("TERTIARY");
            else if (i == 4)
                recommendations.add("QUATERNARY");
            else recommendations.add("QUINARY");
        }
        return recommendations;
    }

    public static Map<Integer, Map<Integer, String>> getRecommendationRankBasedOnScoreIndex(Map<Integer, List<Integer>> scoreIndexMap) {
        ArrayList<String> rankList = getRecommendationRankList();
        Map<Integer, Map<Integer, String>> scoreIndexRank = new HashMap<>();

        List<Integer> sortedScores = new ArrayList(scoreIndexMap.keySet());
        Collections.sort(sortedScores, Collections.reverseOrder());

        for (Integer score : sortedScores) {
            List<Integer> indices = scoreIndexMap.get(score);
            for (Integer index : indices) {
                Map<Integer, String> indexRank;
                if (scoreIndexRank.get(score) == null)
                    indexRank = new HashMap<>();
                else
                    indexRank = scoreIndexRank.get(score);

                indexRank.put(index, rankList.get(0));
                scoreIndexRank.put(score, indexRank);
                rankList.remove(0);
            }
        }

        return scoreIndexRank;
    }

    public static Map<String, ArrayList<String>> getDoctorMap(String csvPath, LogUtil logUtil, Map<String, Object> logMetaData) {
        Map<String, ArrayList<String>> records = new HashMap<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(csvPath));) {
            String[] values = null;
            int rowCount = 0;
            while ((values = csvReader.readNext()) != null) {
                if (rowCount == 0) {
                    rowCount++;
                    continue;
                }
                String doctorId = values[0].toUpperCase();
                String doctorName = values[1].toUpperCase();
                if (doctorId.isEmpty() || doctorName.isEmpty())
                    continue;

                ArrayList<String> doctorIds;
                if (records.containsKey(doctorName))
                    doctorIds = records.get(doctorName);
                else
                    doctorIds = new ArrayList<>();

                if (!doctorIds.contains(doctorId)) {
                    doctorIds.add(doctorId);
                    records.put(doctorName, doctorIds);
                }
            }
        } catch (Exception e) {
            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, stringifyException(e), Level.ERROR, null);
        }
        return records;
    }
}
