package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Embedded
public class DBOccupation implements Comparable<DBOccupation>{
	@Id ObjectId id = new ObjectId();
	@Property private String companyName; 
	@Property private String occupationTitle;
	@Property private Date startDate;   
	@Property private Date endDate;   


	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getOccupationTitle() {
		return occupationTitle;
	}

	public void setOccupationTitle(String occupationTitle) {
		this.occupationTitle = occupationTitle;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	

	@Override
	public int compareTo(DBOccupation arg0) {
		if(arg0 == null)
			throw new IllegalArgumentException("DBOccupation is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}
}
	