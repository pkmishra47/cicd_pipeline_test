package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "SugarCRMPatient", noClassnameStored = true)
public class DBSugarCRMPatient {
	
	@Id private ObjectId id;
	@Reference private DBUser dbUser;
	@Property private String patientId;
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
	
	

}
