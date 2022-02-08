package org.apache.nifi.processors.daxoperation.dm;

public class ClinicProhealth {
    private String mr_no;
    private String CreatedOn;
    private String CityName;
    private String visit_id;
    private String act_description;
    private String HospitalName;
    private String CONTACTPREFERENCE;
    private String ref_doct_name;
    private String SpecialityName;
    private String MSCardiacScoreRequest;
    private String ComprehensiveHRASubmitPayLoad;
    private String allergies;

    public String getMr_no() {
        return mr_no;
    }

    public void setMr_no(String mr_no) {
        this.mr_no = mr_no;
    }

    public String getCreatedOn() {
        return CreatedOn;
    }

    public void setCreatedOn(String createdOn) {
        CreatedOn = createdOn;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getVisit_id() {
        return visit_id;
    }

    public void setVisit_id(String visit_id) {
        this.visit_id = visit_id;
    }

    public String getAct_description() {
        return act_description;
    }

    public void setAct_description(String act_description) {
        this.act_description = act_description;
    }

    public String getHospitalName() {
        return HospitalName;
    }

    public void setHospitalName(String hospitalName) {
        HospitalName = hospitalName;
    }

    public String getCONTACTPREFERENCE() {
        return CONTACTPREFERENCE;
    }

    public void setCONTACTPREFERENCE(String CONTACTPREFERENCE) {
        this.CONTACTPREFERENCE = CONTACTPREFERENCE;
    }

    public String getRef_doct_name() {
        return ref_doct_name;
    }

    public void setRef_doct_name(String ref_doct_name) {
        this.ref_doct_name = ref_doct_name;
    }

    public String getSpecialityName() {
        return SpecialityName;
    }

    public void setSpecialityName(String specialityName) {
        SpecialityName = specialityName;
    }

    public String getMSCardiacScoreRequest() {
        return MSCardiacScoreRequest;
    }

    public void setMSCardiacScoreRequest(String MSCardiacScoreRequest) {
        this.MSCardiacScoreRequest = MSCardiacScoreRequest;
    }

    public String getComprehensiveHRASubmitPayLoad() {
        return ComprehensiveHRASubmitPayLoad;
    }

    public void setComprehensiveHRASubmitPayLoad(String comprehensiveHRASubmitPayLoad) {
        ComprehensiveHRASubmitPayLoad = comprehensiveHRASubmitPayLoad;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
}
