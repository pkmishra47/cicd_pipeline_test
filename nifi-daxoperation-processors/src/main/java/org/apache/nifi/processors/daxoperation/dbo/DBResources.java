package org.apache.nifi.processors.daxoperation.dbo;

public enum DBResources {
	TOOL("Tool"),
	ALLERGY("Allergy"),
	DOCTOR("Doctor"),
	MEDICALCONDITION("MedicalCondition"),
	HOSPITAL("Hospital"),
	IMMUNIZATION("Immunization"), 
	INSURANCE("Insurance"),
	MEDICATION("Medication"), 
	PROCEDURE("Procedure"),
	TESTNAME("Testname"),
	TPA("TPA");

	/*LABRESULT("LabResult"), 
	PRESCRIPTION("Prescription"), 
	HOSPITALVISIT("HospitalVisit"), 
	APPOINTMENT("Appointment"), 
	ALLERGYTYPE("AllergyType"),
	TESTRESULT("TestResult"),
	RESTRICTION("Restriction"),
	PROBLEM("Problem"),*/

	private String text;

	private DBResources(String text) {
		this.text=text;
	}

	public String getText(){
		return text;
	}

	public static DBResources fromString(String text) {
		DBResources dbRes = null;
		if (text == null)
			return dbRes;
		
		for (DBResources res : DBResources.values()) {
			if (text.equalsIgnoreCase(res.text)) {
				dbRes =  res;
				break;
			}
		}
		
		return dbRes;
	}
}