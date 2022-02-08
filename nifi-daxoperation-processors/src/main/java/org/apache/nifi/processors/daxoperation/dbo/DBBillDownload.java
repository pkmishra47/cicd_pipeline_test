package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;

@Entity(value = "BillDownload", noClassnameStored = true)
public class DBBillDownload {

	@Id private ObjectId id;

	@Property private ObjectId userObjectId;
	@Property private String otp;
	@Property private String bill_no;
	@Property private Date createdDate;
	@Property private String mobileNumber;
	@Property private String downloadId;
	@Property private Date otpDate;
	@Property private Integer otpAttempts;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getUserObjectId() {
		return userObjectId;
	}

	public void setUserObjectId(ObjectId userObjectId) {
		this.userObjectId = userObjectId;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getBill_no() {
		return bill_no;
	}

	public void setBill_no(String bill_no) {
		this.bill_no = bill_no;
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

	public Date getOtpDate() {
		return otpDate;
	}

	public void setOtpDate(Date otpDate) {
		this.otpDate = otpDate;
	}

	public Integer getOtpAttempts() {
		return otpAttempts;
	}

	public void setOtpAttempts(Integer otpAttempts) {
		this.otpAttempts = otpAttempts;
	}
}
