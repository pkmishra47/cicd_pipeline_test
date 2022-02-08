package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "SugarInfo", noClassnameStored = true)
public class DBSugarInfo {
	@Id
	private ObjectId id = new ObjectId();
	@Reference 
	private DBUser dbUser;
	@Property private String pacakgeId;
	@Property private String packageName;
	@Property private Date billingDate;
	@Property private String treatingPhysician;
	@Property private String preferredLanguage;
	@Property private String daysReminder;
	@Property private String dcpFlag;
	@Property private String siteType;
	
	public ObjectId getId() {
		return id;
	}
	public String getDaysReminder() {
		return daysReminder;
	}
	public void setDaysReminder(String daysReminder) {
		this.daysReminder = daysReminder;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getPacakgeId() {
		return pacakgeId;
	}
	public void setPacakgeId(String pacakgeId) {
		this.pacakgeId = pacakgeId;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public Date getBillingDate() {
		return billingDate;
	}
	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}
	public String getTreatingPhysician() {
		return treatingPhysician;
	}
	public void setTreatingPhysician(String treatingPhysician) {
		this.treatingPhysician = treatingPhysician;
	}
	public String getPreferredLanguage() {
		return preferredLanguage;
	}
	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}
	public String getDcpFlag() {
		return dcpFlag;
	}
	public void setDcpFlag(String dcpFlag) {
		this.dcpFlag = dcpFlag;
	}
	public String getSiteType() {
		return siteType;
	}
	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}
	public DBUser getDbUser() {
		return dbUser;
	}
	public void setDbUser(DBUser dbUser) {
		this.dbUser = dbUser;
	}
}