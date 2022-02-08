package org.apache.nifi.processors.daxoperation.dbo;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Embedded
public class DBAddress implements Comparable<DBAddress>{
	@Id ObjectId id = new ObjectId();
	@Property private String tag;          // This denotes the address type like primary, postal, official, etc
	@Property private String doorNumber;
	@Property private String buildingName;
	@Property private String streetName_1;
	@Property private String streetName_2;
	@Property private String cityName;     // comes from CountryMaster
	@Property private String stateName;    // comes from CountryMaster
	@Property private String countryName;  // comes from CountryMaster
	@Property private String pinCode;      // comes from CountryMaster
	// A phone number might be needed here -- Think again Mani
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setDoorNumber(String doorNumber) {
		this.doorNumber = doorNumber;
	}
	public String getDoorNumber() {
		return doorNumber;
	}
	public void setHouseName(String houseName) {
		this.buildingName = houseName;
	}
	public String getHouseName() {
		return buildingName;
	}
	public void setStreetName_1(String streetName_1) {
		this.streetName_1 = streetName_1;
	}
	public String getStreetName_1() {
		return streetName_1;
	}
	public void setStreetName_2(String streetName_2) {
		this.streetName_2 = streetName_2;
	}
	public String getStreetName_2() {
		return streetName_2;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getCityName() {
		return cityName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	public String getStateName() {
		return stateName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}
	public String getPinCode() {
		return pinCode;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTag() {
		return tag;
	}
	@Override
	public int compareTo(DBAddress o) {
		if(o == null)
			throw new IllegalArgumentException("DBAddress is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}
	
}
