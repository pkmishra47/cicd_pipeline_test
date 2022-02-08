package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBAllergy;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class Allergy {
    public ObjectId id;
    public String allergyName;
    public Boolean havingNow;
    public Date startDate;
    public Date endDate;
    public DBAllergy.Severity severity;
    public String doctorTreated;
    public String reactionToAllergy;
    public String notes;
    public List<Attachment> attachmentList;
    public String source;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getAllergyName() {
        return allergyName;
    }

    public void setAllergyName(String allergyName) {
        this.allergyName = allergyName;
    }

    public Boolean getHavingNow() {
        return havingNow;
    }

    public void setHavingNow(Boolean havingNow) {
        this.havingNow = havingNow;
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

    public DBAllergy.Severity getSeverity() {
        return severity;
    }

    public void setSeverity(DBAllergy.Severity severity) {
        this.severity = severity;
    }

    public String getDoctorTreated() {
        return doctorTreated;
    }

    public void setDoctorTreated(String doctorTreated) {
        this.doctorTreated = doctorTreated;
    }

    public String getReactionToAllergy() {
        return reactionToAllergy;
    }

    public void setReactionToAllergy(String reactionToAllergy) {
        this.reactionToAllergy = reactionToAllergy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
