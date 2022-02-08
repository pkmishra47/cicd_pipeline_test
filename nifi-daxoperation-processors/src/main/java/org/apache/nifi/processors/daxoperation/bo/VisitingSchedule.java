package org.apache.nifi.processors.daxoperation.bo;

import org.apache.nifi.processors.daxoperation.dbo.DBVisitingSchedule;
import org.bson.types.ObjectId;

public class VisitingSchedule {
    public ObjectId id;
    public Hospital hospital;
    public DBVisitingSchedule.DayOfWeek dayOfWeek;
    public Double startTime;
    public Double endTime;
    public long slotDuration;
    public long maxAppointmentsInOneSlot;
}
