package org.apache.nifi.processors.daxoperation.dbo;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import java.util.ArrayList;
import java.util.List;

@Embedded
public class DBMedicinePrescription {

    @Property
    private String id;
    @Property
    private String medicineConsumptionDuration;
    @Property
    private String medicineConsumptionDurationInDays;
    @Property
    private String medicineConsumptionDurationUnit;
    @Property
    private String medicineDosage;
    @Property
    private String medicineFormTypes;
    @Property
    private String medicineFrequency;
    @Property
    private String medicineInstructions;
    @Property
    private String medicineName;
    @Property
    private String medicineUnit;
    @Property
    private String routeOfAdministration;
    @Property
    private String medicineCustomDosage;
    @Property
    private List<String> medicineTimings = new ArrayList<>();
    @Property
    private List<String> medicineToBeTaken = new ArrayList<>();
    @Property
    private String price;
    @Property
    private String pack;
    @Property
    private String qty;
    @Property
    private String mou;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMedicineConsumptionDuration() {
        return medicineConsumptionDuration;
    }

    public void setMedicineConsumptionDuration(String medicineConsumptionDuration) {
        this.medicineConsumptionDuration = medicineConsumptionDuration;
    }

    public String getMedicineConsumptionDurationInDays() {
        return medicineConsumptionDurationInDays;
    }

    public void setMedicineConsumptionDurationInDays(String medicineConsumptionDurationInDays) {
        this.medicineConsumptionDurationInDays = medicineConsumptionDurationInDays;
    }

    public String getMedicineConsumptionDurationUnit() {
        return medicineConsumptionDurationUnit;
    }

    public void setMedicineConsumptionDurationUnit(String medicineConsumptionDurationUnit) {
        this.medicineConsumptionDurationUnit = medicineConsumptionDurationUnit;
    }

    public String getMedicineDosage() {
        return medicineDosage;
    }

    public void setMedicineDosage(String medicineDosage) {
        this.medicineDosage = medicineDosage;
    }

    public String getMedicineFormTypes() {
        return medicineFormTypes;
    }

    public void setMedicineFormTypes(String medicineFormTypes) {
        this.medicineFormTypes = medicineFormTypes;
    }

    public String getMedicineFrequency() {
        return medicineFrequency;
    }

    public void setMedicineFrequency(String medicineFrequency) {
        this.medicineFrequency = medicineFrequency;
    }

    public String getMedicineInstructions() {
        return medicineInstructions;
    }

    public void setMedicineInstructions(String medicineInstructions) {
        this.medicineInstructions = medicineInstructions;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineUnit() {
        return medicineUnit;
    }

    public void setMedicineUnit(String medicineUnit) {
        this.medicineUnit = medicineUnit;
    }

    public String getRouteOfAdministration() {
        return routeOfAdministration;
    }

    public void setRouteOfAdministration(String routeOfAdministration) {
        this.routeOfAdministration = routeOfAdministration;
    }

    public String getMedicineCustomDosage() {
        return medicineCustomDosage;
    }

    public void setMedicineCustomDosage(String medicineCustomDosage) {
        this.medicineCustomDosage = medicineCustomDosage;
    }

    public List<String> getMedicineTimings() {
        return medicineTimings;
    }

    public void setMedicineTimings(List<String> medicineTimings) {
        this.medicineTimings = medicineTimings;
    }

    public List<String> getMedicineToBeTaken() {
        return medicineToBeTaken;
    }

    public void setMedicineToBeTaken(List<String> medicineToBeTaken) {
        this.medicineToBeTaken = medicineToBeTaken;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getMou() {
        return mou;
    }

    public void setMou(String mou) {
        this.mou = mou;
    }
}
