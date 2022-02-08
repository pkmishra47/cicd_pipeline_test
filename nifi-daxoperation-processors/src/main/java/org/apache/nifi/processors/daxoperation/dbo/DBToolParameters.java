package org.apache.nifi.processors.daxoperation.dbo;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBToolParameters {
	@Property private String testName;
	@Property private String testValue;
	@Property private String range;
	@Property private String units;
	@Property private String paramType;
	
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	public String getTestValue() {
		return testValue;
	}
	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	public String getParamType() {
		return paramType;
	}
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}	
	
}
