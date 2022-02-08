package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "UploadFile", noClassnameStored = true)
public class DBUploadFile implements Comparable<DBUploadFile>{
	@Id private ObjectId id;
	@Property private String source;
	@Property private Date uploadedDate;
	@Property private long totalChunks;
	@Property private String fileType;
	@Property private String fileName;
	@Property private String uhid;
	@Property private String fileCategory;
	@Property private String uploader;
	@Property private long fileSize;
	@Property private long uploadedSize;
	@Property private String filePath;
	@Property private boolean inProgress;
	@Property private int chunkNumber;
	@Property private boolean isHealthRecordExisit;
	@Property private ObjectId healthRecordId;
	
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
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public long getTotalChunks() {
		return totalChunks;
	}
	public void setTotalChunks(long totalChunks) {
		this.totalChunks = totalChunks;
	}
	
	
	@Override
	public int compareTo(DBUploadFile dbMedicalFile) {
		if(dbMedicalFile == null)
			throw new IllegalArgumentException("DBMedicalFile is Null");

		return Comparators.stringCompare(id.toString(), dbMedicalFile.getId().toString());
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUhid() {
		return uhid;
	}
	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public boolean isInProgress() {
		return inProgress;
	}
	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}
	public int getChunkNumber() {
		return chunkNumber;
	}
	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}
	public String getFileCategory() {
		return fileCategory;
	}
	public void setFileCategory(String fileCategory) {
		this.fileCategory = fileCategory;
	}
	public String getUploader() {
		return uploader;
	}
	public void setUploader(String uploader) {
		this.uploader = uploader;
	}
	public boolean isHealthRecordExisit() {
		return isHealthRecordExisit;
	}
	public void setHealthRecordExisit(boolean isHealthRecordExisit) {
		this.isHealthRecordExisit = isHealthRecordExisit;
	}
	public ObjectId getHealthRecordId() {
		return healthRecordId;
	}
	public void setHealthRecordId(ObjectId healthRecordId) {
		this.healthRecordId = healthRecordId;
	}
	
	
}
