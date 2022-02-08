package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * Some rework is needed on this class for Medication detail, This and
 * prescription are overlapping with each other
 * 
 * @author Manigandan
 *
 */
@Entity(value = "Medication", noClassnameStored = true)
public class DBMedication implements Comparable<DBMedication> {
	@Id
	private ObjectId id;
	@Property private String medicineName; // This is referenced from MedicineMaster

	@Property private Date startDate;
	@Property private Date endDate;
    @Property private String notes;
	@Property private String strength; // This field has to be re-factored..

	@Property private boolean morning;
	@Property private boolean noon;
	@Property
	private boolean evening;
	@Property
	private boolean stillActive;

	public boolean isMorning() {
		return morning;

	}

	public void setMorning(boolean morning) {
		this.morning = morning;

	}

	public boolean isNoon() {
		return noon;

	}

	public void setNoon(boolean noon) {
		this.noon = noon;

	}

	public boolean isEvening() {
		return evening;

	}

	public void setEvening(boolean evening) {
		this.evening = evening;

	}

	public boolean isStillActive() {
		return stillActive;

	}

	public void setStillActive(boolean stillActive) {
		this.stillActive = stillActive;

	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}

	public String getMedicineName() {
		return medicineName;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setStrength(String strength) {
		this.strength = strength;
	}

	public String getStrength() {
		return strength;
	}

	@Override
	public int compareTo(DBMedication o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

}