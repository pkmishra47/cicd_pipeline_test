package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "Reminder", noClassnameStored = true)
public class DBReminder implements Comparable<DBReminder>{
	@Id private ObjectId id;
	@Property private String reminderName;
	@Property private String reminderDesc;
	@Property private Date duetime;
	@Property private Date reminderNotifyTime;
	@Property private String notifyBy;
	@Property private int reminderStatus;  // Note : Mani -- This should be converted to Enum so we know we don't get junk values
										   // There should be a default value for this reminder, and using reminder status jobs to be triggered

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getReminderName() {
		return reminderName;
	}

	public void setReminderName(String reminderName) {
		this.reminderName = reminderName;
	}

	public String getReminderDesc() {
		return reminderDesc;
	}

	public void setReminderDesc(String reminderDesc) {
		this.reminderDesc = reminderDesc;
	}

	public Date getDuetime() {
		return duetime;
	}

	public void setDuetime(Date duetime) {
		this.duetime = duetime;
	}
	
	public int getReminderStatus() {
		return reminderStatus;
	}

	public void setReminderStatus(int reminderStatus) {
		this.reminderStatus = reminderStatus;
	}

	@Override
	public int compareTo(DBReminder arg0) {
		if(arg0== null)
			throw new IllegalArgumentException("DBReminder is Null");

		return Comparators.stringCompare(getId().toString(), arg0.getId().toString());
	}

	public Date getReminderNotifyTime() {
		return reminderNotifyTime;
	}

	public void setReminderNotifyTime(Date reminderNotifyTime) {
		this.reminderNotifyTime = reminderNotifyTime;
	}

	public String getNotifyBy() {
		return notifyBy;
	}

	public void setNotifyBy(String notifyBy) {
		this.notifyBy = notifyBy;
	}
	
}
