package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "LabResultDownload", noClassnameStored = true)
public class DBLabResultDownload {

    @Id
    private ObjectId id;
    @Property
    private ObjectId userObjectId;
    @Property
    private String otp;
    @Property
    private String orderId;
    @Property
    private Date createdDate;
    @Property
    private String mobileNumber;
    @Property
    private String downloadId;
    @Property
    private Date otpDate;
    @Property
    private Integer otpAttempts;
    @Property
    private String identifier;
    @Property
    private boolean isSmartReport;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ObjectId getUserObjectId() {
        return userObjectId;
    }

    public void setUserObjectId(ObjectId userObjectId) {
        this.userObjectId = userObjectId;
    }

    public Integer getOtpAttempts() {
        return otpAttempts;
    }

    public void setOtpAttempts(Integer otpAttempts) {
        this.otpAttempts = otpAttempts;
    }

    public Date getOtpDate() {
        return otpDate;
    }

    public void setOtpDate(Date otpDate) {
        this.otpDate = otpDate;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isSmartReport() {
        return isSmartReport;
    }

    public void setSmartReport(boolean smartReport) {
        isSmartReport = smartReport;
    }
}
