package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Role", noClassnameStored = true)
public class DBRole {
	@Id private ObjectId id;
	
	@Property  private String roleName;
	@Reference private List<DBFeature> features = new ArrayList<DBFeature>();
	@Property private List<String> sites = new ArrayList<String>();
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public List<DBFeature> getFeatures() {
		return features;
	}
	public void setFeatures(List<DBFeature> features) {
		this.features = features;
	}
	public List<String> getSites() {
		return sites;
	}
	public void setSites(List<String> sites) {
		this.sites = sites;
	}
	
	
}
