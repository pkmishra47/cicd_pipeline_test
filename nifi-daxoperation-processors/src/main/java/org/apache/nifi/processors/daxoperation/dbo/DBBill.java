package org.apache.nifi.processors.daxoperation.dbo;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(value = "Bill", noClassnameStored = true)
public class DBBill {
	@Id private ObjectId id;
	@Property private String bill_no;
	@Property private Date billDate;
	@Property private Date createdDateTime;
	@Property private Date updatedDateTime;
	@Property private String hospitalName;
	@Property private String source;
	@Property private String mobileNumber;
	@Property private String notes;


	@Embedded("billFiles")
	List<DBAttachement> billFilesList = new ArrayList<>();

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getBillNo() {
		return bill_no;
	}

	public void setBillNo(String billNo) {
		this.bill_no = billNo;
	}

	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public String getHospitalName() {
		return hospitalName;
	}

	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<DBAttachement> getBillFilesList() {
		return billFilesList;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Date getCreatedDatetime() {
		return createdDateTime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDateTime = createdDatetime;
	}

	public void setBillFilesList(List<DBAttachement> billFilesList) {
		this.billFilesList = billFilesList;
	}

	public Date getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(Date updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
}