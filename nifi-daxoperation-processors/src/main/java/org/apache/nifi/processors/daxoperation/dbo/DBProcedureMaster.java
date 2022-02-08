package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "ProcedureMaster", noClassnameStored = true)
public class DBProcedureMaster {
	@Id private ObjectId id;
	@Property private String procedureName; // This is referenced from Problem Master
	@Property private String description;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getProcedureName() {
		return procedureName;
	}
	
	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
}
