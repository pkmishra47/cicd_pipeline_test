package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "CallInfo", noClassnameStored = true)
public class DBCallInfo {
	@Id private ObjectId id;
	@Property private String sno;
	@Property private String calendarEvent;
	@Property private Date interactionDate;
	@Property private String eventType;
	@Property private String callStatus;
	@Property private String calledByUser;
	@Property private String comment;
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getSno() {
		return sno;
	}
	public void setSno(String sno) {
		this.sno = sno;
	}
	public String getCalendarEvent() {
		return calendarEvent;
	}
	public void setCalendarEvent(String calendarEvent) {
		this.calendarEvent = calendarEvent;
	}
	public Date getInteractionDate() {
		return interactionDate;
	}
	public void setInteractionDate(Date interactionDate) {
		this.interactionDate = interactionDate;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getCallStatus() {
		return callStatus;
	}
	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}
	public String getCalledByUser() {
		return calledByUser;
	}
	public void setCalledByUser(String calledByUser) {
		this.calledByUser = calledByUser;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}