package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Feature", noClassnameStored = true)
public class DBFeature {
	@Id private ObjectId id;
	@Property private String featureName;
	@Reference private List<DBService> services = new ArrayList<DBService>();
	@Reference private List<DBPage> pages = new ArrayList<DBPage>();
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public List<DBService> getServices() {
		return services;
	}
	public void setServices(List<DBService> services) {
		this.services = services;
	}
	public List<DBPage> getPages() {
		return pages;
	}
	public void setPages(List<DBPage> pages) {
		this.pages = pages;
	}
}
