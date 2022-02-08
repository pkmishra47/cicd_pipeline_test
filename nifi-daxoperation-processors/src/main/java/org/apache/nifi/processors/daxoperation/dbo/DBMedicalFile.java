package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "MedicalFile", noClassnameStored = true)
public class DBMedicalFile implements Comparable<DBMedicalFile>{
	@Id private ObjectId id;
	@Property private String source;
	@Property private Date uploadedDate;
	@Property private Date readDate;
	@Property private boolean inProgress;
	@Property private long fileSize;
	@Property private long uploadedSize;
	@Embedded("file") private DBAttachement file = new DBAttachement();
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getUploadedDate() {
		return uploadedDate;
	}
	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}
	public Date getReadDate() {
		return readDate;
	}
	public void setReadDate(Date readDate) {
		this.readDate = readDate;
	}
	public boolean isInProgress() {
		return inProgress;
	}
	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public long getUploadedSize() {
		return uploadedSize;
	}
	public void setUploadedSize(long uploadedSize) {
		this.uploadedSize = uploadedSize;
	}
	public DBAttachement getFile() {
		return file;
	}
	public void setFile(DBAttachement file) {
		this.file = file;
	}
	
	@Override
	public int compareTo(DBMedicalFile dbMedicalFile) {
		if(dbMedicalFile == null)
			throw new IllegalArgumentException("DBMedicalFile is Null");

		return Comparators.stringCompare(id.toString(), dbMedicalFile.getId().toString());
	}
}
