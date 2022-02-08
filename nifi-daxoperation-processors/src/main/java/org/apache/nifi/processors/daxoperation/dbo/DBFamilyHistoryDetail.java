package org.apache.nifi.processors.daxoperation.dbo;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBFamilyHistoryDetail {
	
	@Property private String familyHistoryType;
	@Property private String familyMember;
	@Property private String notes;
	@Property private int age;
	
	public String getFamilyHistoryType() {
		return familyHistoryType;
	}
	public void setFamilyHistoryType(String familyHistoryType) {
		this.familyHistoryType = familyHistoryType;
	}
	public String getFamilyMember() {
		return familyMember;
	}
	public void setFamilyMember(String familyMember) {
		this.familyMember = familyMember;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
}
