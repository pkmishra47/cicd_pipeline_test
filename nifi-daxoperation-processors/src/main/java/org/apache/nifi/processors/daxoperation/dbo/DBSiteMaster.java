package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "SiteMasterDB", noClassnameStored = true)
public class DBSiteMaster {
	@Id	private ObjectId id;
	@Property private String dbSiteName;	
	@Property private String dbSiteKey;
	@Property private String dbSiteType;
	@Property private String testBlackList;
	@Property private String hcuBlackList;
	@Property private String shortName;
	@Property private String address;
	@Property private String city;
	@Property private String uhidPrefix;
	@Property private String locationId;
	@Property private boolean debug;
	@Property private boolean sms;
	@Property private String siteDisplayName;	
	@Property private String entityName;
	@Property private String siteSupportId;
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getDbSiteName() {
		return dbSiteName;
	}

	public void setDbSiteName(String dbSiteName) {
		this.dbSiteName = dbSiteName;
	}

	public String getDbSiteKey() {
		return dbSiteKey;
	}

	public void setDbSiteKey(String dbSiteKey) {
		this.dbSiteKey = dbSiteKey;
	}

	public String getDbSiteType() {
		return dbSiteType;
	}

	public void setDbSiteType(String dbSiteType) {
		this.dbSiteType = dbSiteType;
	}

	public String getTestBlackList() {
		return testBlackList;
	}

	public void setTestBlackList(String testBlackList) {
		this.testBlackList = testBlackList;
	}

	public String getHcuBlackList() {
		return hcuBlackList;
	}

	public void setHcuBlackList(String hcuBlackList) {
		this.hcuBlackList = hcuBlackList;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getUhidPrefix() {
		return uhidPrefix;
	}

	public void setUhidPrefix(String uhidPrefix) {
		this.uhidPrefix = uhidPrefix;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public boolean isSms() {
		return sms;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setSms(boolean sms) {
		this.sms = sms;
	}

	public String getSiteDisplayName() {
		return siteDisplayName;
	}

	public void setSiteDisplayName(String siteDisplayName) {
		this.siteDisplayName = siteDisplayName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getSiteSupportId() {
		return siteSupportId;
	}

	public void setSiteSupportId(String siteSupportId) {
		this.siteSupportId = siteSupportId;
	}
}
