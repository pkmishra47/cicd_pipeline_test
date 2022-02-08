package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class UploadFile {
    public ObjectId id;
    public String source;
    public Date uploadedDate;
    public long totalChunks;
    public long fileSize;
    public long uploadedSize;
    public String fileType;
    public String fileName;
    public String uhid;
    public String filePath;
    public Boolean inProgress;
    public String fileCategory;
    public String uploader;
}
