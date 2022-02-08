package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Entity(value = "ProHealth", noClassnameStored = true)
public class DBProHealth implements Comparable<DBProHealth> {
    @Id
    private ObjectId id;
    @Reference
    private DBUser dbUser;

    @Property
    private Date testDate;
    @Property
    private String ahcno;
    @Property
    private String guestLocation;
    @Property
    private String proHealthLocation;
    @Property
    private String contactPreference;
    @Property
    private String ahcphysician;
    @Property
    private String doctorsSpeciality;
    @Property
    private String height;
    @Property
    private String weight;
    @Property
    private String impression;
    @Property
    private String dietChanges;
    @Property
    private String activityChanges;
    @Property
    private String otherLifeStyleChanges;
    @Property
    private String followupPreview;
    @Property
    private String allergy;
    @Property
    private String investigationsDetail;
    @Property
    private String consultationDetail;
    @Property
    private String systolicBP;
    @Property
    private String packageId;
    @Property
    private String packageName;
    @Property
    private String diastolicBP;
    @Property
    private String bmi;
    @Property
    private String hypertension;
    @Property
    private String dysplipideamia;
    @Property
    private String bmi_category;
    @Property
    private String metabolic_syndrome;
    @Property
    private String diabetes;
    @Property
    private String cardiac_risk;
    @Property
    private String status;
    @Property
    private String vip;
    @Property
    private Date billDate;
    @Property
    private String overAllSeverity;
    @Property
    private String callTag;
    @Property
    private String packageCategory;

    private Date updatedAt;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public DBUser getDbUser() {
        return dbUser;
    }

    public void setDbUser(DBUser dbUser) {
        this.dbUser = dbUser;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public String getAhcno() {
        return ahcno;
    }

    public void setAhcno(String ahcno) {
        this.ahcno = ahcno;
    }

    public String getGuestLocation() {
        return guestLocation;
    }

    public void setGuestLocation(String guestLocation) {
        this.guestLocation = guestLocation;
    }

    public String getProHealthLocation() {
        return proHealthLocation;
    }

    public void setProHealthLocation(String proHealthLocation) {
        this.proHealthLocation = proHealthLocation;
    }

    public String getContactPreference() {
        return contactPreference;
    }

    public void setContactPreference(String contactPreference) {
        this.contactPreference = contactPreference;
    }

    public String getAhcphysician() {
        return ahcphysician;
    }

    public void setAhcphysician(String ahcphysician) {
        this.ahcphysician = ahcphysician;
    }

    public String getDoctorsSpeciality() {
        return doctorsSpeciality;
    }

    public void setDoctorsSpeciality(String doctorsSpeciality) {
        this.doctorsSpeciality = doctorsSpeciality;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

    public String getDietChanges() {
        return dietChanges;
    }

    public void setDietChanges(String dietChanges) {
        this.dietChanges = dietChanges;
    }

    public String getActivityChanges() {
        return activityChanges;
    }

    public void setActivityChanges(String activityChanges) {
        this.activityChanges = activityChanges;
    }

    public String getOtherLifeStyleChanges() {
        return otherLifeStyleChanges;
    }

    public void setOtherLifeStyleChanges(String otherLifeStyleChanges) {
        this.otherLifeStyleChanges = otherLifeStyleChanges;
    }

    public String getFollowupPreview() {
        return followupPreview;
    }

    public void setFollowupPreview(String followupPreview) {
        this.followupPreview = followupPreview;
    }

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }

    public String getInvestigationsDetail() {
        return investigationsDetail;
    }

    public void setInvestigationsDetail(String investigationsDetail) {
        this.investigationsDetail = investigationsDetail;
    }

    public String getConsultationDetail() {
        return consultationDetail;
    }

    public void setConsultationDetail(String consultationDetail) {
        this.consultationDetail = consultationDetail;
    }

    public String getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(String systolicBP) {
        this.systolicBP = systolicBP;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(String diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String bmi) {
        this.bmi = bmi;
    }

    public String getHypertension() {
        return hypertension;
    }

    public void setHypertension(String hypertension) {
        this.hypertension = hypertension;
    }

    public String getDysplipideamia() {
        return dysplipideamia;
    }

    public void setDysplipideamia(String dysplipideamia) {
        this.dysplipideamia = dysplipideamia;
    }

    public String getBmiCategory() {
        return bmi_category;
    }

    public void setBmiCategory(String bmi_category) {
        this.bmi_category = bmi_category;
    }

    public String getMetabolicSyndrome() {
        return metabolic_syndrome;
    }

    public void setMetabolicSyndrome(String metabolic_syndrome) {
        this.metabolic_syndrome = metabolic_syndrome;
    }

    public String getDiabetes() {
        return diabetes;
    }

    public void setDiabetes(String diabetes) {
        this.diabetes = diabetes;
    }

    public String getCardiacRisk() {
        return cardiac_risk;
    }

    public void setCardiacRisk(String cardiac_risk) {
        this.cardiac_risk = cardiac_risk;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public String getOverAllSeverity() {
        return overAllSeverity;
    }

    public void setOverAllSeverity(String overAllSeverity) {
        this.overAllSeverity = overAllSeverity;
    }

    public String getCallTag() {
        return callTag;
    }

    public void setCallTag(String callTag) {
        this.callTag = callTag;
    }

    public String getPackageCategory() {
        return packageCategory;
    }

    public void setPackageCategory(String packageCategory) {
        this.packageCategory = packageCategory;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int compareTo(DBProHealth o) {
        if (o == null)
            throw new IllegalArgumentException("DBProCheck is Null");

        return Comparators.stringCompare(id.toString(), o.getId().toString());
    }
}