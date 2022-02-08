package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "LoginDetail")
public class DBLoginDetail {
	@Id private ObjectId id;
	@Property private String uhid;
	@Embedded private List<DBLoginDevice> devices = new ArrayList<DBLoginDevice>();
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getUhid() {
		return uhid;
	}
	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	public List<DBLoginDevice> getDevices() {
		return devices;
	}
	public void setDevices(List<DBLoginDevice> devices) {
		this.devices = devices;
	}
}