package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "LabTestMaster", noClassnameStored = true)
public class DBLabTestMaster {
	@Id private ObjectId id;
	@Property private String testName; 
	@Embedded("parameters") 
	List<String> parameters = new ArrayList<String>(); // All parameters are stored as string
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}
	
	public String getTestName() {
		return testName;
	}
	
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	public List<String> getParameters() {
		return parameters;
	}
	
	public void addParameters(String parameters) {
		this.parameters.add(parameters);
	}
}
