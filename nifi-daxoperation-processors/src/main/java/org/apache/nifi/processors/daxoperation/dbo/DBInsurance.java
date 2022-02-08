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


@Entity(value = "Insurance", noClassnameStored = true)
public class DBInsurance implements Comparable<DBInsurance>{
	@Id private ObjectId id;
	@Property private String insuranceCompany;
	@Property private String policyNumber;
	@Property private double sumInsured;
	@Property private String source;	
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private Boolean isActive;
	@Embedded("insuranceFiles")
	List<DBAttachement> insuranceFiles = new ArrayList<DBAttachement>();

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public ObjectId getId() {
		return id;
	}

	
	public void setInsuranceFiles(List<DBAttachement> attachList) {
		this.insuranceFiles = attachList;
	}
	
	public List<DBAttachement> getInsuranceFiles() {
		return insuranceFiles;
	}
	
	public void addInsuranceFile(DBAttachement attachment) {
		this.insuranceFiles.add(attachment);
	}

	public void setInsuranceCompany(String insuranceCompany) {
		this.insuranceCompany = insuranceCompany;
	}

	public String getInsuranceCompany() {
		return insuranceCompany;
	}

	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}

	public String getPolicyNumber() {
		return policyNumber;
	}

	public void setSumInsured(double sumInsured) {
		this.sumInsured = sumInsured;
	}

	public double getSumInsured() {
		return sumInsured;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsActive() {
		return isActive;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public int compareTo(DBInsurance o) {
		if(o == null)
			throw new IllegalArgumentException("DBInsurance is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}

}
