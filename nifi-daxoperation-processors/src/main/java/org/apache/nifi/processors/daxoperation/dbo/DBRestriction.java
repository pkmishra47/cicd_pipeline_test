package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "Restriction", noClassnameStored = true)
public class DBRestriction  implements Comparable<DBRestriction>{
	@Id private ObjectId id;
	@Property private String restrictionName; // This is referenced from DBDoctor
	@Property private RestrictionType nature;
	@Property private String suggestedByDoctor; // This should be Doctor Id
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private String notes;
	@Property private String source;	

	@Property private boolean havingNow;
		
	public static  enum RestrictionType {
		Physical,Dietary,OTHER
	}

	public void setRestrictionName(String restrictionName) {
		this.restrictionName = restrictionName;
	}

	public String getRestrictionName() {
		return restrictionName;
	}

	public void setNature(RestrictionType nature) {
		this.nature = nature;
	}

	public RestrictionType getNature() {
		return nature;
	}

	public void setSuggestedByDoctor(String suggestedByDoctor) {
		this.suggestedByDoctor = suggestedByDoctor;
	}

	public String getSuggestedByDoctor() {
		return suggestedByDoctor;
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

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}

	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId objectId) {
		this.id = objectId;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public int compareTo(DBRestriction o) {
		if(o == null)
			throw new IllegalArgumentException("DBRestriction is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}

	public boolean isHavingNow() {
		return havingNow;
	}

	public void setHavingNow(boolean havingNow) {
		this.havingNow = havingNow;
	}
	
}