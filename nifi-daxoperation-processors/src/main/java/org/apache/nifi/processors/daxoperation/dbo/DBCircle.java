package org.apache.nifi.processors.daxoperation.dbo;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "Circle", noClassnameStored = true)
public class DBCircle implements Comparable<DBCircle> {
	@Id private ObjectId id;
	@Property private String name;
	@Property private String ownerId;
	@Property private String category;
	@Property private List<String> userList = new ArrayList<String>();
	@Property private List<String> removedUsers = new ArrayList<String>();
	@Embedded("permissions")
	private Map<DBResources, List<DBAccessLevel>> permissions = new HashMap<DBResources, List<DBAccessLevel>>();
	@Property private Date updatedAt;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<String> getUserList() {
		return userList;
	}

	public void setUserList(List<String> userList) {
		this.userList = userList;
	}

	public List<String> getRemovedUsers() {
		return removedUsers;
	}

	public void setRemovedUsers(List<String> removedUsers) {
		this.removedUsers = removedUsers;
	}

	public Map<DBResources, List<DBAccessLevel>> getPermissions() {
		return permissions;
	}

	public void setPermissions(Map<DBResources, List<DBAccessLevel>> permissions) {
		this.permissions = permissions;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public int compareTo(DBCircle o) {
        // A null check here would be expensive, and mostly we would not have null
		// because its collection that are working internally.
		if(o == null)
			throw new IllegalArgumentException("DBCircle is Null");
	
		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}
}