package org.apache.nifi.processors.daxoperation.bo;

import java.util.Date;

public class SiteStats {
    public String siteKey;
    public Date statDate;
    public long patients;
    public String lastUhid;
    public Date lastRegistrationDate;
    public long tests;
    public String lastOrderId;
    public String lastBillId;
    public Date lastTestImportDate;
    public Date lastResultImportDate;
    public long dischargeSummaryImported;
    public long healthchecksImported;
    public String rawData;
    public long prescriptionsImported;
    public long activatedPatients;
    public long otpLogins;
    public long googleLogins;


    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public Date getStatDate() {
        return statDate;
    }

    public void setStatDate(Date statDate) {
        this.statDate = statDate;
    }

    public long getPatients() {
        return patients;
    }

    public void setPatients(long patients) {
        this.patients = patients;
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

    public long getTests() {
        return tests;
    }

    public void setTests(long tests) {
        this.tests = tests;
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

    public long getDischargeSummaryImported() {
        return dischargeSummaryImported;
    }

    public void setDischargeSummaryImported(long dischargeSummaryImported) {
        this.dischargeSummaryImported = dischargeSummaryImported;
    }

    public long getHealthchecksImported() {
        return healthchecksImported;
    }

    public void setHealthchecksImported(long healthchecksImported) {
        this.healthchecksImported = healthchecksImported;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public long getPrescriptionsImported() {
        return prescriptionsImported;
    }

    public void setPrescriptionsImported(long prescriptionsImported) {
        this.prescriptionsImported = prescriptionsImported;
    }

    public long getActivatedPatients() {
        return activatedPatients;
    }

    public void setActivatedPatients(long activatedPatients) {
        this.activatedPatients = activatedPatients;
    }

    public long getOtpLogins() {
        return otpLogins;
    }

    public void setOtpLogins(long otpLogins) {
        this.otpLogins = otpLogins;
    }

    public long getGoogleLogins() {
        return googleLogins;
    }

    public void setGoogleLogins(long googleLogins) {
        this.googleLogins = googleLogins;
    }
}
