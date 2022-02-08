package org.apache.nifi.processors.daxoperation.bo;

import com.mongodb.*;
import org.apache.nifi.processors.daxoperation.dm.SiteDetails;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.bson.types.ObjectId;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SiteMaster {
    public ObjectId id;
    public String siteName;
    public String shortName;
    public String siteKey;
    public String siteType;
    public String testBlackList;
    public String hcuBlackList;
    public String address;
    public String city;
    public String uhidPrefix;
    public String locationId;
    public Boolean debug;
    public String siteDisplayName;
    public String entityName;       //Mandatory will have 1 entity.
    public String siteSupportId;
    public Boolean sms;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public String getTestBlackList() {
        return testBlackList;
    }

    public void setTestBlackList(String testBlackList) {
        this.testBlackList = testBlackList;
    }

    public String getHcuBlackList() {
        return hcuBlackList;
    }

    public void setHcuBlackList(String hcuBlackList) {
        this.hcuBlackList = hcuBlackList;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUhidPrefix() {
        return uhidPrefix;
    }

    public void setUhidPrefix(String uhidPrefix) {
        this.uhidPrefix = uhidPrefix;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Boolean isDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getSiteDisplayName() {
        return siteDisplayName;
    }

    public void setSiteDisplayName(String siteDisplayName) {
        this.siteDisplayName = siteDisplayName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getSiteSupportId() {
        return siteSupportId;
    }

    public void setSiteSupportId(String siteSupportId) {
        this.siteSupportId = siteSupportId;
    }

    public Boolean isSms() {
        return sms;
    }

    public void setSms(Boolean sms) {
        this.sms = sms;
    }

    public static Map<String, SiteDetails> loadSiteMasterMap(MongoClient client, Map<String, Object> logMetaData, LogUtil logUtil) {
        DB appDb = client.getDB("HHAppData");
        Map<String, SiteDetails> mp = new HashMap<String, SiteDetails>();
        DBCollection coll = appDb.getCollection("SiteMasterDB");
        DBCursor cur = coll.find();
        while (cur.hasNext()) {
            DBObject obj = cur.next();
            String siteKey = obj.get("dbSiteKey").toString();
//            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Got sitekey : " + siteKey, Level.INFO, null);
            SiteDetails siteDetails = new SiteDetails(siteKey);
            siteDetails.setSiteName(obj.get("dbSiteName").toString());
            siteDetails.setSiteKey(siteKey);
            siteDetails.setSiteType(obj.get("dbSiteType").toString());
            siteDetails.setEntityName(obj.get("entityName").toString());
            Object tbl = obj.get("testBlackList");
            if (tbl != null)
                siteDetails.setTestBlackList(tbl.toString());
            Object hbl = obj.get("hcuBlackList");
            if (hbl != null)
                siteDetails.setHcuBlackList(hbl.toString());
            Object uhp = obj.get("uhidPrefix");
            if (uhp != null)
                siteDetails.setUhidPrefix(uhp.toString());

            Object locId = obj.get("locationId");
            if (locId != null)
                siteDetails.setLocationId(locId.toString());

            Object deb = obj.get("debug");
            if (deb != null)
                siteDetails.setDebug(new Boolean(deb.toString()));

            siteDetails.setSms(true);
            Object sms = obj.get("sms");
            if (sms != null)
                siteDetails.setSms(new Boolean(sms.toString()));

            //Populate the hidden tests array from the configuration.
            loadHiddenTests(siteDetails);
            mp.put(siteDetails.getSiteKey(), siteDetails);
        }
        return mp;
    }

    private static boolean loadHiddenTests(SiteDetails siteDetails) {
        try {
            if (siteDetails.getTestBlackList() != null && siteDetails.getTestBlackList().length() != 0) {
                String[] testIds = siteDetails.getTestBlackList().split(",");
                int[] blackListTests = new int[testIds.length];
                siteDetails.setBlackListTests(testIds); // here to testIds
            }
            if (siteDetails.getBlackListHCUs() != null && siteDetails.getBlackListHCUs().length != 0) {
                String[] testIds = siteDetails.getHcuBlackList().split(",");
                int[] blackListHCUs = new int[testIds.length];
                for (int pos = 0; pos < testIds.length; pos++)
                    blackListHCUs[pos] = Integer.parseInt(testIds[pos].trim());
                Arrays.sort(blackListHCUs);
                siteDetails.setBlackListHCUs(blackListHCUs);
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
