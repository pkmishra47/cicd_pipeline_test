package org.apache.nifi.processors.daxoperation.dbo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "Allergy", noClassnameStored = true)
public class DBAllergy implements Comparable<DBAllergy>{
	
	@Id private ObjectId id;
	@Property private String allergyName; // This is referenced from AllergType
	@Property private Boolean havingNow;
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private Severity severity; // This should be chosen using UI 
	@Property private String reactionToAllergy;
	@Property private String notes;
	@Property private String source;	

	@Property private String doctorTreated; // This doctor name is got from the Doctor collection
	@Embedded("attachmentList")
	List<DBAttachement> attachementList = new ArrayList<DBAttachement>();

	public static enum Severity {
		MILD, SEVERE, LIFE_THREATENING,NOT_KNOWN, UNKNOWN
	}

	// For future
	//@Embedded("allergyDetails")
	//List<DBAllergyDetail> allergyDetails = new ArrayList<DBAllergyDetail>(); // Reason is we could have the allergy for life long and add data at various time
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getAllergyName() {
		return allergyName;
	}
	
	public void setAllergyName(String name) {
		this.allergyName = name;
	}
	
	public void setHavingNow(Boolean havingNow){
		this.havingNow = havingNow;
	}
	
	public Boolean getHavingNow() {
		return this.havingNow;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getNotes() {
		return this.notes;
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

	public void setDoctorTreated(String doctorTreated) {
		this.doctorTreated = doctorTreated;
	}

	public String getDoctorTreated() {
		return doctorTreated;
	}
	
	public List<DBAttachement> getAttachmentList() {
		return attachementList;
	}
	
	public void setAttachementList(List<DBAttachement> attachmentList) {
		this.attachementList = attachmentList;
	}
	
	public void addAttachment(DBAttachement attachment) {
		this.attachementList.add(attachment);
	}	
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public int compareTo(DBAllergy o) {
		// A null check here would be expensive, and mostly we would not have null
		// because its collection that are working internally.
		if(o == null)
			throw new IllegalArgumentException("DBAllergy is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}
}
