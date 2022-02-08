package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;

@Entity(value = "PrescriptionLead", noClassnameStored = true)
public class DBPrescriptionLead {

    @Id
    private ObjectId id;

    public enum ProcessStatus {
        PROCESSED,
        NOTPROCESSED
    }

    @Reference
    private DBUser dbUser;

    @Reference
    private DBPrescription dbPrescription;

    @Property private String mobileNumber;

    @Property private String uhid;

    @Property private String hospitalId;

    @Property private DBPrescriptionLead.ProcessStatus processedStatus;

    @Property private Date createdDate;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public DBUser getDbUser() {
        return dbUser;
    }

    public void setDbUser(DBUser dbUser) {
        this.dbUser = dbUser;
    }

    public DBPrescription getDbPrescription() {
        return dbPrescription;
    }

    public void setDbPrescription(DBPrescription dbPrescription) {
        this.dbPrescription = dbPrescription;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public ProcessStatus getProcessedStatus() {
        return processedStatus;
    }

    public void setProcessedStatus(ProcessStatus processedStatus) {
        this.processedStatus = processedStatus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }
}
