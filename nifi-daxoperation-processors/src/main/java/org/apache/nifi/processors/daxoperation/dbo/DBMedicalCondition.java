package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;


@Entity(value = "MedicalCondition", noClassnameStored = true)
public class DBMedicalCondition implements Comparable<DBMedicalCondition>{
	@Id private ObjectId id;
	@Property private String medicalCondition;
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private String doctorTreated;
	@Property private IllnessType illnessType;
	@Property private String notes;
	@Property private boolean havingNow;
	@Property private String source;


	@Embedded("medicationFiles")
	List<DBAttachement> medicationFiles = new ArrayList<DBAttachement>();

	public static enum IllnessType {
		Acute,
		Chronic, 
		Recurring, 
		Intermittent
	}
	
	public boolean isHavingNow() {
		return havingNow;
	}

	public void setHavingNow(boolean havingNow) {
		this.havingNow = havingNow;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public ObjectId getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public void setMedicationFiles(List<DBAttachement> attachList) {
		this.medicationFiles = attachList;
	}
	
	public List<DBAttachement> getMedicationFiles() {
		return medicationFiles;
	}
	
	public void addMedicationFile(DBAttachement attachment) {
		this.medicationFiles.add(attachment);
	}

	public void setMedicalCondition(String medicalCondition) {
		this.medicalCondition = medicalCondition;
	}

	public String getMedicalCondition() {
		return medicalCondition;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setDoctorTreated(String doctorTreated) {
		this.doctorTreated = doctorTreated;
	}

	public String getDoctorTreated() {
		return doctorTreated;
	}

	public void setIllnessType(IllnessType illnessType) {
		this.illnessType = illnessType;
	}

	public IllnessType getIllnessType() {
		return illnessType;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}
	
	@Override
	public int compareTo(DBMedicalCondition o) {
		if(o == null)
			throw new IllegalArgumentException("DBMedicalCondition is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}

}
