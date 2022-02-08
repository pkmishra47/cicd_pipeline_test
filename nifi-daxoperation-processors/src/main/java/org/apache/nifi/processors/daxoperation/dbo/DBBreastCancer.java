package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "BreastCancer", noClassnameStored = true)
public class DBBreastCancer {
	@Id private ObjectId id;
	@Property private String uhid;
	@Property private int age;
	@Property private boolean gender;
	@Property private boolean priorHistoryOfCancer;
	@Property private int ageWhenFirstMensural;
	@Property private int ageFirstLiveBirth;
	@Property private int noOfRelativeHavingCancer;
	@Property private boolean hadDoneBiopsy;
	@Property private int noOfBiopsy;
	@Property private boolean atyplBiopsy;
	@Property private int race;
	@Property private int subRace;
	@Property private int breastCancerRiskFactor;
	@Property private double riskPercentage;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getUhid() {
		return uhid;
	}
	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public boolean isGender() {
		return gender;
	}
	public void setGender(boolean gender) {
		this.gender = gender;
	}
	public boolean isPriorHistoryOfCancer() {
		return priorHistoryOfCancer;
	}
	public void setPriorHistoryOfCancer(boolean priorHistoryOfCancer) {
		this.priorHistoryOfCancer = priorHistoryOfCancer;
	}
	public int getAgeWhenFirstMensural() {
		return ageWhenFirstMensural;
	}
	public void setAgeWhenFirstMensural(int ageWhenFirstMensural) {
		this.ageWhenFirstMensural = ageWhenFirstMensural;
	}
	public int getAgeFirstLiveBirth() {
		return ageFirstLiveBirth;
	}
	public void setAgeFirstLiveBirth(int ageFirstLiveBirth) {
		this.ageFirstLiveBirth = ageFirstLiveBirth;
	}
	public int getNoOfRelativeHavingCancer() {
		return noOfRelativeHavingCancer;
	}
	public void setNoOfRelativeHavingCancer(int noOfRelativeHavingCancer) {
		this.noOfRelativeHavingCancer = noOfRelativeHavingCancer;
	}
	public boolean isHadDoneBiopsy() {
		return hadDoneBiopsy;
	}
	public void setHadDoneBiopsy(boolean hadDoneBiopsy) {
		this.hadDoneBiopsy = hadDoneBiopsy;
	}
	public int getNoOfBiopsy() {
		return noOfBiopsy;
	}
	public void setNoOfBiopsy(int noOfBiopsy) {
		this.noOfBiopsy = noOfBiopsy;
	}
	public boolean isAtyplBiopsy() {
		return atyplBiopsy;
	}
	public void setAtyplBiopsy(boolean atyplBiopsy) {
		this.atyplBiopsy = atyplBiopsy;
	}
	public int getRace() {
		return race;
	}
	public void setRace(int race) {
		this.race = race;
	}
	public int getSubRace() {
		return subRace;
	}
	public void setSubRace(int subRace) {
		this.subRace = subRace;
	}
	public int getBreastCancerRiskFactor() {
		return breastCancerRiskFactor;
	}
	public void setBreastCancerRiskFactor(int breastCancerRiskFactor) {
		this.breastCancerRiskFactor = breastCancerRiskFactor;
	}
	public double getRiskPercentage() {
		return riskPercentage;
	}
	public void setRiskPercentage(double riskPercentage) {
		this.riskPercentage = riskPercentage;
	}
}
