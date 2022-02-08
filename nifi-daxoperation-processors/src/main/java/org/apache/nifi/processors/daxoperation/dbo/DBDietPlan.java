package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "DietPlan", noClassnameStored = true)
public class DBDietPlan {
	@Id private ObjectId id;
	@Property private String uhid;
	@Property private Date date;
	@Property private String age;
	@Property private String protein;
	@Property private String fat;
	@Property private String energy;
	@Property private String carbohydrates;
	@Property private String weight;
	@Property private String height;
	@Property private String bmi;
	@Property private String diagnosis;
	@Property private String comments;
	@Property private String dietician;
	@Property private String consultant;
	@Embedded private Map<String,String> fooditems = new HashMap<String, String>();
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
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getProtein() {
		return protein;
	}
	public void setProtein(String protein) {
		this.protein = protein;
	}
	public String getFat() {
		return fat;
	}
	public void setFat(String fat) {
		this.fat = fat;
	}
	public String getEnergy() {
		return energy;
	}
	public void setEnergy(String energy) {
		this.energy = energy;
	}
	public String getCarbohydrates() {
		return carbohydrates;
	}
	public void setCarbohydrates(String carbohydrates) {
		this.carbohydrates = carbohydrates;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getBmi() {
		return bmi;
	}
	public void setBmi(String bmi) {
		this.bmi = bmi;
	}
	public String getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getDietician() {
		return dietician;
	}
	public void setDietician(String dietician) {
		this.dietician = dietician;
	}
	public String getConsultant() {
		return consultant;
	}
	public void setConsultant(String consultant) {
		this.consultant = consultant;
	}
	public Map<String,String> getFooditems() {
		return fooditems;
	}
	public void setFooditems(Map<String,String> fooditems) {
		this.fooditems = fooditems;
	}
}