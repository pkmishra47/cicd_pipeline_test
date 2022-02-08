package org.apache.nifi.processors.daxoperation.dm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SiteStats {

    private String dbSiteKey;
    private Date statDate;

    private int patientsImported;
    private int sugarPatientsImported;
    private String lastUhid;
    private Date lastRegistrationDate = new Date(0L);

    private int testsImported;
    private String lastOrderId;
    private String lastBillId;
    private Date lastTestImportDate = new Date(0L);
    private Date lastResultImportDate = new Date(0L);

    private int proceduresImported;
    private int prescriptionsImported;
    private int dischargeSummarysImported;
    private int billsImported;
    private int healthChecksImported;
    private int activatedPatients;
    private int otpLogins;
    private int googleLogins;
    private Map<String, Integer> rawData = new HashMap<String, Integer>();

    public SiteStats(String siteKey) {
        dbSiteKey = siteKey;
        statDate = new Date();
    }

    public String getDbSiteKey() {
        return dbSiteKey;
    }
    public void setDbSiteKey(String dbSiteKey) {
        this.dbSiteKey = dbSiteKey;
    }
    public Date getStatDate() {
        return statDate;
    }
    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }
    public int getPatientsImported() {
        return patientsImported;
    }
    public void setPatientsImported(int patientsImported) {
        this.patientsImported = patientsImported;
    }
    public int getSugarPatientsImported() {
        return sugarPatientsImported;
    }

    public void setSugarPatientsImported(int sugarPatientsImported) {
        this.sugarPatientsImported = sugarPatientsImported;
    }

    public String getLastUhid() {
        return lastUhid;
    }
    public void setLastUhid(String lastUhid) {
        this.lastUhid = lastUhid;
    }
    public Date getLastRegistrationDate() {
        return lastRegistrationDate;
    }
    public void setLastRegistrationDate(Date lastRegistrationDate) {
        this.lastRegistrationDate = lastRegistrationDate;
    }
    public int getTestsImported() {
        return testsImported;
    }
    public void setTestsImported(int testsImported) {
        this.testsImported = testsImported;
    }
    public String getLastOrderId() {
        return lastOrderId;
    }
    public void setLastOrderId(String lastOrderId) {
        this.lastOrderId = lastOrderId;
    }
    public String getLastBillId() {
        return lastBillId;
    }
    public void setLastBillId(String lastBillId) {
        this.lastBillId = lastBillId;
    }
    public Date getLastTestImportDate() {
        return lastTestImportDate;
    }
    public void setLastTestImportDate(Date lastTestImportDate) {
        this.lastTestImportDate = lastTestImportDate;
    }
    public Date getLastResultImportDate() {
        return lastResultImportDate;
    }
    public void setLastResultImportDate(Date lastResultImportDate) {
        this.lastResultImportDate = lastResultImportDate;
    }

    public int getBillsImported() {
        return billsImported;
    }

    public void setBillsImported(int billsImported) {
        this.billsImported = billsImported;
    }

    public int getProceduresImported() {
        return proceduresImported;
    }

    public void setProceduresImported(int proceduresImported) {
        this.proceduresImported = proceduresImported;
    }

    public int getPrescriptionsImported() {
        return prescriptionsImported;
    }

    public void setPrescriptionsImported(int prescriptionsImported) {
        this.prescriptionsImported = prescriptionsImported;
    }

    public int getDischargeSummarysImported() {
        return dischargeSummarysImported;
    }
    public void setDischargeSummarysImported(int dischargeSummarysImported) {
        this.dischargeSummarysImported = dischargeSummarysImported;
    }
    public int getHealthChecksImported() {
        return healthChecksImported;
    }
    public void setHealthChecksImported(int healthChecksImported) {
        this.healthChecksImported = healthChecksImported;
    }

    public Map<String, Integer> getRawData() {
        return rawData;
    }

    public void incRawData(String dataId) {
        if(rawData.containsKey(dataId)) {
            int count = rawData.get(dataId);
            count++;
            rawData.put(dataId, count);
        } else {
            rawData.put(dataId, new Integer(1));
        }
    }

    public boolean hasAnyRawData() {
        for(String statId : rawData.keySet()) {
            if(rawData.get(statId) != 0)
                return true;
        }

        return false;
    }

    public String getRawStats() {
        StringBuilder sb = new StringBuilder();
        for(String statId : rawData.keySet()) {
            sb.append(String.format("%s : %s\n", statId, rawData.get(statId)));
        }

        return sb.toString();
    }

    public void mergeRawData(SiteStats other) {
        for(String statId : other.rawData.keySet()) {
            if(rawData.containsKey(statId)) {
                int count = rawData.get(statId);
                count += other.rawData.get(statId);
                rawData.put(statId, count);
            } else {
                rawData.put(statId, other.rawData.get(statId));
            }
        }
    }

    public int getActivatedPatients() {
        return activatedPatients;
    }

    public void setActivatedPatients(int activatedPatients) {
        this.activatedPatients = activatedPatients;
    }

    public int getOtpLogins() {
        return otpLogins;
    }

    public void setOtpLogins(int otpLogins) {
        this.otpLogins = otpLogins;
    }

    public int getGoogleLogins() {
        return googleLogins;
    }

    public void setGoogleLogins(int googleLogins) {
        this.googleLogins = googleLogins;
    }


}
