package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class HealthCheck {
    public ObjectId id;
    public String healthCheckName;
    public Date healthCheckDate;
    public String healthCheckSummary;
    public List<Attachment> healthCheckFiles;
    public String source;
    public String healthCheckType;
    public Date followupDate;
    public List<String> keywords;
    public Date appointmentDate;
    public String clinicName;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getHealthCheckName() {
        return healthCheckName;
    }

    public void setHealthCheckName(String healthCheckName) {
        this.healthCheckName = healthCheckName;
    }

    public Date getHealthCheckDate() {
        return healthCheckDate;
    }

    public void setHealthCheckDate(Date healthCheckDate) {
        this.healthCheckDate = healthCheckDate;
    }

    public String getHealthCheckSummary() {
        return healthCheckSummary;
    }

    public void setHealthCheckSummary(String healthCheckSummary) {
        this.healthCheckSummary = healthCheckSummary;
    }

    public List<Attachment> getHealthCheckFiles() {
        return healthCheckFiles;
    }

    public void setHealthCheckFiles(List<Attachment> healthCheckFiles) {
        this.healthCheckFiles = healthCheckFiles;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHealthCheckType() {
        return healthCheckType;
    }

    public void setHealthCheckType(String healthCheckType) {
        this.healthCheckType = healthCheckType;
    }

    public Date getFollowupDate() {
        return followupDate;
    }

    public void setFollowupDate(Date followupDate) {
        this.followupDate = followupDate;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }
}
