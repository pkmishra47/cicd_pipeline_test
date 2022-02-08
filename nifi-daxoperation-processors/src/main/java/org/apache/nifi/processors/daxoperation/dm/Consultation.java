package org.apache.nifi.processors.daxoperation.dm;

public class Consultation {
    private Consultation consultation;
    private String uhid;
    private String doctorid;
    private String appointmentid;
    private String specialityid;
    private String speciality;
    private String appointmenttype;
    private String modeofappointment;
    private String consultedtime;
    private String doctor_name;
    private String locationid;
    private String location_name;

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getDoctorid() {
        return doctorid;
    }

    public void setDoctorid(String doctorid) {
        this.doctorid = doctorid;
    }

    public String getAppointmentid() {
        return appointmentid;
    }

    public void setAppointmentid(String appointmentid) {
        this.appointmentid = appointmentid;
    }

    public String getSpecialityid() {
        return specialityid;
    }

    public void setSpecialityid(String specialityid) {
        this.specialityid = specialityid;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getAppointmenttype() {
        return appointmenttype;
    }

    public void setAppointmenttype(String appointmenttype) {
        this.appointmenttype = appointmenttype;
    }

    public String getModeofappointment() {
        return modeofappointment;
    }

    public void setModeofappointment(String modeofappointment) {
        this.modeofappointment = modeofappointment;
    }

    public String getConsultedtime() {
        return consultedtime;
    }

    public void setConsultedtime(String consultedtime) {
        this.consultedtime = consultedtime;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(String locationid) {
        this.locationid = locationid;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }
}
