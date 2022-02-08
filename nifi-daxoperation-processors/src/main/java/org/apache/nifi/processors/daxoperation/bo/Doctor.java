package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.List;

public class Doctor {
    public ObjectId id;
    public String firstName;
    public String middleName;
    public String lastName;
    public String specialization;
    public String entity;
    public List<Education> educations;
    public List<Hospital> hospitalsAssociated;
    public List<VisitingSchedule> visitingSchedule;
    public String email;
    public String emergencyContact_1;
    public String emergencyContact_2;
    public String personalMobile_1;
    public String personalMobile_2;
}
