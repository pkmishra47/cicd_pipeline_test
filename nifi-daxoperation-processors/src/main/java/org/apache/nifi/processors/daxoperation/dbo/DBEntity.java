package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Entity", noClassnameStored = true)
public class DBEntity {
	@Id private ObjectId id;
  	@Property private String shortName;
  	@Property private String displayName;
  	@Property private String brandName;
  	@Reference private List<DBFeature> features = new ArrayList<DBFeature>();
  	@Property private String smsInvitation;
  	@Property private String smsInvitationTwo;
  	@Property private int smsDelayDays;
   	@Property private String smsLabtest;
  	@Property private String smsHealthCheck;
  	@Property private String smsDischarge;
  	@Property private String smsPrescription;
  	@Property private String smsHealthcheckdelay;
  	@Property private int daysToDelayHealthCheck;
  	@Property private String ftpServer;
  	@Property private String ftpUser;
  	@Property private String ftpPassword;
  	@Property private String smsPostActivation;  
  	@Property private List<String> sourceConditionFlags = new ArrayList<String>();
  	  	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getBrandname() {
        return brandName;		
	}
	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}
	public List<DBFeature> getFeatures() {
		return features;
	}
	public void setFeatures(List<DBFeature> features) {
		this.features = features;
	}
	public String getSmsInvitation() {
		return smsInvitation;
	}
	public void setSmsInvitation(String smsInvitation) {
		this.smsInvitation = smsInvitation;
	}
	public String getSmsInvitationTwo() {
		return smsInvitationTwo;
	}
	public void setSmsInvitationTwo(String smsInvitationTwo) {
		this.smsInvitationTwo = smsInvitationTwo;
	}
	public int getSmsDelayDays() {
		return smsDelayDays;
	}
	public void setSmsDelayDays(int smsDelayDays) {
		this.smsDelayDays = smsDelayDays;
	}
	public String getSmsLabtest() {
		return smsLabtest;
	}
	public void setSmsLabtest(String smsLabtest) {
		this.smsLabtest = smsLabtest;
	}
	public String getSmsHealthCheck() {
		return smsHealthCheck;
	}
	public void setSmsHealthCheck(String smsHealthCheck) {
		this.smsHealthCheck = smsHealthCheck;
	}
	public String getSmsDischarge() {
		return smsDischarge;
	}
	public void setSmsDischarge(String smsDischarge) {
		this.smsDischarge = smsDischarge;
	}
	public String getSmsPrescription() {
		return smsPrescription;
	}
	public void setSmsPrescription(String smsPrescription) {
		this.smsPrescription = smsPrescription;
	}
	public String getSmsHealthcheckdelay() {
		return smsHealthcheckdelay;
	}
	public void setSmsHealthcheckdelay(String smsHealthcheckdelay) {
		this.smsHealthcheckdelay = smsHealthcheckdelay;
	}
	public int getDaysToDelayHealthCheck() {
		return daysToDelayHealthCheck;
	}
	public void setDaysToDelayHealthCheck(int daysToDelayHealthCheck) {
		this.daysToDelayHealthCheck = daysToDelayHealthCheck;
	}
	public String getFtpServer() {
		return ftpServer;
	}
	public void setFtpServer(String ftpServer) {
		this.ftpServer = ftpServer;
	}
	public String getFtpUser() {
		return ftpUser;
	}
	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}
	public String getFtpPassword() {
		return ftpPassword;
	}
	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}
	public String getSmsPostActivation() {
		return smsPostActivation;
	}
	public void setSmsPostActivation(String smsPostActivation) {
		this.smsPostActivation = smsPostActivation;
	}
	public List<String> getSourceConditionFlags() {
		return sourceConditionFlags;
	}
	public void setSourceConditionFlags(List<String> sourceConditionFlags) {
		this.sourceConditionFlags = sourceConditionFlags;
	}

}
