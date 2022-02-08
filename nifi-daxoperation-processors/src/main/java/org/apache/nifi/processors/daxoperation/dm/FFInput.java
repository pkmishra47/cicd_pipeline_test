package org.apache.nifi.processors.daxoperation.dm;

import java.util.Date;
import java.util.List;

import org.apache.nifi.processors.daxoperation.dbo.DBsms;

import java.util.Map;

public class FFInput {
    public String executionID = new Date().getTime() + "";
    public SiteDetails siteDetails;
    public Map<String, SiteDetails> siteMasterMap;
    public List<Patient> patients;
    public List<SpecialResult> specialResults;
    public List<DischargeSummary> dischargeSummaries;
    public List<DischargeSummary> prescriptions;
    public List<Bills> bill_group;
    public List<ClinicProhealth> clinicprohealth;
    public List<Consultation> consultation;
    public List<ProHealth> prohealth;
    public List<DietPlan> dietPlan;
    public List<HomehealthVisit> homehealthVisit;
    public List<PharmacyBill> pharmacyBill;
    public List<Procedure> procedure;
    public List<Tool> tool;
    public List<PharmacyRecommendation> pharmacyRecommendations;
    public DBsms sms;
    public String siteApiKey;
}
