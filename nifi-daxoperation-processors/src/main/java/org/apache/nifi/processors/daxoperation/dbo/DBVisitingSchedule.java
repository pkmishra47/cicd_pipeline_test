package org.apache.nifi.processors.daxoperation.dbo;

import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

public class DBVisitingSchedule {
	@Reference("hospital")
	private DBHospital hospital;
	@Property private DayOfWeek dayOfWeek;
	@Property private double startTime;
	@Property private double endTime;
	@Property private int slotDuration;
	@Property private int maxAppointmentsInOneSlot;
	
	public static enum DayOfWeek {
		Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
	}

	public DBHospital getHospital() {
		return hospital;
	}

	public void setHospital(DBHospital hospital) {
		this.hospital = hospital;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
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

	public int getSlotDuration() {
		return slotDuration;
	}

	public void setSlotDuration(int slotDuration) {
		this.slotDuration = slotDuration;
	}

	public int getMaxAppointmentsInOneSlot() {
		return maxAppointmentsInOneSlot;
	}

	public void setMaxAppointmentsInOneSlot(int maxAppointmentsInOneSlot) {
		this.maxAppointmentsInOneSlot = maxAppointmentsInOneSlot;
	}
}
