package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "AgentCmd", noClassnameStored = true)
public class DBAgentCmd implements Comparable<DBAgentCmd> {
	@Id private ObjectId id;
	@Property private String siteKey;
	@Property private String jobName;
	@Property private String jobClass;
	@Property private boolean tobeSent;
	@Property private String cmdData;
	@Property private Date sentTime;
	@Property private int noOfTimesSent;
	@Property private boolean recived;
	@Property private Date recieveTime;
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getSiteKey() {
		return siteKey;
	}
	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobClass() {
		return jobClass;
	}
	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}
	public String getCmdData() {
		return cmdData;
	}
	public void setCmdData(String cmdData) {
		this.cmdData = cmdData;
	}
	public Date getSentTime() {
		return sentTime;
	}
	public boolean isTobeSent() {
		return tobeSent;
	}
	public void setTobeSent(boolean tobeSent) {
		this.tobeSent = tobeSent;
	}
	public void setSentTime(Date sentTime) {
		this.sentTime = sentTime;
	}
	public int getNoOfTimesSent() {
		return noOfTimesSent;
	}
	public void setNoOfTimesSent(int noOfTimesSent) {
		this.noOfTimesSent = noOfTimesSent;
	}
	@Override
	public int compareTo(DBAgentCmd o) {
		// A null check here would be expensive, and mostly we would not have null
		// because its collections that are working internally.
		if(o == null)
			throw new IllegalArgumentException("DBAgentCmd is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}
	public boolean isRecived() {
		return recived;
	}
	public void setRecived(boolean recived) {
		this.recived = recived;
	}
	public Date getRecieveTime() {
		return recieveTime;
	}
	public void setRecieveTime(Date recieveTime) {
		this.recieveTime = recieveTime;
	}
}
