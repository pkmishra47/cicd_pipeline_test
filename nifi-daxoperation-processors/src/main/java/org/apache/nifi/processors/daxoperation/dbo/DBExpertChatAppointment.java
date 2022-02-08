package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "ExpertChatAppointment", noClassnameStored = true)
public class DBExpertChatAppointment implements Comparable<DBExpertChatAppointment>{
	@Id private ObjectId id;
  	@Property private String expertChatType;
  	@Property private Date chatDate;
  	@Property private String slotTime;
  	@Property private int duration;
  	@Property private String chatUserId;
  	@Property private boolean chatCompleted;
  	@Property private String conversationLog;
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getExpertChatType() {
		return expertChatType;
	}
	public void setExpertChatType(String expertChatType) {
		this.expertChatType = expertChatType;
	}
	public Date getChatDate() {
		return chatDate;
	}
	public void setChatDate(Date chatDate) {
		this.chatDate = chatDate;
	}
	public String getSlotTime() {
		return slotTime;
	}
	public void setSlotTime(String slotTime) {
		this.slotTime = slotTime;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getChatUserId() {
		return chatUserId;
	}
	public void setChatUserId(String chatUserId) {
		this.chatUserId = chatUserId;
	}
	public boolean isChatCompleted() {
		return chatCompleted;
	}
	public void setChatCompleted(boolean chatCompleted) {
		this.chatCompleted = chatCompleted;
	}
	public String getConversationLog() {
		return conversationLog;
	}
	public void setConversationLog(String conversationLog) {
		this.conversationLog = conversationLog;
	}
	@Override
	public int compareTo(DBExpertChatAppointment arg0) {
		if(arg0== null)
			throw new IllegalArgumentException("DBReminder is Null");

		return Comparators.stringCompare(getId().toString(), arg0.getId().toString());
	}  
}