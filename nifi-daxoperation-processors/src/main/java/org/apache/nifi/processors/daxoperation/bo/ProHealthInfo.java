package org.apache.nifi.processors.daxoperation.bo;

import java.util.ArrayList;
import java.util.List;

public class ProHealthInfo {
    private long testDate;
	private String ahcno;
	private String guestLocation;
	private String proHealthLocation;
	private String contactPreference;
	private String ahcphysician;
	private String doctorsSpeciality;
	private String height;
	private String weight;
	private String impression;
	private String dietChanges;
	private String activityChanges;
	private String otherLifeStyleChanges;
	private String followupPreview;
	private String allergy;
	private List<InvestigationDetail> investigationsDetails = new ArrayList<>();
	private List<ConsultationDetail> consultationDetails = new ArrayList<>();
	private List<SugarLabresult> tests = new ArrayList<>();
	private String systolicBP;
	private String diastolicBP;
	private String bmi;

	public long getTestDate() {
		return testDate;
	}

	public void setTestDate(long testDate) {
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

	public List<InvestigationDetail> getInvestigationsDetails() {
		return investigationsDetails;
	}

	public void setInvestigationsDetails(List<InvestigationDetail> investigationsDetails) {
		this.investigationsDetails = investigationsDetails;
	}

	public List<ConsultationDetail> getConsultationDetails() {
		return consultationDetails;
	}

	public void setConsultationDetails(List<ConsultationDetail> consultationDetails) {
		this.consultationDetails = consultationDetails;
	}

	public List<SugarLabresult> getTests() {
		return tests;
	}

	public void setTests(List<SugarLabresult> tests) {
		this.tests = tests;
	}

	public String getSystolicBP() {
		return systolicBP;
	}

	public void setSystolicBP(String systolicBP) {
		this.systolicBP = systolicBP;
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
}
