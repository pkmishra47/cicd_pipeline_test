package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Corporate", noClassnameStored = true)
public class DBCorporate {
	@Id private ObjectId id;
	@Property private String name;
	@Property private String displayName;
	@Property private String contactPerson;
	@Property private String mobileNumber;
	@Reference private DBCircle circle;
	@Embedded private List<DBLabPackage> exclusivePackages = new ArrayList<DBLabPackage>();
	@Embedded private List<DBLabPackage> useralsoPackages = new ArrayList<DBLabPackage>();
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getContactPerson() {
		return contactPerson;
	}
	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public DBCircle getCircle() {
		return circle;
	}
	public void setCircle(DBCircle circle) {
		this.circle = circle;
	}
	public List<DBLabPackage> getExclusivePackages() {
		return exclusivePackages;
	}
	public void setExclusivePackages(List<DBLabPackage> exclusivePackages) {
		this.exclusivePackages = exclusivePackages;
	}
	public List<DBLabPackage> getUseralsoPackages() {
		return useralsoPackages;
	}
	public void setUseralsoPackages(List<DBLabPackage> useralsoPackages) {
		this.useralsoPackages = useralsoPackages;
	}
	public void addUseralsoPackages(DBLabPackage useralsoPackage) {
		useralsoPackages.add(useralsoPackage);
	}
	public void addExclusivePackages(DBLabPackage exclusivePackage) {
		exclusivePackages.add(exclusivePackage);
	}
}