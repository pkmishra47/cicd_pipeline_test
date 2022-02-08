package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class Insurance {
    public ObjectId id;
    public String insuranceCompany;
    public String policyNumber;
    public Double sumInsured;
    public Date startDate;
    public Date endDate;
    public Boolean isActive;
    public List<Attachment> insuranceFiles;
    public String source;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Double getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Double sumInsured) {
        this.sumInsured = sumInsured;
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<Attachment> getInsuranceFiles() {
        return insuranceFiles;
    }

    public void setInsuranceFiles(List<Attachment> insuranceFiles) {
        this.insuranceFiles = insuranceFiles;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
