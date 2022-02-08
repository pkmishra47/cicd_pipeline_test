package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;



@Entity(value = "DoctorVisit", noClassnameStored = true)
public class DBDoctorVisit  implements Comparable<DBDoctorVisit>{
	@Id private ObjectId id;
	@Property private String doctorName;// This is referenced from DBDoctor
	@Property private List<String> allergyName;
	@Property private List<String> vaccination;
	@Property private String patientComplaints;
	@Property private Date followupDate;
	@Property private Date dateOfVisit;
	@Property private String observations;
	@Reference private DBMedicalCondition medicalCondition;
	@Embedded("physiologicalVitals") DBPhysiologicalVitals physiologicalVitals;
	@Embedded("injury") DBInjury injury;
	@Property private String restriction;	
	@Property private String notes;
	@Embedded("notesAttachement")
	List<DBAttachement> notesAttachementList = new ArrayList<DBAttachement>();
	@Property private String prescriptionNotes;
	@Embedded("prespAttachemen")
	List<DBAttachement> prespAttachementList = new ArrayList<DBAttachement>();
	@Property private String source;
	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public ObjectId getId() {
		return id;
	}
	public void setMedicalCondition(DBMedicalCondition medicalCondition) {
		this.medicalCondition = medicalCondition;
	}
	
	public DBMedicalCondition getMedicalCondition() {
		return medicalCondition;
	}
	
	public String getRestriction() {
		return restriction;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public void setNotesAttachementList(List<DBAttachement> attachList) {
		this.notesAttachementList = attachList;
	}
	
	public List<DBAttachement> getNotesAttachementList() {
		return notesAttachementList;
	}
	
	public void addNotesAttachement(DBAttachement attachment) {
		this.notesAttachementList.add(attachment);
	}

	public void setPrescAttachementList(List<DBAttachement> attachList) {
		this.prespAttachementList = attachList;
	}
	
	public List<DBAttachement> getPrescAttachmentList() {
		return prespAttachementList;
	}
	
	public void addPresAttachment(DBAttachement attachment) {
		this.prespAttachementList.add(attachment);
	}

	public void setPrescriptionNotes(String prescriptionNotes) {
		this.prescriptionNotes = prescriptionNotes;
	}

	public String getPrescriptionNotes() {
		return prescriptionNotes;
	}	
	@Override
	public int compareTo(DBDoctorVisit arg0) {
		if(arg0== null)
			throw new IllegalArgumentException("DBLabTest is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public DBPhysiologicalVitals getPhysiologicalVitals() {
		return physiologicalVitals;
	}

	public void setPhysiologicalVitals(DBPhysiologicalVitals physiologicalVitals) {
		this.physiologicalVitals = physiologicalVitals;
	}

	public DBInjury getInjury() {
		return injury;
	}

	public void setInjury(DBInjury injury) {
		this.injury = injury;
	}

	public List<String> getAllergyName() {
		return allergyName;
	}

	public void setAllergyName(List<String> allergyName) {
		this.allergyName = allergyName;
	}

	public List<String> getVaccination() {
		return vaccination;
	}

	public void setVaccination(List<String> vaccination) {
		this.vaccination = vaccination;
	}

	public String getPatientComplaints() {
		return patientComplaints;
	}

	public void setPatientComplaints(String patientComplaints) {
		this.patientComplaints = patientComplaints;
	}

	public Date getFollowupDate() {
		return followupDate;
	}

	public void setFollowupDate(Date followupDate) {
		this.followupDate = followupDate;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Date getDateOfVisit() {
		return dateOfVisit;
	}

	public void setDateOfVisit(Date dateOfVisit) {
		this.dateOfVisit = dateOfVisit;
	}

}