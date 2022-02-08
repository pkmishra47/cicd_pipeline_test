package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "HRACancer", noClassnameStored = true)
public class DBHRACancer {
	@Id private ObjectId id;
	@Property private String uhid;
	@Property private int age;
	@Property private boolean doSmoke;
	@Property private int exercideDuration;
	@Property private boolean hadEarlierCardicArrest;
	@Property private int cancerRiskFactor; 
	
	public static int NO_RISK = 0;
	public static int LOW_RISK = 1;
	public static int MODERATE_RISK = 2;
	public static int HIGH_RISK = 3;
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public boolean isDoSmoke() {
		return doSmoke;
	}
	public void setDoSmoke(boolean doSmoke) {
		this.doSmoke = doSmoke;
	}
	public int getExercideDuration() {
		return exercideDuration;
	}
	public void setExercideDuration(int exercideDuration) {
		this.exercideDuration = exercideDuration;
	}
	public boolean isHadEarlierCardicArrest() {
		return hadEarlierCardicArrest;
	}
	public void setHadEarlierCardicArrest(boolean hadEarlierCardicArrest) {
		this.hadEarlierCardicArrest = hadEarlierCardicArrest;
	}
	public int getCancerRiskFactor() {
		return this.cancerRiskFactor;
	}
	public void setCancerRiskFactor(int cancerRiskFactor) {
		this.cancerRiskFactor = cancerRiskFactor;
	}
	
	public String toString() {
	    StringBuilder result = new StringBuilder();
	    String NEW_LINE = System.getProperty("line.separator");
	    result.append(this.getClass().getName() + " Object {" + NEW_LINE);
	    result.append(" age: " + age + NEW_LINE);
	    result.append(" doSmoke: " + doSmoke + NEW_LINE);
	    result.append(" exercideDuration: " + exercideDuration + NEW_LINE);
	    result.append(" cancerRiskFactor: " + cancerRiskFactor + NEW_LINE);
		result.append("}");
	    return result.toString();					
		}

	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}


}