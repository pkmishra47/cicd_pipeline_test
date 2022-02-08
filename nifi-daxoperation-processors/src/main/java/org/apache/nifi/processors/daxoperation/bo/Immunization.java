package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

public class Immunization {
    public ObjectId id;
    public String immunizationName;
    public String orderedByDoctor;
    public Date dateOfImmunization;
    public Map<Date, string> immunizationDetail;
    public String notes;
    public String source;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getImmunizationName() {
        return immunizationName;
    }

    public void setImmunizationName(String immunizationName) {
        this.immunizationName = immunizationName;
    }

    public String getOrderedByDoctor() {
        return orderedByDoctor;
    }

    public void setOrderedByDoctor(String orderedByDoctor) {
        this.orderedByDoctor = orderedByDoctor;
    }

    public Date getDateOfImmunization() {
        return dateOfImmunization;
    }

    public void setDateOfImmunization(Date dateOfImmunization) {
        this.dateOfImmunization = dateOfImmunization;
    }

    public Map<Date, string> getImmunizationDetail() {
        return immunizationDetail;
    }

    public void setImmunizationDetail(Map<Date, string> immunizationDetail) {
        this.immunizationDetail = immunizationDetail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
