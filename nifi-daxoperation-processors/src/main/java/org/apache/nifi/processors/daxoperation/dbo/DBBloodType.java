package org.apache.nifi.processors.daxoperation.dbo;


/**
 * Reference and embedding of other objects is still pending
 * Mani -- important to note on reference vs embedding has to be done
 */
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "BloodType", noClassnameStored = true)
public class DBBloodType {
	@Id private ObjectId id;
	@Property private String bloodType;
	@Property private String description;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setBloodType(String bloodType) {
		this.bloodType = bloodType;
	}
	public String getBloodType() {
		return bloodType;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
}
