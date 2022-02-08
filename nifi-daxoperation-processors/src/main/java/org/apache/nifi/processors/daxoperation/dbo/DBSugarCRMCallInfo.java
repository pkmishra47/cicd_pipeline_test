package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "SugarCRMCallInfo", noClassnameStored = true)
public class DBSugarCRMCallInfo {
	@Id private ObjectId id;
	@Reference private DBUser dbUser;
	@Property private String patientId;
	@Property private Date callDate;
	@Property private String callInfo;
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
	public String getPatientId() {
		return patientId;
	}
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	public Date getCallDate() {
		return callDate;
	}
	public void setCallDate(Date callDate) {
		this.callDate = callDate;
	}
	public String getCallInfo() {
		return callInfo;
	}
	public void setCallInfo(String callInfo) {
		this.callInfo = callInfo;
	}
	
}
