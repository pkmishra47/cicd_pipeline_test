package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "HRA", noClassnameStored = true)
public class DBHRA {
	@Id private ObjectId id;
  	@Property private String hraName;
  	@Property private String hraShortName;
  	@Property private String hraImage;
  	@Property private String hraDescription;
  	@Property private String hraType;
  	@Embedded private List<DBHRASection> sections = new ArrayList<DBHRASection>();
  
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setHraName(String hraName) {
		this.hraName = hraName;
	}
	public String getHraName() {
		return hraName;
	}
	public void setHraShortName(String hraShortName) {
		this.hraShortName = hraShortName;
	}
	public String getHraShortName() {
		return hraShortName;
	}
	public String getHraImage() {
		return hraImage;
	}
	public void setHraImage(String hraImage) {
		this.hraImage = hraImage;
	}
	public void setHraDescription(String hraDescription) {
		this.hraDescription = hraDescription;
	}
	public String getHraDescription() {
		return hraDescription;
	}
	public List<DBHRASection> getSections() {
		return sections;
	}
	public void setSections(List<DBHRASection> sections) {
		this.sections = sections;
	}
	public String getHraType() {
		return hraType;
	}
	public void setHraType(String hraType) {
		this.hraType = hraType;
	}
}