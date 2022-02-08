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

@Entity(value = "HealthCheck", noClassnameStored = true)
public class DBHealthCheck implements Comparable<DBHealthCheck> {
    @Id
    private ObjectId id;
    @Property
    private String healthCheckName;
    @Property
    private Date healthCheckDate;
    //	@Property private Date appointmentDate;
    @Property
    private String healthCheckSummary;
    @Property
    private String clinicName;
    @Property
    private String source;
    @Property
    private String healthCheckType;
    @Property
    private Date followupDate;
    @Property
    private List<String> keywords;
    @Property
    private String billNo;
    @Property
    private String locId;

    @Embedded("healthCheckFiles")
    List<DBAttachement> healthCheckFilesList = new ArrayList<DBAttachement>();

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<DBAttachement> getHealthCheckFilesList() {
        return healthCheckFilesList;
    }

    public void setHealthCheckFilesList(List<DBAttachement> healthCheckFilesList) {
        this.healthCheckFilesList = healthCheckFilesList;
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

    @Override
    public int compareTo(DBHealthCheck dbHealthCheck) {
        if (dbHealthCheck == null)
            throw new IllegalArgumentException("DBHealthCheck is Null");

        return Comparators.stringCompare(id.toString(), dbHealthCheck.getId().toString());
    }

    //	public Date getAppointmentDate() {
//		return appointmentDate;
//	}
//	public void setAppointmentDate(Date appointmentDate) {
//		this.appointmentDate = appointmentDate;
//	}
    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getLocId() {
        return locId;
    }

    public void setLocId(String locId) {
        this.locId = locId;
    }
}