package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBMedicationTaken;
import org.bson.types.ObjectId;

import java.util.Date;

public class MedicationTaken {
    public ObjectId id;
    public String medicineName;
    public String strength;
    public Date dateTaken;
    public DBMedicationTaken.MedicineConsumed morning;
    public DBMedicationTaken.MedicineConsumed afternoon;
    public DBMedicationTaken.MedicineConsumed evening;
}
