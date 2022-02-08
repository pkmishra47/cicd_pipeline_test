package org.apache.nifi.processors.daxoperation.dbo;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;

@Embedded
public class DBEducation implements Comparable<DBUserBasicInfo>{
	@Id ObjectId id = new ObjectId();
	@Property private String graduationLevel; // got from master data
	@Property private String graduationName;  // got from master data
	@Property private Date attendedFrom;   
	@Property private Date attendedEnd;   


	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	@Override
	public int compareTo(DBUserBasicInfo arg0) {
		if(arg0 == null)
			throw new IllegalArgumentException("DBUserBasicInfo is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}
	
	public String getGraduationLevel() {
		return graduationLevel;
	}
	
	public void setGraduationLevel(String graduationLevel) {
		this.graduationLevel = graduationLevel;
	}
	
	public String getGraduationName() {
		return graduationName;
	}
	public void setGraduationName(String graduationName) {
		this.graduationName = graduationName;
	}
	
	public Date getAttendedFrom() {
		return attendedFrom;
	}
	
	public void setAttendedFrom(Date attendedFrom) {
		this.attendedFrom = attendedFrom;
	}
	public Date getAttendedEnd() {
		return attendedEnd;
	}
	
	public void setAttendedEnd(Date attendedEnd) {
		this.attendedEnd = attendedEnd;
	}
}
