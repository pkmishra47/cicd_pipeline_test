package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Page", noClassnameStored = true)
public class DBPage {
	@Id private ObjectId id;
	
	@Property private String name;
	@Property private String displayName;
	@Property private String relativeUrl;
	@Property private int pageWeight;
	@Property private String mobileUrl;
	
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
	public String getRelativeUrl() {
		return relativeUrl;
	}
	public void setRelativeUrl(String relativeUrl) {
		this.relativeUrl = relativeUrl;
	}
	public int getPageWeight() {
		return pageWeight;
	}
	public void setPageWeight(int pageWeight) {
		this.pageWeight = pageWeight;
	}
	public String getMobileUrl() {
		return mobileUrl;
	}
	public void setMobileUrl(String mobileUrl) {
		this.mobileUrl = mobileUrl;
	}
}
