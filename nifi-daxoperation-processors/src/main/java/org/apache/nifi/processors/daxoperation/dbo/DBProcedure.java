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

@Entity(value = "Procedure", noClassnameStored = true)

/**
 * Keep a history of any in-patient or out-patient procedures and surgeries.
 */
public class DBProcedure implements Comparable<DBProcedure>{
	@Id private ObjectId id;
	@Property private String procedureName; // This is referenced from ProcedureMaster
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private String doctorTreated;
	@Property private String notes;
	@Property private String siteKey;
	@Property private String uhid;
	@Property private String procedureId;
	@Property private String source;
	

	@Embedded("procedureFiles")
	List<DBAttachement> procedureFiles = new ArrayList<DBAttachement>();
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public DBProcedure() {	
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}
	
	@Override
	public int compareTo(DBProcedure o) {
		if(o == null)
			throw new IllegalArgumentException("DBProcedure is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
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

	public void setDoctorTreated(String doctorTreated) {
		this.doctorTreated = doctorTreated;
	}

	public String getDoctorTreated() {
		return doctorTreated;
	}
	
	public void setProcedureFiles(List<DBAttachement> attachList) {
		this.procedureFiles = attachList;
	}
	
	public List<DBAttachement> getProcedureFiles() {
		return procedureFiles;
	}
	
	public void addProcedureFile(DBAttachement attachment) {
		this.procedureFiles.add(attachment);
	}

	public String getSiteKey() {
		return siteKey;
	}

	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}

	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}

	public String getProcedureId() {
		return procedureId;
	}

	public void setProcedureId(String procedureId) {
		this.procedureId = procedureId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}