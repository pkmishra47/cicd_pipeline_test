package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Version;

@Entity(value = "User", noClassnameStored = true)
public class DBUser implements Comparable<DBUser> {
    @Id
    private ObjectId id;

    public static enum UserStatus {
        ACTIVE,
        INACTIVE,
        TERMINATED,
        NOT_ACTIVATED,
        CARE_GIVER
    }

    @Property
    private String salutation;
    @Property
    private String firstName;
    @Property
    private String middleName;
    @Property
    private String lastName;
    @Property
    private UserStatus status;
    @Property
    private Date dateActivated;
    @Property
    private Date updatedAt;
    @Property
    private String defaultEntity;
    @Property
    private String userCreationRoleName;
    @Property
    private String corporateName;
    @Property
    private String mobileNumber;
    @Property
    private Date dateImported;
    @Property
    private Date healthCheckDelaySMSSent;
    @Property
    private String aadharNumber;
    @Property
    private String license;
    @Property
    private String panCard;
    @Property
    private Boolean updateIdentityObject;
    @Version
    long version;

    @Embedded("userBasicInfo")
    DBUserBasicInfo userBasicInfo;
    @Embedded("userContactInfo")
    DBUserContactInfo userContactInfo;
    @Embedded("userPreferenceInfo")
    DBUserPreferenceInfo userPreferenceInfo;
    @Property("entitys")
    List<String> entitys = new ArrayList<String>();
    @Property("roles")
    List<String> roles = new ArrayList<String>();
    @Reference("circles")
    List<DBCircle> circles = new ArrayList<DBCircle>();

    @Embedded("address")
    private List<DBAddress> addressList = new ArrayList<DBAddress>();

//	@Embedded("sugarInfo")
//	private List<DBSugarInfo> sugarInfo;


    @Reference(value = "sugarInfo")
    private List<DBSugarInfo> sugarInfo = new ArrayList<DBSugarInfo>();

    @Reference(value = "allergy", lazy = true)
    private List<DBAllergy> allergy = new ArrayList<DBAllergy>();  // This should be set / key to hold uniqueness

    @Reference(value = "medicalImage", lazy = true)
    private List<DBMedicalImage> medicalImage = new ArrayList<DBMedicalImage>();

    @Reference(value = "medicalFile", lazy = true)
    private List<DBMedicalFile> medicalFile = new ArrayList<DBMedicalFile>();


    @Reference(value = "protondetails", lazy = true)
    private DBProtonDetails protondetails;

    @Reference(value = "immunizations", lazy = true)
    List<DBImmunization> immunizations = new ArrayList<DBImmunization>();

    @Reference(value = "labtests", lazy = true)
    List<DBLabTest> labtests = new ArrayList<DBLabTest>();

    @Reference(value = "cardiacRiskScore", lazy = true)
    private
    List<DBCardiacRiskScore> cardiacRiskScores = new ArrayList<DBCardiacRiskScore>();

    @Reference(value = "medications", lazy = true)
    List<DBMedication> medications = new ArrayList<DBMedication>();

    @Reference(value = "medicationTaken", lazy = true)
    List<DBMedicationTaken> medicationTaken = new ArrayList<DBMedicationTaken>();

    @Reference(value = "sleepPattern", lazy = true)
    List<DBSleepPattern> sleepPattern = new ArrayList<DBSleepPattern>();

    @Reference(value = "dairyDetails", lazy = true)
    List<DBDairyDetails> dairyDetails = new ArrayList<DBDairyDetails>();

    @Reference(value = "problems", lazy = true)
    List<DBProblem> problems = new ArrayList<DBProblem>();

    @Reference(value = "procedures", lazy = true)
    List<DBProcedure> procedures = new ArrayList<DBProcedure>();

    @Reference(value = "prescriptions", lazy = true)
    List<DBPrescription> prescriptions = new ArrayList<DBPrescription>();

    @Reference(value = "appointments", lazy = true)
    List<DBAppointment> appointments = new ArrayList<DBAppointment>();

    @Reference(value = "restrictions", lazy = true)
    List<DBRestriction> restrictions = new ArrayList<DBRestriction>();

    @Reference(value = "doctorVisits", lazy = true)
    List<DBDoctorVisit> doctorVisits = new ArrayList<DBDoctorVisit>();

    @Reference(value = "hospitilization", lazy = true)
    List<DBHospitalization> hospitilization = new ArrayList<DBHospitalization>();

    @Reference(value = "insurance", lazy = true)
    List<DBInsurance> insurance = new ArrayList<DBInsurance>();

    @Reference(value = "medicalCondition", lazy = true)
    List<DBMedicalCondition> medicalCondition = new ArrayList<DBMedicalCondition>();

    @Reference(value = "toolsMap", lazy = true)
    Map<String, DBTool> toolsMap = new HashMap<String, DBTool>();

    @Reference(value = "hospitals", lazy = true)
    List<DBHospital> hospitals = new ArrayList<DBHospital>();

    @Reference(value = "doctors", lazy = true)
    List<DBDoctor> doctors = new ArrayList<DBDoctor>();

    @Reference(value = "reminders", lazy = true)
    private List<DBReminder> reminders = new ArrayList<DBReminder>();

    @Reference(value = "expertChats", lazy = true)
    private List<DBExpertChatAppointment> expertChats = new ArrayList<DBExpertChatAppointment>();

    @Reference(value = "hraTests", lazy = true)
    private List<DBHRATest> hraTests = new ArrayList<DBHRATest>();

    @Reference(value = "healthChecks", lazy = true)
    private List<DBHealthCheck> healthChecks = new ArrayList<DBHealthCheck>();

    @Reference(value = "bills", lazy = true)
    private List<DBBill> bills = new ArrayList<>();

    @Reference(value = "familyHistory", lazy = true)
    private List<DBFamilyHistory> familyHistorys = new ArrayList<DBFamilyHistory>();

    @Reference(value = "callInfo", lazy = true)
    private List<DBCallInfo> callInfo = new ArrayList<DBCallInfo>();

    @Reference(value = "homeHealth", lazy = true)
    private List<DBHomeHealth> homeHealth = new ArrayList<DBHomeHealth>();

    @Reference(value = "dietPlan", lazy = true)
    private DBDietPlan dietPlan = null;


    @Reference(value = "proHealth")
    private DBProHealth proHealth = null;

    @Reference(value = "consultations", lazy = true)
    private List<DBConsultation> consultations = new ArrayList<DBConsultation>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getEntitys() {
        return entitys;
    }

    public void setEntitys(List<String> entitys) {
        this.entitys = entitys;
    }

    public Date getDateImported() {
        return dateImported;
    }

    public void setDateImported(Date dateImported) {
        this.dateImported = dateImported;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<DBAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<DBAddress> addressList) {
        this.addressList = addressList;
    }

    public void addAddress(DBAddress address) {
        addressList.add(address);
    }

    public List<DBAllergy> getAllergy() {
        return allergy;
    }

    public void setAllergy(List<DBAllergy> allergy) {
        this.allergy = allergy;
    }

    public List<DBImmunization> getImmunizations() {
        return immunizations;
    }

    public void setImmunizations(List<DBImmunization> immunizations) {
        this.immunizations = immunizations;
    }

    public List<DBLabTest> getLabtests() {
        return labtests;
    }

    public void setLabtests(List<DBLabTest> labtests) {
        this.labtests = labtests;
    }

    public List<DBMedication> getMedications() {
        return medications;
    }

    public void setMedications(List<DBMedication> medications) {
        this.medications = medications;
    }

    public List<DBMedicationTaken> getMedicationTaken() {
        return medicationTaken;
    }

    public void setMedicationTakne(List<DBMedicationTaken> medicationTaken) {
        this.medicationTaken = medicationTaken;
    }

    public List<DBSleepPattern> getSleepPattern() {
        return sleepPattern;
    }

    public void setSleepPattern(List<DBSleepPattern> sleepPattern) {
        this.sleepPattern = sleepPattern;
    }

    public List<DBDairyDetails> getDairyDetails() {
        return dairyDetails;
    }

    public void setDairyDetails(List<DBDairyDetails> dairyDetails) {
        this.dairyDetails = dairyDetails;
    }

    public List<DBProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<DBProblem> problems) {
        this.problems = problems;
    }

    public List<DBProcedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<DBProcedure> procedures) {
        this.procedures = procedures;
    }

    public List<DBPrescription> getPrescriptions() {
        return prescriptions;
    }

    public List<DBConsultation> getConsultations() {
        return consultations;
    }

    public void setConsultations(List<DBConsultation> consultations) {
        this.consultations = consultations;
    }

    public void setPrescriptions(List<DBPrescription> prescriptions) {
        this.prescriptions = prescriptions;
    }

    public void addMedication(DBMedication medication) {
        this.medications.add(medication);
    }

    public List<DBAppointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<DBAppointment> appointments) {
        this.appointments = appointments;
    }

    public List<DBCircle> getCircles() {
        return circles;
    }

    public void setCircles(List<DBCircle> circleList) {
        this.circles = circleList;
    }

    public void addCircle(DBCircle dbCircle) {
        this.circles.add(dbCircle);
    }

    public void setRestrictions(List<DBRestriction> dbRestrictionList) {
        this.restrictions = dbRestrictionList;
    }

    public List<DBRestriction> getRestrictionList() {
        return this.restrictions;
    }

    public void addRestrictions(DBRestriction dbRestriction) {
        this.restrictions.add(dbRestriction);
    }

    public void setDoctorVisit(List<DBDoctorVisit> dbDoctorVisitList) {
        this.doctorVisits = dbDoctorVisitList;
    }

    public List<DBDoctorVisit> getDoctorVist() {
        return this.doctorVisits;
    }

    public void addDoctorVisit(DBDoctorVisit docVisit) {
        this.doctorVisits.add(docVisit);
    }

    public List<DBHospitalization> getHospitilization() {
        return this.hospitilization;
    }

    public void addHospitilization(DBHospitalization hospitilization) {
        this.hospitilization.add(hospitilization);
    }

    public void addPrescriptions(DBPrescription prescriptions) {
        this.prescriptions.add(prescriptions);
    }

    public void addConsultations(DBConsultation consultation) {
        this.consultations.add(consultation);
    }

    public void setHospitilization(List<DBHospitalization> dbHospitilization) {
        this.hospitilization = dbHospitilization;
    }

    public List<DBInsurance> getInsurance() {
        return this.insurance;
    }

    public void addInsurance(DBInsurance insurance) {
        this.insurance.add(insurance);
    }

    public void setInsurance(List<DBInsurance> dbInsurance) {
        this.insurance = dbInsurance;
    }

    public List<DBMedicalCondition> getMedicalCondition() {
        return this.medicalCondition;
    }

    public void addMedicalCondition(DBMedicalCondition medicalCondition) {
        this.medicalCondition.add(medicalCondition);
    }

    public void setMedicalCondition(List<DBMedicalCondition> dbMedicalCondition) {
        this.medicalCondition = dbMedicalCondition;
    }

    public DBTool getTool(String toolName) {
        if (!toolsMap.containsKey(toolName))
            return null;
        return toolsMap.get(toolName);
    }

    public void deleteTool(String toolName) {
        if (!toolsMap.containsKey(toolName))
            return;
        toolsMap.remove(toolName);
    }

    // Have to make sure this tool is available in the tool master, but there are also
    // scenario where tools not in list are also added.
    public void addTool(String toolName, DBTool dbTool) {
        this.toolsMap.put(toolName, dbTool);
    }

    public void addToolData(String toolName, DBToolData toolData) {
        if (!toolsMap.containsKey(toolName))
            return;
        toolsMap.get(toolName).addToolData(toolData);
    }

    public void setTools(Map<String, DBTool> dbToolMap) {
        this.toolsMap = dbToolMap;
    }

    public List<String> getToolList() {
        return new ArrayList<String>(toolsMap.keySet());
    }

    public List<DBHospital> getHospitals() {
        return this.hospitals;
    }

    public void setHospitals(List<DBHospital> hospitalList) {
        this.hospitals = hospitalList;
    }

    public void addHospital(DBHospital dbHospital) {
        this.hospitals.add(dbHospital);
    }

    public List<DBDoctor> getDoctors() {
        return this.doctors;
    }

    public void setDoctors(List<DBDoctor> doctorList) {
        this.doctors = doctorList;
    }

    public void addDoctor(DBDoctor doctor) {
        this.doctors.add(doctor);
    }

    public DBUserBasicInfo getUserBasicInfo() {
        return userBasicInfo;
    }

    public void setUserBasicInfo(DBUserBasicInfo userBasicInfo) {
        this.userBasicInfo = userBasicInfo;
    }

    public DBUserContactInfo getUserContactInfo() {
        return userContactInfo;
    }

    public void setUserContactInfo(DBUserContactInfo userContactInfo) {
        this.userContactInfo = userContactInfo;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getDateActivated() {
        return dateActivated;
    }

    public void setDateActivated(Date dateActivated) {
        this.dateActivated = dateActivated;
    }

    public Date getHealthCheckDelaySMSSent() {
        return healthCheckDelaySMSSent;
    }

    public void setHealthCheckDelaySMSSent(Date healthCheckDelaySMSSent) {
        this.healthCheckDelaySMSSent = healthCheckDelaySMSSent;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public List<DBReminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<DBReminder> reminders) {
        this.reminders = reminders;
    }

    public List<DBExpertChatAppointment> getExpertChats() {
        return expertChats;
    }

    public void setExpertChats(List<DBExpertChatAppointment> expertChats) {
        this.expertChats = expertChats;
    }

    public List<DBHRATest> getHraTests() {
        return hraTests;
    }

    public void setHraTests(List<DBHRATest> hraTests) {
        this.hraTests = hraTests;
    }

    public DBUserPreferenceInfo getUserPreferenceInfo() {
        return userPreferenceInfo;
    }

    public void setUserPreferenceInfo(DBUserPreferenceInfo userPreferenceInfo) {
        this.userPreferenceInfo = userPreferenceInfo;
    }

    public List<DBSugarInfo> getSugarInfo() {
        return sugarInfo;
    }

    public void setSugarInfo(List<DBSugarInfo> sugarInfo) {
        this.sugarInfo = sugarInfo;
    }

    public List<DBHealthCheck> getHealthChecks() {
        return healthChecks;
    }

    public void setHealthChecks(List<DBHealthCheck> healthChecks) {
        this.healthChecks = healthChecks;
    }

    public void addHealthCheck(DBHealthCheck healthCheck) {
        this.healthChecks.add(healthCheck);
    }

    public List<DBBill> getBills() {
        return bills;
    }

    public void setBills(List<DBBill> bills) {
        this.bills = bills;
    }

    public void addBill(DBBill bill) {
        this.bills.add(bill);
    }


    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(DBUser dbUsr) {
        if (dbUsr == null)
            throw new NullPointerException("DBUser is Null for compareTo function");

        return Comparators.stringCompare(getId().toString(), dbUsr.getId().toString());
    }

    public List<DBMedicalImage> getMedicalImage() {
        return medicalImage;
    }

    public void setMedicalImage(List<DBMedicalImage> medicalImage) {
        this.medicalImage = medicalImage;
    }

    public List<DBMedicalFile> getMedicalFile() {
        return medicalFile;
    }

    public void setMedicalFile(List<DBMedicalFile> medicalFile) {
        this.medicalFile = medicalFile;
    }


    public String getDefaultEntity() {
        return defaultEntity;
    }

    public void setDefaultEntity(String defaultEntity) {
        this.defaultEntity = defaultEntity;
    }

    public String getUserCreationRoleName() {
        return userCreationRoleName;
    }

    public void setUserCreationRoleName(String userCreationRoleName) {
        this.userCreationRoleName = userCreationRoleName;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public List<DBFamilyHistory> getFamilyHistorys() {
        return familyHistorys;
    }

    public void setFamilyHistorys(List<DBFamilyHistory> familyHistorys) {
        this.familyHistorys = familyHistorys;
    }

    public void addFamilyHistory(DBFamilyHistory familyHistory) {
        this.familyHistorys.add(familyHistory);
    }

    public List<DBCallInfo> getCallInfo() {
        return callInfo;
    }

    public void setCallInfo(List<DBCallInfo> callInfo) {
        this.callInfo = callInfo;
    }

    public List<DBHomeHealth> getHomeHealth() {
        return homeHealth;
    }

    public void setHomeHealth(List<DBHomeHealth> homeHealth) {
        this.homeHealth = homeHealth;
    }

    public DBDietPlan getDietPlan() {
        return dietPlan;
    }

    public void setDietPlan(DBDietPlan dietPlan) {
        this.dietPlan = dietPlan;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getPanCard() {
        return panCard;
    }

    public void setPanCard(String panCard) {
        this.panCard = panCard;
    }

    public Boolean getUpdateIdentityObject() {
        return updateIdentityObject;
    }

    public void setUpdateIdentityObject(Boolean updateIdentityObject) {
        this.updateIdentityObject = updateIdentityObject;
    }

    public List<DBCardiacRiskScore> getCardiacRiskScores() {
        return cardiacRiskScores;
    }

    public void setCardiacRiskScores(List<DBCardiacRiskScore> cardiacRiskScores) {
        this.cardiacRiskScores = cardiacRiskScores;
    }

    public DBProtonDetails getProtondetails() {
        return protondetails;
    }

    public void setProtondetails(DBProtonDetails protondetails) {
        this.protondetails = protondetails;
    }

    public DBProHealth getProHealth() {
        return proHealth;
    }

    public void setProHealth(DBProHealth proHealth) {
        this.proHealth = proHealth;
    }

}