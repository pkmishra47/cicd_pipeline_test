package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Embedded
public class DBUserContactInfo implements Comparable<DBUserContactInfo>{
	@Id ObjectId id = new ObjectId();
	@Property private String addressLine1; 
	@Property private String addressLine2; 
	@Property private String city; 
	@Property private String state; 
	@Property private String country; 
	@Property private int pincode; 
	@Property private String phone1; 
	@Property private String phone2; 
	@Property private String phone3; 
	@Property private String email1; 
	@Property private String email2; 
	
	@Embedded
	List<DBPrimaryCarePhysician> primaryCarePhysicialList = new ArrayList<DBPrimaryCarePhysician>();
	
	@Embedded
	List<DBEmergencyContact> emergencyContactList = new ArrayList<DBEmergencyContact>();


	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public String getPhone2() {
		return phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	public String getPhone3() {
		return phone3;
	}

	public void setPhone3(String phone3) {
		this.phone3 = phone3;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	}

	public String getEmail2() {
		return email2;
	}

	public void setEmail2(String email2) {
		this.email2 = email2;
	}
	public List<DBPrimaryCarePhysician> getPrimaryCarePhysicialList() {
		return primaryCarePhysicialList;
	}

	public void setPrimaryCarePhysicialList(
			List<DBPrimaryCarePhysician> primaryCarePhysicialList) {
		this.primaryCarePhysicialList = primaryCarePhysicialList;
	}

	public List<DBEmergencyContact> getEmergencyContactList() {
		return emergencyContactList;
	}

	public void setEmergencyContactList(
			List<DBEmergencyContact> emergencyContactList) {
		this.emergencyContactList = emergencyContactList;
	}
	public void addPrimaryCarePhysician(DBPrimaryCarePhysician pC) {
		this.primaryCarePhysicialList.add(pC);
	}
	public void addEmergencyContact(DBEmergencyContact ec) {
		this.emergencyContactList.add(ec);
	}

	@Override
	public int compareTo(DBUserContactInfo arg0) {
		if(arg0 == null)
			throw new IllegalArgumentException("DBUserContactInfo is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getPincode() {
		return pincode;
	}

	public void setPincode(int pincode) {
		this.pincode = pincode;
	}
}
