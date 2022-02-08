package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class MedicalImage {
    public ObjectId id;
    public String name;
    public Date dateOfImaging;
    public List<Attachment> fileList;
    public String referringDoctor;
    public String laboratory;
    public String additionalNotes;
    public String source;

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

    public Date getDateOfImaging() {
        return dateOfImaging;
    }

    public void setDateOfImaging(Date dateOfImaging) {
        this.dateOfImaging = dateOfImaging;
    }

    public List<Attachment> getFileList() {
        return fileList;
    }

    public void setFileList(List<Attachment> fileList) {
        this.fileList = fileList;
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
}
