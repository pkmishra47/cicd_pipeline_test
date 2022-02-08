package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBFamilyHistory;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class FamilyHistory {
    public ObjectId id;
    public DBFamilyHistory.InheritedDiseaseName DiseaseName;
    public List<FamilyHistoryDetail> familyHistoryDetailList;
    public Date createdAt;
}
