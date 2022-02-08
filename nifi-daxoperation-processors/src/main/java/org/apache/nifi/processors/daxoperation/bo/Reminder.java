package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class Reminder {
    public ObjectId id;
    public String reminderName;
    public String reminderDesc;
    public Date duetime;
    public long reminderStatus; // 0,1,2,3,4,5 [ fix them using enum ]
}
