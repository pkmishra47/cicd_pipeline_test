package org.apache.nifi.processors.daxoperation.dbo;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "Immunization", noClassnameStored = true)
public class DBImmunization  implements Comparable<DBImmunization>{
	@Id private ObjectId id;
	@Property private String immunizationName;           // This is referenced from ImmunizationMaster
	@Property private Date dateOfFirstShot;
	@Property private String orderedByDoctor;
	@Embedded("immunizationDetail")
	Map<Date,String> immunizationDetails = new HashMap<Date, String>();
	
	//@Embedded("immuizationDetails")
	//private List<ImmunizationDetail> immuizationDetails; // Shots will be taken on multiple dates
	
	@Property private String notes;
	@Property private String source;	
	
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setImmunizationName(String immunizationName) {
		this.immunizationName = immunizationName;
	}
	public String getImmunizationName() {
		return immunizationName;
	}
	public void setDateOfFirstShot(Date dateOfImmunization) {
		this.dateOfFirstShot = dateOfImmunization;
	}
	public Date getDateOfFirstShot() {
		return dateOfFirstShot;
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
//	public void setImmuizationDetails(List<ImmunizationDetail> immuizationDetails) {
//		this.immuizationDetails = immuizationDetails;
//	}
//	public List<ImmunizationDetail> getImmuizationDetails() {
//		return immuizationDetails;
//	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getNotes() {
		return notes;
	}
	public void setImmunizationDetails(Map<Date,String> immunizationDetails) {
		this.immunizationDetails = immunizationDetails;
	}
	public Map<Date,String> getImmunizationDetails() {
		return immunizationDetails;
	}
	
	// We need a timestamp 
	public void addImmunizationDetail(Date date, String notes) {
		this.immunizationDetails.put(date, notes);
	}
	public String getOrderedByDoctor() {
		return orderedByDoctor;
	}
	public void setOrderedByDoctor(String orderedByDoctor) {
		this.orderedByDoctor = orderedByDoctor;
	}
	@Override
	public int compareTo(DBImmunization arg0) {
		if(arg0== null)
			throw new IllegalArgumentException("DBImmunization is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}
}