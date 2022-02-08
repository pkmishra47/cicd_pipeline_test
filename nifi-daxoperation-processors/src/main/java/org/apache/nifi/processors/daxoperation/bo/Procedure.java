package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class Procedure {
    public ObjectId id;
    public String procedureName;
    public Date startDate;
    public Date endDate;
    public String doctorTreated;
    public String notes;
    public List<Attachment> procedureFiles;
    public String source;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Attachment> getProcedureFiles() {
        return procedureFiles;
    }

    public void setProcedureFiles(List<Attachment> procedureFiles) {
        this.procedureFiles = procedureFiles;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
