package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "DairyDetails", noClassnameStored = true)
public class DBDairyDetails implements Comparable<DBDairyDetails> {
	@Id
	private ObjectId id;
	@Property
	private String Notes;
	@Property
	private String type;
	@Property
	private Date Date;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setNotes(String Notes) {
		this.Notes = Notes;
	}

	public String getNotes() {
		return Notes;
	}

	public void setDate(Date Date) {
		this.Date = Date;
	}

	public Date getDate() {
		return Date;
	}

	public void settype(String type) {
		this.type = type;
	}

	public String gettype() {
		return type;
	}

	@Override
	public int compareTo(DBDairyDetails o) {
		// TODO Auto-generated method stub
		return 0;
	}

}