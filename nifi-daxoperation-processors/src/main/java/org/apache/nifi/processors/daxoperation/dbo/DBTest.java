package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBTest {
	@Property private String testName; // This is referenced from LabTestMaster, remove LabTestDetail
	@Property private String testid;
	@Property private String source;
	@Embedded private List<DBLabResult> results = new ArrayList<DBLabResult>();
	@Property private String observation;
	@Property private String static_comments;
	@Property private String notes;
	@Property private String departmentName;
	@Property private String identifier;
	@Property private String patientservice;
	@Property private String visitId;
	@Property private String signingDocName;
	@Property private String doc_sign;
	@Property private String checkedBy;
	@Property private String laboratoryHod;
	@Property private boolean resultsReceived;
	@Property private int expectedTAT;
	@Property private Date followupDate;
	@Property private Date first_report_printed_on;
	@Property private Date collectedOn;
	@Property private Date reportedOn;
	@Property private Date receivedOn;
	@Property private Date printedOn;
	@Property private String drn;
	@Property private int blocked;
	@Property private String specimen;
	
	
	
	@Embedded("testResultFiles")
	private List<DBAttachement> testResultFiles = new ArrayList<DBAttachement>();

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public int getTestid() {
		int testid=0;
		try{
		testid=new Float(Float.parseFloat(this.testid)).intValue();;
		}catch(NumberFormatException e){
			testid=-1;
		}
		
		return testid;
	}
	
	public String getTestidString1(){
		String str; 
		str =  this.testid;
		str = str.replace(".0", "");
		return str;
	
	}		
	public String getTestidString(){
		return this.testid;
	}

	public void setTestid(int testid) {
		this.testid = Integer.toString(testid);
	}
	
	public void setTestid(String testid) {
		this.testid = testid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<DBLabResult> getResults() {
		return results;
	}

	public void setResults(List<DBLabResult> results) {
		this.results = results;
	}

	
	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getSigningDocName() {
		return signingDocName;
	}

	public void setSigningDocName(String signingDocName) {
		this.signingDocName = signingDocName;
	}

	public String getCheckedBy() {
		return checkedBy;
	}

	public void setCheckedBy(String checkedBy) {
		this.checkedBy = checkedBy;
	}

	public String getLaboratoryHod() {
		return laboratoryHod;
	}

	public void setLaboratoryHod(String laboratoryHod) {
		this.laboratoryHod = laboratoryHod;
	}

	public boolean isResultsReceived() {
		return resultsReceived;
	}

	public void setResultsReceived(boolean resultsReceived) {
		this.resultsReceived = resultsReceived;
	}

	public int getExpectedTAT() {
		return expectedTAT;
	}

	public void setExpectedTAT(int expectedTAT) {
		this.expectedTAT = expectedTAT;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getVisitId() {
		return visitId;
	}

	public void setVisitId(String visitId) {
		this.visitId = visitId;
	}

	public List<DBAttachement> getTestResultFiles() {
		return testResultFiles;
	}

	public void setTestResultFiles(List<DBAttachement> testResultFiles) {
		this.testResultFiles = testResultFiles;
	}

	public Date getFollowupDate() {
		return followupDate;
	}

	public void setFollowupDate(Date followupDate) {
		this.followupDate = followupDate;
	}

	public int getBlocked() {
		return blocked;
	}

	public void setBlocked(int blocked) {
		this.blocked = blocked;
	}

	public Date getFirst_report_printed_on() {
		return first_report_printed_on;
	}

	public void setFirst_report_printed_on(Date first_report_printed_on) {
		this.first_report_printed_on = first_report_printed_on;
	}

	public Date getPrintedOn() {
		return printedOn;
	}

	public void setPrintedOn(Date printedOn) {
		this.printedOn = printedOn;
	}

	public String getDrn() {
		return drn;
	}

	public void setDrn(String drn) {
		this.drn = drn;
	}

	public Date getCollectedOn() {
		return collectedOn;
	}

	public void setCollectedOn(Date collectedOn) {
		this.collectedOn = collectedOn;
	}

	public Date getReportedOn() {
		return reportedOn;
	}

	public void setReportedOn(Date reportedOn) {
		this.reportedOn = reportedOn;
	}

	public String getSpecimen() {
		return specimen;
	}

	public void setSpecimen(String specimen) {
		this.specimen = specimen;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public String getPatientservice() {
		return patientservice;
	}

	public void setPatientservice(String patientservice) {
		this.patientservice = patientservice;
	}

	public String getDoc_sign() {
		return doc_sign;
	}

	public void setDoc_sign(String doc_sign) {
		this.doc_sign = doc_sign;
	}

	public String getStatic_comments() {
		return static_comments;
	}

	public void setStatic_comments(String static_comments) {
		this.static_comments = static_comments;
	}
}