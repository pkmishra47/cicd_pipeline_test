package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "AllergyType", noClassnameStored = true)
public class DBAllergyType {
	@Id private ObjectId id;
	@Property private String allergyId;
	@Property private String allergyName;
	@Embedded("affectedOrgans")
	List<String> affectedOrgans = new ArrayList<String>(); // Referenced using OrganType 
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setAllergyId(String allergyId) {
		this.allergyId = allergyId;
	}
	
	public String getAllergyId() {
		return this.allergyId;
	}
	
	public void setAllergyName(String allergyName) {
		this.allergyName = allergyName;
	}
	
	public String getAllergyName() {
		return this.allergyName;
	}
	
	public void addAffectedOrgan(String organName) {
		affectedOrgans.add(organName);
	}
	
	public List<String> getAffectedOrgans() {
		return affectedOrgans;
	}
}
