package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Device", noClassnameStored = true)
public class DBDevice {
	@Id private ObjectId id;
	@Property private String company;
	@Property private String deviceType;
	@Property private String deviceId;
	@Property private Date creationDate;
	@Property private String uhid;

	@Reference 
	private DBUser dbUser;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}

	public DBUser getDbUser() {
		return dbUser;
	}

	public void setDbUser(DBUser dbUser) {
		this.dbUser = dbUser;
	}
}