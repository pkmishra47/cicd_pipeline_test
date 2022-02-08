package org.apache.nifi.processors.daxoperation.bo;

import java.util.ArrayList;
import java.util.List;

public class SugarLabresult {
    private String testId;
	private String testName;
	private long testDate;
	private List<SugarParameter> parameters = new ArrayList<>();

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public long getTestDate() {
		return testDate;
	}

	public void setTestDate(long testDate) {
		this.testDate = testDate;
	}

	public List<SugarParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<SugarParameter> parameters) {
		this.parameters = parameters;
	}
}
