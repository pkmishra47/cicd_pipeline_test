package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "HomeHealth", noClassnameStored = true)
public class DBHomeHealth {
	@Id private ObjectId id;
	@Property private float temperature;
	@Property private float weight;
	@Property private int pulse;
	@Property private int bloodPressureSystolic;
	@Property private int bloodPressureDiastolic;
	@Property private int respiration;
	@Property private Boolean oxygenUse;
	@Property private int painScore;
	@Property private int spO2;
	@Property private String oxygenDevice;
	@Property private String remarks;
	@Property private Date checkupDate;
	@Property private int oxygenValue;
	
	
	public int getOxygenValue() {
		return oxygenValue;
	}
	public void setOxygenValue(int oxygenValue) {
		this.oxygenValue = oxygenValue;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public float getTemperature() {
		return temperature;
	}
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public int getPulse() {
		return pulse;
	}
	public void setPulse(int pulse) {
		this.pulse = pulse;
	}
	public int getBloodPressureSystolic() {
		return bloodPressureSystolic;
	}
	public void setBloodPressureSystolic(int bloodPressureSystolic) {
		this.bloodPressureSystolic = bloodPressureSystolic;
	}
	public int getBloodPressureDiastolic() {
		return bloodPressureDiastolic;
	}
	public void setBloodPressureDiastolic(int bloodPressureDiastolic) {
		this.bloodPressureDiastolic = bloodPressureDiastolic;
	}
	public int getRespiration() {
		return respiration;
	}
	public void setRespiration(int respiration) {
		this.respiration = respiration;
	}
	public Boolean getOxygenUse() {
		return oxygenUse;
	}
	public void setOxygenUse(Boolean oxygenUse) {
		this.oxygenUse = oxygenUse;
	}
	public int getPainScore() {
		return painScore;
	}
	public void setPainScore(int painScore) {
		this.painScore = painScore;
	}
	public int getSpO2() {
		return spO2;
	}
	public void setSpO2(int spO2) {
		this.spO2 = spO2;
	}
	public String getOxygenDevice() {
		return oxygenDevice;
	}
	public void setOxygenDevice(String oxygenDevice) {
		this.oxygenDevice = oxygenDevice;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public Date getCheckupDate() {
		return checkupDate;
	}
	public void setCheckupDate(Date checkupDate) {
		this.checkupDate = checkupDate;
	}	
}