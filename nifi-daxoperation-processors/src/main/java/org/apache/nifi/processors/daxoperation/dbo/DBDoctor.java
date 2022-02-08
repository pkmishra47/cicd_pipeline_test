package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Doctor", noClassnameStored = true)
public class DBDoctor {
	@Id private ObjectId id;
	@Property private String firstName; 
	@Property private String middleName;
	@Property private String lastName;
	@Property private String specialization;
	@Property private String entity;	
	@Property private String email;
	@Property private String emergencyContact_1;
	@Property private String emergencyContact_2;
	@Property private String personalMobile_1;
	@Property private String personalMobile_2;
	
	@Embedded("educations")
	private List<DBEducation> educations = new ArrayList<DBEducation>();

	@Reference("hospitalAssociated")
	private List<DBHospital> hospitalAssociated = new ArrayList<DBHospital>();
	
	@Embedded("visitingSchedule")
	private List<DBVisitingSchedule> visitingSchedule = new ArrayList<DBVisitingSchedule>();

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getSpecialization() {
		return specialization;
	}
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public List<DBEducation> getEducations() {
		return educations;
	}
	public void setEducations(List<DBEducation> educations) {
		this.educations = educations;
	}
	public List<DBHospital> getHospitalAssociated() {
		return hospitalAssociated;
	}
	public void setHospitalAssociated(List<DBHospital> hospitalAssociated) {
		this.hospitalAssociated = hospitalAssociated;
	}
	public List<DBVisitingSchedule> getVisitingSchedule() {
		return visitingSchedule;
	}
	public void setVisitingSchedule(List<DBVisitingSchedule> visitingSchedule) {
		this.visitingSchedule = visitingSchedule;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmergencyContact_1() {
		return emergencyContact_1;
	}
	public void setEmergencyContact_1(String emergencyContact_1) {
		this.emergencyContact_1 = emergencyContact_1;
	}
	public String getEmergencyContact_2() {
		return emergencyContact_2;
	}
	public void setEmergencyContact_2(String emergencyContact_2) {
		this.emergencyContact_2 = emergencyContact_2;
	}
	public String getPersonalMobile_1() {
		return personalMobile_1;
	}
	public void setPersonalMobile_1(String personalMobile_1) {
		this.personalMobile_1 = personalMobile_1;
	}
	public String getPersonalMobile_2() {
		return personalMobile_2;
	}
	public void setPersonalMobile_2(String personalMobile_2) {
		this.personalMobile_2 = personalMobile_2;
	}	
}
