package org.apache.nifi.processors.daxoperation.dbo;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBAllergyDetail {
	@Id private ObjectId id = new ObjectId();
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private Severity severity; // This should be chosen using UI 
	@Property private String reactionToAllergy;
	@Property private String notes;
	@Property private String doctorTreated; // This doctor name is got from the Doctor collection
	@Embedded("attachmentList")
	List<DBAttachement> attachementList = new ArrayList<DBAttachement>();

	
	public static enum Severity {
		MILD, SEVERE, LIFE_THREATNING, UNKNOWN
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
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

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setReactionToAllergy(String reactionToAllergy) {
		this.reactionToAllergy = reactionToAllergy;
	}

	public String getReactionToAllergy() {
		return reactionToAllergy;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}
	public void setDoctorTreated(String doctorTreated) {
		this.doctorTreated = doctorTreated;
	}
	public String getDoctorTreated() {
		return doctorTreated;
	}
	public List<DBAttachement> getAttachmentList() {
		return attachementList;
	}
	
	public void addAttachment(DBAttachement attachment) {
		this.attachementList.add(attachment);
	}
	
}
	