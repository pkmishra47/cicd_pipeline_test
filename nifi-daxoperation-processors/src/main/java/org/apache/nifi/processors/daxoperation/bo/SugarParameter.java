package org.apache.nifi.processors.daxoperation.bo;

public class SugarParameter {
    private String parameterId;
	private String parameterName;
	private String std_parameterName;
	private String value;
	private String unit;
	private String range;

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getstd_parameterName() {
		return std_parameterName;
	}

	public void setstd_parameterName(String std_parameterName) {
		this.std_parameterName = std_parameterName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}
}
