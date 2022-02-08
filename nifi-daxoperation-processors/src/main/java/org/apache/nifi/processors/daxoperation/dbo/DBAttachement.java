package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBAttachement {
    @Id
    private ObjectId id = new ObjectId();
    @Property
    private String fileName;
    @Property
    private Date dateCreated;
    @Property
    private String mimeType; // can change to enum latter
    @Property
    private Object fileAttached;
    @Property
    private Date reqRaisedDate;
    @Property
    private Date sysDateTime;
    @Property
    private String ocrData;
    @Property
    private String azurePath;

    public static enum MimeType {
        image, text, audio, UNKNOWN
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setFileAttached(Object fileAttached) {
        this.fileAttached = fileAttached;
    }

    public Object getFileAttached() {
        return fileAttached;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getReqRaisedDate() {
        return reqRaisedDate;
    }

    public void setReqRaisedDate(Date reqRaisedDate) {
        this.reqRaisedDate = reqRaisedDate;
    }

    public Date getSysDateTime() {
        return sysDateTime;
    }

    public void setSysDateTime(Date sysDateTime) {
        this.sysDateTime = sysDateTime;
    }

    public String getOcrData() {
        return ocrData;
    }

    public void setOcrData(String ocrData) {
        this.ocrData = ocrData;
    }

    public String getAzurePath() {
        return azurePath;
    }

    public void setAzurePath(String azurePath) {
        this.azurePath = azurePath;
    }
}