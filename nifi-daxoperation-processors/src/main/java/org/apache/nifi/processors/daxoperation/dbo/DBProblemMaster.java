package org.apache.nifi.processors.daxoperation.dbo;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "ProblemMaster", noClassnameStored = true)
public class DBProblemMaster {
	@Id private ObjectId id;
	@Property private String problemName; // This is referenced from Problem Master
	@Property private String description;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getProblemName() {
		return problemName;
	}
	
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
}
