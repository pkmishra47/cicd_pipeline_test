package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Application", noClassnameStored = true)

/**
 * This is for external applications, like facebook, linkedin, etc
 */
public class DBApplication {
	@Id private ObjectId id;
	@Property private String applicationName; // External or Internal Application 
	
	// Note : Model to be evolved, as of now its a placeholder class
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getApplicationName() {
		return applicationName;
	}

}
	