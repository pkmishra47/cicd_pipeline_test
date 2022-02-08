package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class DoctorVisit {
    public ObjectId id;
    public String doctorName; // This is referenced from DBDoctor
    public Date dateOfVisit;
    public MedicalCondition medicalCondition;
    public String restriction;
    public String notes;
    public List<Attachment> notesAttachementList;
    public String prescriptionNotes;
    public List<Attachment> prespAttachementList;
    public String source;
    public List<string> allergyName;
    public List<string> vaccination;
    public String patientComplaints;
    public Date followupDate;
    public PhysiologicalVitals physiologicalVitals;
    public Injury injury;
    public String observations;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Date getDateOfVisit() {
        return dateOfVisit;
    }

    public void setDateOfVisit(Date dateOfVisit) {
        this.dateOfVisit = dateOfVisit;
    }

    public MedicalCondition getMedicalCondition() {
        return medicalCondition;
    }

    public void setMedicalCondition(MedicalCondition medicalCondition) {
        this.medicalCondition = medicalCondition;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Attachment> getNotesAttachementList() {
        return notesAttachementList;
    }

    public void setNotesAttachementList(List<Attachment> notesAttachementList) {
        this.notesAttachementList = notesAttachementList;
    }

    public String getPrescriptionNotes() {
        return prescriptionNotes;
    }

    public void setPrescriptionNotes(String prescriptionNotes) {
        this.prescriptionNotes = prescriptionNotes;
    }

    public List<Attachment> getPrespAttachementList() {
        return prespAttachementList;
    }

    public void setPrespAttachementList(List<Attachment> prespAttachementList) {
        this.prespAttachementList = prespAttachementList;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<string> getAllergyName() {
        return allergyName;
    }

    public void setAllergyName(List<string> allergyName) {
        this.allergyName = allergyName;
    }

    public List<string> getVaccination() {
        return vaccination;
    }

    public void setVaccination(List<string> vaccination) {
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

    public PhysiologicalVitals getPhysiologicalVitals() {
        return physiologicalVitals;
    }

    public void setPhysiologicalVitals(PhysiologicalVitals physiologicalVitals) {
        this.physiologicalVitals = physiologicalVitals;
    }

    public Injury getInjury() {
        return injury;
    }

    public void setInjury(Injury injury) {
        this.injury = injury;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
}
