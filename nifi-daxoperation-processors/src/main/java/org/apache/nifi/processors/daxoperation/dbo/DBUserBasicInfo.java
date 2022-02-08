package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Embedded
public class DBUserBasicInfo implements Comparable<DBUserBasicInfo>{
	@Id ObjectId id = new ObjectId();
	@Property private Date dateOfBirth;
	@Property private int age;
	@Property private String sex;
	@Property private String bloodGroup;    // got from master data
	@Property private String martialStatus; // got from master data
	@Property private String diabetesRegimen;
	@Property private String edocReferenceId;
	@Property private String employeeId; 
	@Property private String employeeStatus; 
	@Property private String groupName;
	@Property private String entityUserType;
	
	
	@Embedded
	List<DBEducation> educationDetailList = new ArrayList<DBEducation>();
	
	@Embedded
	List<DBOccupation> occupationDetailList = new ArrayList<DBOccupation>();
	
	@Embedded("profileImage")
	DBAttachement profileImage = new DBAttachement();
	
	public DBAttachement getProfileImage() {
		return profileImage;
	}
	public void setProfileImage(DBAttachement profileImage) {
		this.profileImage = profileImage;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getBloodGroup() {
		return bloodGroup;
	}

	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}

	public String getMartialStatus() {
		return martialStatus;
	}

	public void setMartialStatus(String martialStatus) {
		this.martialStatus = martialStatus;
	}

	public List<DBEducation> getEducationDetailList() {
		return educationDetailList;
	}
	
	public void setEducationDetailList(List<DBEducation> educationDetailList) {
		this.educationDetailList = educationDetailList;
	}
	
	public void addEducationDetail(DBEducation edu) {
		educationDetailList.add(edu);
	}
	
	public List<DBOccupation> getOccupationDetailList() {
		return occupationDetailList;
	}
	
	public void setOccupationDetailList(List<DBOccupation> occupationDetailList) {
		this.occupationDetailList = occupationDetailList;
	}
	
	public void addOccupationDetail(DBOccupation occupation) {
		this.occupationDetailList.add(occupation);
	}
	
	@Override
	public int compareTo(DBUserBasicInfo arg0) {
		if(arg0 == null)
			throw new IllegalArgumentException("DBUserBasicInfo is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getDiabetesRegimen() {
		return diabetesRegimen;
	}
	public void setDiabetesRegimen(String diabetesRegimen) {
		this.diabetesRegimen = diabetesRegimen;
	}
	public String getEdocReferenceId() {
		return edocReferenceId;
	}
	public void setEdocReferenceId(String edocReferenceId) {
		this.edocReferenceId = edocReferenceId;
	}
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getEmployeeStatus() {
		return employeeStatus;
	}
	public void setEmployeeStatus(String employeeStatus) {
		this.employeeStatus = employeeStatus;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getEntityUserType() {
		return entityUserType;
	}
	public void setEntityUserType(String entityUserType) {
		this.entityUserType = entityUserType;
	}
	
	
}