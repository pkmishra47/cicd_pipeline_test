package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "ImportData", noClassnameStored = true)
public class DBImportData implements Comparable<DBImportData> {
	@Id private ObjectId id;
	@Property private String siteApiKey; // This is referenced from AllergType
	@Property private String siteID;
	@Property private String version;
	@Property private String tableName;
	@Property private Date importTime;
	@Property private String data;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getSiteApiKey() {
		return siteApiKey;
	}
	public void setSiteApiKey(String siteApiKey) {
		this.siteApiKey = siteApiKey;
	}
	public String getSiteID() {
		return siteID;
	}
	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Date getImportTime() {
		return importTime;
	}
	public void setImportTime(Date importTime) {
		this.importTime = importTime;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public int compareTo(DBImportData other) {
		// A null check here would be expensive, and mostly we would not have null
		// because its collectiosn that are working internally.
		if(other == null)
			throw new IllegalArgumentException("DBImportData is Null");

		return Comparators.stringCompare(id.toString(), other.getId().toString());
	}
}
