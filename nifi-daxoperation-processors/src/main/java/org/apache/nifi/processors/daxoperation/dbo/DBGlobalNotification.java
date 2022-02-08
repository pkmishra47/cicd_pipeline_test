package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

/*
 * The logic:
 * 
 * For notiification that have a global scope, use this one. A event here would be notified to all users. 
 * To decide to notify a user or not has to be decided by the following logic. 
 * See if the user is in the list called userConsumedLis, if not then notify the user and mark as notified.
 * 
 * Mani
 */
@Entity(value = "GlobalNotification", noClassnameStored = true)
public class DBGlobalNotification {
	@Id private ObjectId id;
	@Property private String message;
	@Reference("usersConsumed")
	List<DBUser> userConsumedList = new ArrayList<DBUser>();
	
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public List<DBUser> getUserConsumedList() {
		return userConsumedList;
	}
	
	public void setUserConsumedList(List<DBUser> userConsumedList) {
		this.userConsumedList = userConsumedList;
	}
	
	public void addUserToConsumed(DBUser dbUser) {
		userConsumedList.add(dbUser);
	}
	
	public void pruneUserConsumed() {
		this.userConsumedList.clear();
	}

}
