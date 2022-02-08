package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;


@Entity(value = "CardiacRiskScore", noClassnameStored = true)
public class DBCardiacRiskScore  {
	
	
	@Id private ObjectId id;
	@Property private String cardiac_id;
	@Property private String uhid;
	@Property private String patientName;
	@Property private String age;
	@Property private String gender;
	@Property private String centerId;
	@Property private String dateOfVisit;
	@Property private String prediction_heartRisk;
	@Property private String prediction_risk;
	@Property private String prediction_score;
	@Property private String prediction_optimal;
	@Property private String bmi;
	@Property private String tobacco;
	@Property private String dyslipidemia;
	@Property private String physicalInactivity;
	@Property private String smoking;
	@Property private String framinghamRisk_risk;
	@Property private String framinghamRisk_score;
	@Property private String framinghamRisk_optimal;
	@Property private String labInvestigationRecommended_completeBloodCount;
	@Property private String labInvestigationRecommended_fastingBloodSugar;
	@Property private String labInvestigationRecommended_lipidProfile;
	@Property private String labInvestigationRecommended_ureaCreatinine;
	@Property private String labInvestigationRecommended_HBA1C;
	@Property private String diagnostic_ecg;
	@Property private String diagnostic_chestXray;
	@Property private String diagnostic_echo;
	@Property private String diagnostic_tmt;
	@Property private String diagnostic_usg_kub;
	@Property private String diagnostic_usg;
	@Property private String diagnostic_cardiacCT;
	@Property private String diagnostic_coronaryAngiography;
	@Property private String department;
	@Property private String urgent;
	@Property private String general_treatment;
	@Property private String treatmentofCAD;
	@Property private String General_advice;
	@Property private String repeat_unit;
	@Property private String repeat_duration;
	@Property private String repeat_comment;
	@Property private String offset;
	@Property private String errors;
	@Property private String correlationId;
	@Property private String responseURL;
	
	
	public String getCardiac_id() {
		return cardiac_id;
	}
	public void setCardiac_id(String cardiac_id) {
		this.cardiac_id = cardiac_id;
	}
	public String getUhid() {
		return uhid;
	}
	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getCenterId() {
		return centerId;
	}
	public void setCenterId(String centerId) {
		this.centerId = centerId;
	}
	public String getDateOfVisit() {
		return dateOfVisit;
	}
	public void setDateOfVisit(String dateOfVisit) {
		this.dateOfVisit = dateOfVisit;
	}
	public String getPrediction_heartRisk() {
		return prediction_heartRisk;
	}
	public void setPrediction_heartRisk(String prediction_heartRisk) {
		this.prediction_heartRisk = prediction_heartRisk;
	}
	public String getPrediction_risk() {
		return prediction_risk;
	}
	public void setPrediction_risk(String prediction_risk) {
		this.prediction_risk = prediction_risk;
	}
	public String getPrediction_score() {
		return prediction_score;
	}
	public void setPrediction_score(String prediction_score) {
		this.prediction_score = prediction_score;
	}
	public String getPrediction_optimal() {
		return prediction_optimal;
	}
	public void setPrediction_optimal(String prediction_optimal) {
		this.prediction_optimal = prediction_optimal;
	}
	public String getBmi() {
		return bmi;
	}
	public void setBmi(String bmi) {
		this.bmi = bmi;
	}
	public String getTobacco() {
		return tobacco;
	}
	public void setTobacco(String tobacco) {
		this.tobacco = tobacco;
	}
	public String getDyslipidemia() {
		return dyslipidemia;
	}
	public void setDyslipidemia(String dyslipidemia) {
		this.dyslipidemia = dyslipidemia;
	}
	public String getPhysicalInactivity() {
		return physicalInactivity;
	}
	public void setPhysicalInactivity(String physicalInactivity) {
		this.physicalInactivity = physicalInactivity;
	}
	public String getSmoking() {
		return smoking;
	}
	public void setSmoking(String smoking) {
		this.smoking = smoking;
	}
	public String getFraminghamRisk_risk() {
		return framinghamRisk_risk;
	}
	public void setFraminghamRisk_risk(String framinghamRisk_risk) {
		this.framinghamRisk_risk = framinghamRisk_risk;
	}
	public String getFraminghamRisk_score() {
		return framinghamRisk_score;
	}
	public void setFraminghamRisk_score(String framinghamRisk_score) {
		this.framinghamRisk_score = framinghamRisk_score;
	}
	public String getFraminghamRisk_optimal() {
		return framinghamRisk_optimal;
	}
	public void setFraminghamRisk_optimal(String framinghamRisk_optimal) {
		this.framinghamRisk_optimal = framinghamRisk_optimal;
	}
	public String getLabInvestigationRecommended_completeBloodCount() {
		return labInvestigationRecommended_completeBloodCount;
	}
	public void setLabInvestigationRecommended_completeBloodCount(
			String labInvestigationRecommended_completeBloodCount) {
		this.labInvestigationRecommended_completeBloodCount = labInvestigationRecommended_completeBloodCount;
	}
	public String getLabInvestigationRecommended_fastingBloodSugar() {
		return labInvestigationRecommended_fastingBloodSugar;
	}
	public void setLabInvestigationRecommended_fastingBloodSugar(
			String labInvestigationRecommended_fastingBloodSugar) {
		this.labInvestigationRecommended_fastingBloodSugar = labInvestigationRecommended_fastingBloodSugar;
	}
	public String getLabInvestigationRecommended_lipidProfile() {
		return labInvestigationRecommended_lipidProfile;
	}
	public void setLabInvestigationRecommended_lipidProfile(String labInvestigationRecommended_lipidProfile) {
		this.labInvestigationRecommended_lipidProfile = labInvestigationRecommended_lipidProfile;
	}
	public String getLabInvestigationRecommended_ureaCreatinine() {
		return labInvestigationRecommended_ureaCreatinine;
	}
	public void setLabInvestigationRecommended_ureaCreatinine(String labInvestigationRecommended_ureaCreatinine) {
		this.labInvestigationRecommended_ureaCreatinine = labInvestigationRecommended_ureaCreatinine;
	}
	public String getLabInvestigationRecommended_HBA1C() {
		return labInvestigationRecommended_HBA1C;
	}
	public void setLabInvestigationRecommended_HBA1C(String labInvestigationRecommended_HBA1C) {
		this.labInvestigationRecommended_HBA1C = labInvestigationRecommended_HBA1C;
	}
	public String getDiagnostic_ecg() {
		return diagnostic_ecg;
	}
	public void setDiagnostic_ecg(String diagnostic_ecg) {
		this.diagnostic_ecg = diagnostic_ecg;
	}
	public String getDiagnostic_chestXray() {
		return diagnostic_chestXray;
	}
	public void setDiagnostic_chestXray(String diagnostic_chestXray) {
		this.diagnostic_chestXray = diagnostic_chestXray;
	}
	public String getDiagnostic_echo() {
		return diagnostic_echo;
	}
	public void setDiagnostic_echo(String diagnostic_echo) {
		this.diagnostic_echo = diagnostic_echo;
	}
	public String getDiagnostic_tmt() {
		return diagnostic_tmt;
	}
	public void setDiagnostic_tmt(String diagnostic_tmt) {
		this.diagnostic_tmt = diagnostic_tmt;
	}
	public String getDiagnostic_usg_kub() {
		return diagnostic_usg_kub;
	}
	public void setDiagnostic_usg_kub(String diagnostic_usg_kub) {
		this.diagnostic_usg_kub = diagnostic_usg_kub;
	}
	public String getDiagnostic_usg() {
		return diagnostic_usg;
	}
	public void setDiagnostic_usg(String diagnostic_usg) {
		this.diagnostic_usg = diagnostic_usg;
	}
	public String getDiagnostic_cardiacCT() {
		return diagnostic_cardiacCT;
	}
	public void setDiagnostic_cardiacCT(String diagnostic_cardiacCT) {
		this.diagnostic_cardiacCT = diagnostic_cardiacCT;
	}
	public String getDiagnostic_coronaryAngiography() {
		return diagnostic_coronaryAngiography;
	}
	public void setDiagnostic_coronaryAngiography(String diagnostic_coronaryAngiography) {
		this.diagnostic_coronaryAngiography = diagnostic_coronaryAngiography;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getUrgent() {
		return urgent;
	}
	public void setUrgent(String urgent) {
		this.urgent = urgent;
	}
	public String getGeneral_treatment() {
		return general_treatment;
	}
	public void setGeneral_treatment(String general_treatment) {
		this.general_treatment = general_treatment;
	}
	public String getTreatmentofCAD() {
		return treatmentofCAD;
	}
	public void setTreatmentofCAD(String treatmentofCAD) {
		this.treatmentofCAD = treatmentofCAD;
	}
	public String getGeneral_advice() {
		return General_advice;
	}
	public void setGeneral_advice(String general_advice) {
		General_advice = general_advice;
	}
	public String getRepeat_unit() {
		return repeat_unit;
	}
	public void setRepeat_unit(String repeat_unit) {
		this.repeat_unit = repeat_unit;
	}
	public String getRepeat_duration() {
		return repeat_duration;
	}
	public void setRepeat_duration(String repeat_duration) {
		this.repeat_duration = repeat_duration;
	}
	public String getRepeat_comment() {
		return repeat_comment;
	}
	public void setRepeat_comment(String repeat_comment) {
		this.repeat_comment = repeat_comment;
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getErrors() {
		return errors;
	}
	public void setErrors(String errors) {
		this.errors = errors;
	}
	public String getCorrelationId() {
		return correlationId;
	}
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	public String getResponseURL() {
		return responseURL;
	}
	public void setResponseURL(String responseURL) {
		this.responseURL = responseURL;
	}
	
}