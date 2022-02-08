package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "ImmunizationMaster", noClassnameStored = true)
public class DBImmunizationMaster {
	@Id private ObjectId id;
	@Property private String immunizationName; 
	@Property private String description;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setImmunizationName(String immunizationName) {
		this.immunizationName = immunizationName;
	}
	public String getImmunizationName() {
		return immunizationName;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
}
