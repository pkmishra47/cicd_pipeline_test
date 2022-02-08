package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Group", noClassnameStored = true)
public class DBGroup {
	
	@Id private ObjectId id;
	@Property private String groupName;
	@Property private Date dateOfCreated;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public Date getDateOfCreated() {
		return dateOfCreated;
	}
	public void setDateOfCreated(Date dateOfCreated) {
		this.dateOfCreated = dateOfCreated;
	}
	
}
