package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBAllergyDetail;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class AllergyDetail {
    public ObjectId id;
    public Date startDate;
    public Date endDate;
    public DBAllergyDetail.Severity severity;
    public String doctorTreated;
    public String reactionToAllergy;
    public String notes;
    public List<Attachment> attachmentList;
}
