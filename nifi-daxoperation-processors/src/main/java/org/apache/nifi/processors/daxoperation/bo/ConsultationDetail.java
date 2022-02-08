package org.apache.nifi.processors.daxoperation.bo;

public class ConsultationDetail {
    private String specialityName;
	private String doctorName;
	private String testNotesInstructions;

	public String getSpecialityName() {
		return specialityName;
	}

	public void setSpecialityName(String specialityName) {
		this.specialityName = specialityName;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getTestNotesInstructions() {
		return testNotesInstructions;
	}

	public void setTestNotesInstructions(String testNotesInstructions) {
		this.testNotesInstructions = testNotesInstructions;
	}
}
