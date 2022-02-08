package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "Prescription", noClassnameStored = true)
public class DBPrescription implements Comparable<DBPrescription> {

    @Id
    private ObjectId id;
    @Property
    private String prescriptionName;
    @Property
    private String hospitalName;
    @Property
    private String source;
    @Property
    private String consultId;
    @Property
    private String tag;
    @Property
    private Date dateOfPrescription;
    @Property
    private Date startDate;
    @Property
    private Date endDate;
    @Property
    private Date createdDateTime;
    @Property
    private String prescribedBy;
    @Property
    private String notes;
    @Property
    private String speciality;
    @Property
    private String appointmentDisplayId;
    @Property
    private String hospital_name;
    @Property
    private String hospitalId;
    @Property
    private String address;
    @Property
    private String city;
    @Property
    private String pincode;
    @Property
    private String patientLocation;
    @Property
    private String patientPincode;
    @Property
    private Date updatedDateTime;
    @Property
    private List<String> instructions = new ArrayList<>();
    @Property
    private List<String> diagnosis = new ArrayList<>();
    @Property
    private List<String> diagnosticPrescription = new ArrayList<>();
//	@Property private String prescriptionSource;

    @Embedded("prescriptionDetails")
    private List<DBPrescriptionDetail> newprescriptionDetails = new ArrayList<DBPrescriptionDetail>();
    @Embedded("prescriptionFiles")
    private List<DBAttachement> prescriptionFiles = new ArrayList<DBAttachement>();
    @Embedded("medicinePrescriptions")
    private List<DBMedicinePrescription> medicinePrescriptions = new ArrayList<>();

//	public void setPrescriptionSource(String prescriptionSource){
//		this.prescriptionSource=prescriptionSource;
//	}
//
//	public String getPrescriptionSource(){
//		return this.prescriptionSource;
//	}

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPrescriptionName() {
        return prescriptionName;
    }

    public void setPrescriptionName(String prescriptionName) {
        this.prescriptionName = prescriptionName;
    }

    public Date getDateOfPrescription() {
        return dateOfPrescription;
    }

    public void setDateOfPrescription(Date dateOfPrescription) {
        this.dateOfPrescription = dateOfPrescription;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPrescribedBy() {
        return prescribedBy;
    }

    public void setPrescribedBy(String prescribedBy) {
        this.prescribedBy = prescribedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<DBPrescriptionDetail> getNewprescriptionDetails() {
        return newprescriptionDetails;
    }

    public void setNewprescriptionDetails(List<DBPrescriptionDetail> newprescriptionDetails) {
        this.newprescriptionDetails = newprescriptionDetails;
    }

    public List<DBAttachement> getPrescriptionFiles() {
        return prescriptionFiles;
    }

    public void setPrescriptionFiles(List<DBAttachement> prescriptionFiles) {
        this.prescriptionFiles = prescriptionFiles;
    }

    @Override
    public int compareTo(DBPrescription o) {
        if (o == null)
            throw new IllegalArgumentException("DBPrescription is Null");

        return Comparators.stringCompare(id.toString(), o.getId().toString());
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getConsultId() {
        return consultId;
    }

    public void setConsultId(String consultId) {
        this.consultId = consultId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<DBMedicinePrescription> getMedicinePrescriptions() {
        return medicinePrescriptions;
    }

    public void setMedicinePrescriptions(List<DBMedicinePrescription> medicinePrescriptions) {
        this.medicinePrescriptions = medicinePrescriptions;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getHospital_name() {
        return hospital_name;
    }

    public void setHospital_name(String hospital_name) {
        this.hospital_name = hospital_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public List<String> getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(List<String> diagnosis) {
        this.diagnosis = diagnosis;
    }

    public List<String> getDiagnosticPrescription() {
        return diagnosticPrescription;
    }

    public void setDiagnosticPrescription(List<String> diagnosticPrescription) {
        this.diagnosticPrescription = diagnosticPrescription;
    }

    public String getAppointmentDisplayId() {
        return appointmentDisplayId;
    }

    public void setAppointmentDisplayId(String appointmentDisplayId) {
        this.appointmentDisplayId = appointmentDisplayId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getPatientLocation() {
        return patientLocation;
    }

    public void setPatientLocation(String patientLocation) {
        this.patientLocation = patientLocation;
    }

    public String getPatientPincode() {
        return patientPincode;
    }

    public void setPatientPincode(String patientPincode) {
        this.patientPincode = patientPincode;
    }

    public Date getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(Date updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }
}