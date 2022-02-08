package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Hospital", noClassnameStored = true)
public class DBHospital {
	@Id private ObjectId id;
	@Property private String hospitalName; 
	@Property private String region;
	@Property private String entity;
	@Embedded("hospitalAddress")
	private DBAddress hospitalAddress;
	@Property private String email;
	@Property private String emergencyContact_1;
	@Property private String emergencyContact_2;
	@Property private String customerCare_1;
	@Property private String customerCare_2;
	@Property private String description;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getHospitalName() {
		return hospitalName;
	}
	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public DBAddress getHospitalAddress() {
		return hospitalAddress;
	}
	public void setHospitalAddress(DBAddress hospitalAddress) {
		this.hospitalAddress = hospitalAddress;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmergencyContact_1() {
		return emergencyContact_1;
	}
	public void setEmergencyContact_1(String emergencyContact_1) {
		this.emergencyContact_1 = emergencyContact_1;
	}
	public String getEmergencyContact_2() {
		return emergencyContact_2;
	}
	public void setEmergencyContact_2(String emergencyContact_2) {
		this.emergencyContact_2 = emergencyContact_2;
	}
	public String getCustomerCare_1() {
		return customerCare_1;
	}
	public void setCustomerCare_1(String customerCare_1) {
		this.customerCare_1 = customerCare_1;
	}
	public String getCustomerCare_2() {
		return customerCare_2;
	}
	public void setCustomerCare_2(String customerCare_2) {
		this.customerCare_2 = customerCare_2;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}