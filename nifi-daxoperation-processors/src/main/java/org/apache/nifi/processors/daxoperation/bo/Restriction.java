package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBRestriction;
import org.bson.types.ObjectId;

import java.util.Date;

public class Restriction {
    public ObjectId id;
    public String restrictionName;
    public DBRestriction.RestrictionType nature;
    public String suggestedByDoctor;
    public Date startDate;
    public Date endDate;
    public String notes;
    public Boolean havingNow;
    public String source;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getRestrictionName() {
        return restrictionName;
    }

    public void setRestrictionName(String restrictionName) {
        this.restrictionName = restrictionName;
    }

    public DBRestriction.RestrictionType getNature() {
        return nature;
    }

    public void setNature(DBRestriction.RestrictionType nature) {
        this.nature = nature;
    }

    public String getSuggestedByDoctor() {
        return suggestedByDoctor;
    }

    public void setSuggestedByDoctor(String suggestedByDoctor) {
        this.suggestedByDoctor = suggestedByDoctor;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
