package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "SMS", noClassnameStored = true)
public class DBsms {
	@Id private ObjectId id;
	
	@Reference 
	private DBUser dbUser;
	
	private String mobileNumber;
	private String uhid;
	private String siteKey;
	private String smsPurpose;
	private String accessCode;
	private String accessCodeStatus;
	private Date sendAt;
	private Date accessCodeStatusUpdatedDate;
	private String statusUpdatedBy;
	private String downloadId;
	private String sendResult;
	private String whatsAppSendResult;
	private String deliveryResult;
	private String patientName;
	private String entityName;
	private boolean isWhatsapp;
	
	
	public String getSendResult() {
		return sendResult;
	}
	public void setSendResult(String sendResult) {
		this.sendResult = sendResult;
	}
	public String getDeliveryResult() {
		return deliveryResult;
	}
	public void setDeliveryResult(String deliveryResult) {
		this.deliveryResult = deliveryResult;
	}
	public String getStatusUpdatedBy() {
		return statusUpdatedBy;
	}
	public void setStatusUpdatedBy(String statusUpdatedBy) {
		this.statusUpdatedBy = statusUpdatedBy;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public DBUser getDbUser() {
		return dbUser;
	}
	public void setDbUser(DBUser dbUser) {
		this.dbUser = dbUser;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getUhid() {
		return uhid;
	}
	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	public String getAccessCode() {
		return accessCode;
	}
	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}
	public String getAccessCodeStatus() {
		return accessCodeStatus;
	}
	public void setAccessCodeStatus(String accessCodeStatus) {
		this.accessCodeStatus = accessCodeStatus;
	}
	
	public Date getSendAt() {
		return sendAt;
	}
	public void setSendAt(Date sendAt) {
		this.sendAt = sendAt;
	}
	public Date getAccessCodeStatusUpdatedDate() {
		return accessCodeStatusUpdatedDate;
	}
	public void setAccessCodeStatusUpdatedDate(Date accessCodeStatusUpdatedDate) {
		this.accessCodeStatusUpdatedDate = accessCodeStatusUpdatedDate;
	}
	public String getSiteKey() {
		return siteKey;
	}
	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}
	public String getSmsPurpose() {
		return smsPurpose;
	}
	public void setSmsPurpose(String smsPurpose) {
		this.smsPurpose = smsPurpose;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getDownloadId() {
		return downloadId;
	}
	public void setDownloadId(String downloadId) {
		this.downloadId = downloadId;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public boolean isWhatsapp() {
		return isWhatsapp;
	}
	public void setWhatsapp(boolean isWhatsapp) {
		this.isWhatsapp = isWhatsapp;
	}
	public String getWhatsAppSendResult() {
		return whatsAppSendResult;
	}
	public void setWhatsAppSendResult(String whatsAppSendResult) {
		this.whatsAppSendResult = whatsAppSendResult;
	}
}
