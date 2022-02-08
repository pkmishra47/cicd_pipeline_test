package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Schedule", noClassnameStored = true)
public class DBSchedule {
	@Id private ObjectId id;
	@Property private String doctorId;
	@Property private String hospitalId;
	@Property private String doctorName;
	@Property private String specialty;
	@Property private String region;
	@Property private Date date;
	@Property private double startTime;
	@Property private double endTime;
	@Property private int maxAppointmentsInOneSlot;
	@Property private int noOfAppointmentsBooked;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getDoctorId() {
		return doctorId;
	}
	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}
	public String getHospitalId() {
		return hospitalId;
	}
	public void setHospitalId(String hospitalId) {
		this.hospitalId = hospitalId;
	}
	public String getDoctorName() {
		return doctorName;
	}
	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}
	public String getSpecialty() {
		return specialty;
	}
	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getStartTime() {
		return startTime;
	}
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	public double getEndTime() {
		return endTime;
	}
	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	public int getMaxAppointmentsInOneSlot() {
		return maxAppointmentsInOneSlot;
	}
	public void setMaxAppointmentsInOneSlot(int maxAppointmentsInOneSlot) {
		this.maxAppointmentsInOneSlot = maxAppointmentsInOneSlot;
	}
	public int getNoOfAppointmentsBooked() {
		return noOfAppointmentsBooked;
	}
	public void setNoOfAppointmentsBooked(int noOfAppointmentsBooked) {
		this.noOfAppointmentsBooked = noOfAppointmentsBooked;
	}
}
