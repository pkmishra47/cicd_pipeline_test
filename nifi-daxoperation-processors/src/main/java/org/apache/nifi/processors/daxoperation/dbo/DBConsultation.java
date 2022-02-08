package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "Consultation", noClassnameStored = true)
public class DBConsultation {
	@Id private ObjectId id;
	@Property private String appointmentid;
	@Property private String specialityid;
	@Property private String speciality;
	@Property private String doctorid;
	@Property private String doctorid_247;
	@Property private String uhid;
	@Property private Date consultedtime;
	@Property private String appointmenttype;
	@Property private String modeofappointment;
	@Property private String doctor_name;
	@Property private String locationid;
	@Property private String location_name;

	@Property private String documentType;
	@Property private String mobileNumber;
	@Property private String department;
	@Property private String encounterId;

	@Embedded("consultationFiles")
	private List<DBAttachement> attachements;

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
	public String getDoctorid() {
		return doctorid;
	}
	public void setDoctorid(String doctorid) {
		this.doctorid = doctorid;
	}
	public String getUhid() {
		return uhid;
	}
	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	public Date getConsultedtime() {
		return consultedtime;
	}
	public void setConsultedtime(Date consultedtime) {
		this.consultedtime = consultedtime;
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
	public String getDoctorid_247() {
		return doctorid_247;
	}
	public void setDoctorid_247(String doctorid_247) {
		this.doctorid_247 = doctorid_247;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEncounterId() {
		return encounterId;
	}

	public void setEncounterId(String encounterId) {
		this.encounterId = encounterId;
	}

	public List<DBAttachement> getAttachement() {
		return attachements;
	}

	public void setAttachement(List<DBAttachement> attachements) {
		this.attachements = attachements;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
}