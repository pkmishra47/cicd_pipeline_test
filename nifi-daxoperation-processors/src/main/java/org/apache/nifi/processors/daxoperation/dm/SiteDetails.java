package org.apache.nifi.processors.daxoperation.dm;

public class SiteDetails {
    private String siteName;
    private String siteKey;
    private String siteType;
    private String siteDb;
    private String entityName;
    private String testBlackList;
    private String hcuBlackList;
    private String uhidPrefix;
    private String locationId;
    private boolean debug;
    private boolean sms;
    private String[] blackListTests;
    private int[] blackListHCUs;

    private SiteStats stats;
    public SiteDetails(String siteKey) {
        this.siteKey = siteKey;
        stats = new SiteStats(siteKey);
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
        this.siteDb = siteName.replaceAll(" ", "");
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

    public String getSiteDb() {
        return siteDb;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
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

    public SiteStats getStats() {
        return stats;
    }

    public String getUhidPrefix() {
        return uhidPrefix;
    }

    public void setUhidPrefix(String uhidPrefix) {
        this.uhidPrefix = uhidPrefix;
    }

    public String[] getBlackListTests() {
        return blackListTests;
    }

    public void setBlackListTests(String[] testIds) {
        this.blackListTests = testIds;
    }

    public int[] getBlackListHCUs() {
        return blackListHCUs;
    }

    public void setBlackListHCUs(int[] blackListHCUs) {
        this.blackListHCUs = blackListHCUs;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
}
