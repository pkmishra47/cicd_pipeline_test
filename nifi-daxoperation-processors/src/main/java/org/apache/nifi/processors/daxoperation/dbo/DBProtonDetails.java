package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "ProtonDetails", noClassnameStored = true)
public class DBProtonDetails implements Comparable<DBProtonDetails>{
	@Id private ObjectId id;	
	
	public static enum ProtonStatus {
		ACTIVE,
		INACTIVE,
		TERMINATED,
		NOT_ACTIVATED,
	}
	
	@Property private ProtonStatus status;
	@Property private Date activityDate;
	@Property private Boolean isMessageReplyed;
	@Property private ObjectId userId;	
	@Property private List<ObjectId> uploadFile = new ArrayList<ObjectId>();	
	@Property private List<ObjectId> message = new ArrayList<ObjectId>();	
	@Property private String userName;	
	@Property private String uhid;	
	@Embedded private DBIntCheckList checkList;
	@Property private String checkListType;	
	@Property private String unreadMsgs;	
	

	@Version long version;	

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ProtonStatus getStatus() {
		return status;
	}

	public void setStatus(ProtonStatus status) {
		this.status = status;
	}

	@Override
	public int compareTo(DBProtonDetails dbUsr) {
		if(dbUsr == null)
			throw new NullPointerException("DBUser is Null for compareTo function");
		
		return Comparators.stringCompare(getId().toString(), dbUsr.getId().toString());		
	}

	

	public Boolean getIsMessageReplyed() {
		return isMessageReplyed;
	}

	public void setIsMessageReplyed(Boolean isMessageReplyed) {
		this.isMessageReplyed = isMessageReplyed;
	}

	public Date getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}

	public ObjectId getUserId() {
		return userId;
	}

	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}

	public List<ObjectId> getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(List<ObjectId> uploadFile) {
		this.uploadFile = uploadFile;
	}

	public List<ObjectId> getMessage() {
		return message;
	}

	public void setMessage(List<ObjectId> message) {
		this.message = message;
	}

	public DBIntCheckList getCheckList() {
		return checkList;
	}

	public void setCheckList(DBIntCheckList checkList) {
		this.checkList = checkList;
	}

	public String getCheckListType() {
		return checkListType;
	}

	public void setCheckListType(String checkListType) {
		this.checkListType = checkListType;
	}

	public String getUnreadMsgs() {
		return unreadMsgs;
	}

	public void setUnreadMsgs(String unreadMsgs) {
		this.unreadMsgs = unreadMsgs;
	}




}