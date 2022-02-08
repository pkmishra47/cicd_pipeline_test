package org.apache.nifi.processors.daxoperation.dbo;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "MedicineMaster", noClassnameStored = true)
public class DBMedicineMaster {
	@Id private ObjectId id;
	@Property private String medicineName; 
	@Property private String brandName;
	@Property private String genericName;
	@Property private String strength; // Note, this can be changes, as we could track each one by its strengh seperately
	@Property private String description;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getMedicinceName() {
		return medicineName;
	}
	
	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}
	
	public void setBrandName(String brandName){
		this.brandName = brandName;
	}
	public String getBrandName() {
		return this.brandName;
	}
	
	public void setGenericName(String genericName) {
		this.genericName = genericName;
	}
	public String getGenericName() {
		return genericName;
	}
	
	public void setStrengh(String strength) {
		this.strength = strength;
	}
	public String getStrength() {
		return this.strength;
	}
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
}
