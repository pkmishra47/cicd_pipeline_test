package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBMedicalCondition;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class MedicalCondition {
    public ObjectId id;
    public String medicalConditionName;
    public Date startDate;
    public Date endDate;
    public String doctorTreated;
    public DBMedicalCondition.IllnessType illnessType;
    public String notes;
    public List<Attachment> medicationFiles;
    public Boolean havingNow;
    public String source;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMedicalConditionName() {
        return medicalConditionName;
    }

    public void setMedicalConditionName(String medicalConditionName) {
        this.medicalConditionName = medicalConditionName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDoctorTreated() {
        return doctorTreated;
    }

    public void setDoctorTreated(String doctorTreated) {
        this.doctorTreated = doctorTreated;
    }

    public DBMedicalCondition.IllnessType getIllnessType() {
        return illnessType;
    }

    public void setIllnessType(DBMedicalCondition.IllnessType illnessType) {
        this.illnessType = illnessType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Attachment> getMedicationFiles() {
        return medicationFiles;
    }

    public void setMedicationFiles(List<Attachment> medicationFiles) {
        this.medicationFiles = medicationFiles;
    }

    public Boolean getHavingNow() {
        return havingNow;
    }

    public void setHavingNow(Boolean havingNow) {
        this.havingNow = havingNow;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
