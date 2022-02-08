package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "LinkedAccount")
public class DBLinkedAccount {
	@Id private ObjectId id;
	@Property private String linkedUhId;
	@Property private String relationship;
	@Reference private DBUser dbUser;
	@Reference private DBUser linkedDBUser;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public DBUser getDbUser() {
		return dbUser;
	}
	public void setDbUser(DBUser dbUser) {
		this.dbUser = dbUser;
	}
	public DBUser getLinkedDBUser() {
		return linkedDBUser;
	}
	public void setLinkedDBUser(DBUser linkedDBUser) {
		this.linkedDBUser = linkedDBUser;
	}
	public String getLinkedUhId() {
		return linkedUhId;
	}
	public void setLinkedUhId(String linkedUhId) {
		this.linkedUhId = linkedUhId;
	}
	public String getRelationship() {
		return relationship;
	}
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
}

