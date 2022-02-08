package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "MedicationTaken", noClassnameStored = true)
public class DBMedicationTaken implements Comparable<DBMedicationTaken> {
	@Id
	private ObjectId id;
	@Property private String medicineName; // This is referenced from MedicineMaster
	@Property private Date dateTaken;
	@Property private String strength;

	@Property private MedicineConsumed morning;
	@Property private MedicineConsumed afternoon;
	@Property private MedicineConsumed evening;

	public static enum MedicineConsumed {
		NotNeeded, Unknown, Taken, NotTaken
	}

	public MedicineConsumed getMorning() {
		return morning;
	}

	public void setMorning(MedicineConsumed morning) {
		this.morning = morning;
	}

	public MedicineConsumed getAfterNoon() {
		return afternoon;
	}

	public void setAfterNoon(MedicineConsumed afternoon) {
		this.afternoon = afternoon;
	}

	public MedicineConsumed getEvening() {
		return evening;
	}

	public void setEvening(MedicineConsumed evening) {
		this.evening = evening;
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

	public void setDateTaken(Date dateTaken) {
		this.dateTaken = dateTaken;
	}

	public Date getDateTaken() {
		return dateTaken;
	}

	public void setStrength(String strength) {
		this.strength = strength;
	}

	public String getStrength() {
		return strength;
	}

	@Override
	public int compareTo(DBMedicationTaken o) {
		// TODO Auto-generated method stub
		return 0;
	}
}