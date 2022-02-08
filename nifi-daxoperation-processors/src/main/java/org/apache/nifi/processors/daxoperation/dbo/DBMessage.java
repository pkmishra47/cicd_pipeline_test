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

@Entity(value ="Message", noClassnameStored = true)
public class DBMessage {
@Id private ObjectId id;

@Property private ObjectId sentBy;
@Property private ObjectId messageThreadId;
@Property private ObjectId sentTo;
@Property private Date sentTime;
@Property private String subject;
@Property private String recipients;
@Property private String body;
@Property private String senderName;
@Property private String senderType ;

@Embedded("attachmentList")
List<DBAttachement> attachementList = new ArrayList<DBAttachement>();

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
	public ObjectId getSentBy() {
		return sentBy;
	}
	public void setSentBy(ObjectId sentBy) {
		this.sentBy = sentBy;
	}
	public ObjectId getSentTo() {
		return sentTo;
	}
	public void setSentTo(ObjectId sentTo) {
		this.sentTo = sentTo;
	}
	public Date getSentTime(){
		return sentTime;
	}
	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}
	public String getRecipients() {
		return recipients;
	}
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public ObjectId getMessageThreadId() {
		return messageThreadId;
	}
	public void setMessageThreadId(ObjectId messageThreadId) {
		this.messageThreadId = messageThreadId;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public List<DBAttachement> getAttachementList() {
		return attachementList;
	}
	public void setAttachementList(List<DBAttachement> attachmentList) {
		this.attachementList = attachmentList;
	}
	public String getSenderType() {
		return senderType;
	}
	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}
}
