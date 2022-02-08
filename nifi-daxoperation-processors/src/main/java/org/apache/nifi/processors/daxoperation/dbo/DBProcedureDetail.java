package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBProcedureDetail {
	@Id private ObjectId id = new ObjectId();	
	@Property private Date startDate;
	@Property private Date endDate;
	@Property private String notes;
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	public ObjectId getId() {
		return id;
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

}
