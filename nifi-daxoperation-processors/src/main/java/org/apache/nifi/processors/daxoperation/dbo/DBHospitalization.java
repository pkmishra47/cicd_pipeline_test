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

@Entity(value = "Hospitalization", noClassnameStored = true)
public class DBHospitalization   implements Comparable<DBHospitalization>{
	@Id private ObjectId id;
	@Property private Date dateOfHospitilization;
	@Property private String hospitalName;
	@Property private String doctorName;
	@Property private String reasonForAdmission;
	@Property private String diagnosisNotes;
	@Property private Date dateOfDischarge;
	@Property private String dischargeSummary;
	@Property private String doctorInstruction;
	@Property private Date dateOfNextVisit;
	@Embedded("hospitalizationFiles")
	List<DBAttachement> hospitalizationFiles = new ArrayList<DBAttachement>();
	@Property private String source;
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public ObjectId getId() {
		return id;
	}

	public void setDateOfHospitilization(Date dateOfHospitilization) {
		this.dateOfHospitilization = dateOfHospitilization;
	}

	public Date getDateOfHospitilization() {
		return dateOfHospitilization;
	}

	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}

	public String getHospitalName() {
		return hospitalName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setReasonForAdmission(String reasonForAdmission) {
		this.reasonForAdmission = reasonForAdmission;
	}

	public String getReasonForAdmission() {
		return reasonForAdmission;
	}

	public void setDiagnosisNotes(String diagnosisNotes) {
		this.diagnosisNotes = diagnosisNotes;
	}

	public String getDiagnosisNotes() {
		return diagnosisNotes;
	}

	public void setDateOfDischarge(Date dateOfDischarge) {
		this.dateOfDischarge = dateOfDischarge;
	}

	public Date getDateOfDischarge() {
		return dateOfDischarge;
	}

	public void setDischargeSummary(String dischargeSummary) {
		this.dischargeSummary = dischargeSummary;
	}

	public String getDischargeSummary() {
		return dischargeSummary;
	}

	public void setDoctorInstruction(String doctorInstruction) {
		this.doctorInstruction = doctorInstruction;
	}

	public String getDoctorInstruction() {
		return doctorInstruction;
	}
	
	public void setHospitlizationFiles(List<DBAttachement> attachList) {
		this.hospitalizationFiles = attachList;
	}
	
	public List<DBAttachement> getHospitlizationFiles() {
		return hospitalizationFiles;
	}
	
	public void addHospitilizationFile(DBAttachement attachment) {
		this.hospitalizationFiles.add(attachment);
	}

	public void setDateOfNextVisit(Date dateOfNextVisit) {
		this.dateOfNextVisit = dateOfNextVisit;
	}

	public Date getDateOfNextVisit() {
		return dateOfNextVisit;
	}
	
	@Override
	public int compareTo(DBHospitalization o) {
		if(o == null)
			throw new IllegalArgumentException("DBRestriction is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	
}
