package org.apache.nifi.processors.daxoperation.bo;

import java.util.Date;
import java.util.List;

public class Test {
    private String testName;
    private String source;
    private List<LabResult> results;
    private Integer testId;
    private String observation;
    private String notes;
    private List<Attachment> testResultFiles;
    private String departmentName;
    private Integer expectedTAT;
    private String identifier;
    private String signingDocName;
    private String checkedBy;
    private String laboratoryHod;
    private Date followupDate;
    private Date first_report_printed_on;
    private Date printedOn;
    private String drn;
    private String visitId;
    private Date collectedOn;
    private Date reportedOn;
    private String specimen;
    private Date receivedOn;
    private String patientservice;
    private String doc_sign;

    public String getTestName() {
        return this.testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<LabResult> getResults() {
        return this.results;
    }

    public void setResults(List<LabResult> results) {
        this.results = results;
    }

    public Integer getTestId() {
        return this.testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public String getObservation() {
        return this.observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Attachment> getTestResultFiles() {
        return this.testResultFiles;
    }

    public void setTestResultFiles(List<Attachment> testResultFiles) {
        this.testResultFiles = testResultFiles;
    }

    public String getDepartmentName() {
        return this.departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getExpectedTAT() {
        return this.expectedTAT;
    }

    public void setExpectedTAT(Integer expectedTAT) {
        this.expectedTAT = expectedTAT;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSigningDocName() {
        return this.signingDocName;
    }

    public void setSigningDocName(String signingDocName) {
        this.signingDocName = signingDocName;
    }

    public String getCheckedBy() {
        return this.checkedBy;
    }

    public void setCheckedBy(String checkedBy) {
        this.checkedBy = checkedBy;
    }

    public String getLaboratoryHod() {
        return this.laboratoryHod;
    }

    public void setLaboratoryHod(String laboratoryHod) {
        this.laboratoryHod = laboratoryHod;
    }

    public Date getFollowupDate() {
        return this.followupDate;
    }

    public void setFollowupDate(Date followupDate) {
        this.followupDate = followupDate;
    }

    public Date getFirstReportPrintedOn() {
        return this.first_report_printed_on;
    }

    public void setFirstReportPrintedOn(Date firstReportPrintedOn) {
        this.first_report_printed_on = firstReportPrintedOn;
    }

    public Date getPrintedOn() {
        return this.printedOn;
    }

    public void setPrintedOn(Date printedOn) {
        this.printedOn = printedOn;
    }
    
    public String getDrn() {
        return this.drn;
    }

    public void setDrn(String drn) {
        this.drn = drn;
    }

    public String getVisitId() {
        return this.visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public Date getCollectedOn() {
        return this.collectedOn;
    }

    public void setCollectedOn(Date collectedOn) {
        this.collectedOn = collectedOn;
    }

    public Date getReportedOn() {
        return this.reportedOn;
    }

    public void setReportedOn(Date reportedOn) {
        this.reportedOn = reportedOn;
    }

    public String getSpecimen() {
        return this.specimen;
    }

    public void setSpecimen(String specimen) {
        this.specimen = specimen;
    }

    public Date getReceivedOn() {
        return this.receivedOn;
    }

    public void setReceivedOn(Date receivedOn) {
        this.receivedOn = receivedOn;
    }

    public String getPatientservice() {
        return this.patientservice;
    }

    public void setPatientservice(String patientservice) {
        this.patientservice = patientservice;
    }

    public String getDocSign() {
        return this.doc_sign;
    }

    public void setDocSign(String docSign) {
        this.doc_sign = docSign;
    }
}
