package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value ="MessageThread", noClassnameStored = true)
public class DBMessageThread {
	@Id private ObjectId id;
	@Property private String subject;
	@Property private Date startTime;
	@Property private ObjectId ownedBy;
	@Property private Boolean needUserAttention;
	@Property private Boolean needSupportAttention;
	@Property private Date lastActivityTime;
	@Property private Boolean isClosed;
	@Property private Boolean isStarred;
	@Property private String senderName;
	@Property private String ownerSiteId ;
	@Property private String ownerName ;
	@Property private String senderType ;

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Date getStartTime(){
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public ObjectId getOwnedBy() {
		return ownedBy;
	}
	public void setOwnedBy(ObjectId ownedBy) {
		this.ownedBy = ownedBy;
	}
	public Boolean getNeedUserAttention() {
		return needUserAttention;
	}
	public void setNeedUserAttention(Boolean needUserAttention) {
		this.needUserAttention = needUserAttention;
	}
	
	public Boolean getNeedSupportAttention() {
		return needSupportAttention;
	}
	public void setNeedSupportAttention(Boolean needSupportAttention) {
		this.needSupportAttention = needSupportAttention;
	}
	
	public Date getLastActivityTime() {
		return lastActivityTime;
	}
	public void setLastActivityTime(Date lastActivityTime) {
		this.lastActivityTime = lastActivityTime;
	}
	public Boolean getIsClosed() {
		return isClosed;
	}
	public void setIsClosed(Boolean isClosed) {
		this.isClosed = isClosed;
	}
	public Boolean getIsStarred() {
		return isStarred;
	}
	public void setIsStarred(Boolean isStarred) {
		this.isStarred = isStarred;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getOwnerSiteId() {
		return ownerSiteId;
	}
	public void setOwnerSiteId(String ownerSiteId) {
		this.ownerSiteId = ownerSiteId;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getSenderType() {
		return senderType;
	}
	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}
}
