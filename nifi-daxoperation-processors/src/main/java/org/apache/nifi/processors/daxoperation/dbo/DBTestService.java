package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "TestService", noClassnameStored = true)
public class DBTestService 
{
	@Id private ObjectId id;
	@Property private String testServiceName;
	@Property private int testServiceId;
	@Property private String department;
	@Property private String siteKey;
	@Property private int turnArroundTime;
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getTestServiceName() {
		return testServiceName;
	}
	public void setTestServiceName(String testServiceName) {
		this.testServiceName = testServiceName;
	}
	
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getSiteKey() {
		return siteKey;
	}
	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}
	public int getTurnArroundTime() {
		return turnArroundTime;
	}
	public void setTurnArroundTime(int turnArroundTime) {
		this.turnArroundTime = turnArroundTime;
	}
	public int getTestServiceId() {
		return testServiceId;
	}
	public void setTestServiceId(int testServiceId) {
		this.testServiceId = testServiceId;
	}
	
	

}
