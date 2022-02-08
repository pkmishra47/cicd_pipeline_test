package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBLabResult {
	@Property private int parameterId;
	@Property private String parameterName;
	@Property private String std_parameterName;
	@Property private int sequence;
	@Property private String result;
	@Property private String unit;
	@Property private String range;
	@Property private boolean outOfRange;
	@Property private Date resultDateTime;
	
	public int getParameterId() {
		return parameterId;
	}
	public void setParameterId(int parameterId) {
		this.parameterId = parameterId;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getRange() {
		return range;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public boolean isOutOfRange() {
		return outOfRange;
	}
	public void setOutOfRange(boolean outOfRange) {
		this.outOfRange = outOfRange;
	}
	public Date getResultDateTime() {
		return resultDateTime;
	}
	public void setResultDateTime(Date resultDateTime) {
		this.resultDateTime = resultDateTime;
	}

	public String getStd_parameterName() {
		return std_parameterName;
	}

	public void setStd_parameterName(String std_parameterName) {
		this.std_parameterName = std_parameterName;
	}
}
