package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "SiteStats", noClassnameStored = true)
public class DBSiteStats {
	@Id private ObjectId id;
	@Property private String siteKey;
	@Property private Date statDate;
	@Property private int patients;
	@Property private String lastUhid;
	@Property private Date lastRegistrationDate;
	@Property private int tests;
	@Property private String lastOrderId;
	@Property private String lastBillId;
	@Property private Date lastTestImportDate;
	@Property private Date lastResultImportDate;
	@Property private int dischargeSummaryImported;
	@Property private int healthchecksImported;
	@Property private int billsImported;
	@Property private String rawData;
	@Property private int prescriptionsImported;
	@Property private int activatedPatients;
	@Property private int otpLogins;
	@Property private int googleLogins;
	
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
	public int getPatients() {
		return patients;
	}
	public void setPatients(int patients) {
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
	public int getTests() {
		return tests;
	}
	public void setTests(int tests) {
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
	public int getDischargeSummaryImported() {
		return dischargeSummaryImported;
	}
	public void setDischargeSummaryImported(int dischargeSummaryImported) {
		this.dischargeSummaryImported = dischargeSummaryImported;
	}
	public int getHealthchecksImported() {
		return healthchecksImported;
	}
	public void setHealthchecksImported(int healthchecksImported) {
		this.healthchecksImported = healthchecksImported;
	}
	public String getRawData() {
		return rawData;
	}
	public void setRawData(String rawData) {
		this.rawData = rawData;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public int getPrescriptionsImported() {
		return prescriptionsImported;
	}
	public void setPrescriptionsImported(int prescriptionsImported) {
		this.prescriptionsImported = prescriptionsImported;
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
	public int getBillsImported() {
		return billsImported;
	}

	public void setBillsImported(int billsImported) {
		this.billsImported = billsImported;
	}
	
}
