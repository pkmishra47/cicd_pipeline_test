package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

public class DBMedicalImage implements Comparable<DBMedicalImage>{
	
	@Id private ObjectId id;
	@Property private String name;
	@Property private String referringDoctor;
	@Property private String laboratory ;
	@Property private String additionalNotes ;
	@Property private String source ;
	@Property private Date dateOfImaging ;
	@Embedded("fileList")
	private
	List<DBAttachement> fileList = new ArrayList<DBAttachement>();
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getReferringDoctor() {
		return referringDoctor;
	}
	public void setReferringDoctor(String referringDoctor) {
		this.referringDoctor = referringDoctor;
	}
	public String getLaboratory() {
		return laboratory;
	}
	public void setLaboratory(String laboratory) {
		this.laboratory = laboratory;
	}
	public String getAdditionalNotes() {
		return additionalNotes;
	}
	public void setAdditionalNotes(String additionalNotes) {
		this.additionalNotes = additionalNotes;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getDateOfImaging() {
		return dateOfImaging;
	}
	public void setDateOfImaging(Date dateOfImaging) {
		this.dateOfImaging = dateOfImaging;
	}
	public List<DBAttachement> getFileList() {
		return fileList;
	}
	public void setFileList(List<DBAttachement> attachList) {
		this.fileList = attachList;
	}
	@Override
	public int compareTo(DBMedicalImage o) {
		
		if(o == null)
			throw new IllegalArgumentException("DBMedicalImage is Null");

		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}
	
}
