package org.apache.nifi.processors.daxoperation.bo;

public class InvestigationDetail {
    private String testName;
	private String testSchedule;
	private String repeatFrequency;
	private String repeatEveryFreq;
	private String checkDaysRep;
	private String testNotesInstructions;

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestSchedule() {
		return testSchedule;
	}

	public void setTestSchedule(String testSchedule) {
		this.testSchedule = testSchedule;
	}

	public String getRepeatFrequency() {
		return repeatFrequency;
	}

	public void setRepeatFrequency(String repeatFrequency) {
		this.repeatFrequency = repeatFrequency;
	}

	public String getRepeatEveryFreq() {
		return repeatEveryFreq;
	}

	public void setRepeatEveryFreq(String repeatEveryFreq) {
		this.repeatEveryFreq = repeatEveryFreq;
	}

	public String getCheckDaysRep() {
		return checkDaysRep;
	}

	public void setCheckDaysRep(String checkDaysRep) {
		this.checkDaysRep = checkDaysRep;
	}

	public String getTestNotesInstructions() {
		return testNotesInstructions;
	}

	public void setTestNotesInstructions(String testNotesInstructions) {
		this.testNotesInstructions = testNotesInstructions;
	}
}
