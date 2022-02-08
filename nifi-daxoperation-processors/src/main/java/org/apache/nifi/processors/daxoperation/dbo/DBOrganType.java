package org.apache.nifi.processors.daxoperation.dbo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "OrganType", noClassnameStored = true)
public class DBOrganType {
	@Id private ObjectId id;
	@Property private String organName;
	@Property private String description;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public DBOrganType() {
		
	}
	public DBOrganType(String organName, String description) {	
		this.organName = organName;
		this.description = description;
	}
	
	public String getOrganName() {
		return organName;
	}
	public String getDescription() {
		return description;
	}
}
