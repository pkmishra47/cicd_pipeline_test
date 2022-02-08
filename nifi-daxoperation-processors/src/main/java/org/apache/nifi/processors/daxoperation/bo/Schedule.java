package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class Schedule {
    public ObjectId id;
    public String doctorId;
    public String hospitalId;
    public String doctorName;
    public String specialty;
    public String region;
    public Date date;
    public Double startTime;
    public Double endTime;
    public long maxAppointmentsInOneSlot;
    public long noOfAppointmentsBooked;
}
