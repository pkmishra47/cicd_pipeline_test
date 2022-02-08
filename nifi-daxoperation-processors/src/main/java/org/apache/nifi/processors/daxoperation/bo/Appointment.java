package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class Appointment {
    public ObjectId id;
    public String userId;
    public String userName;
    public String userContactNo;
    public String scheduleId;
    public String doctorName;
    public String specialty;
    public Date date;
    public Double startTime;
    public Double endTime;
    public String hospitalName;
    public String region;
    public Date bookedDate;
    public String reminderId;
}
