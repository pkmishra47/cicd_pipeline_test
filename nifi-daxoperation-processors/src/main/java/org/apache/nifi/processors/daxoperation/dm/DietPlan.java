package org.apache.nifi.processors.daxoperation.dm;

public class DietPlan {
    private String UHID;
    private String CREATED_AT;
    private String PARAMETER_NAME;
    private String PARAMETER_VAL;
    private String PARAMETER_DETIALVAL;
    private String FIRSTROWVALUE;
    private String ROWVALUE;

    public String getUHID() {
        return UHID;
    }

    public void setUHID(String UHID) {
        this.UHID = UHID;
    }

    public String getCREATED_AT() {
        return CREATED_AT;
    }

    public void setCREATED_AT(String CREATED_AT) {
        this.CREATED_AT = CREATED_AT;
    }

    public String getPARAMETER_NAME() {
        return PARAMETER_NAME;
    }

    public void setPARAMETER_NAME(String PARAMETER_NAME) {
        this.PARAMETER_NAME = PARAMETER_NAME;
    }

    public String getPARAMETER_VAL() {
        return PARAMETER_VAL;
    }

    public void setPARAMETER_VAL(String PARAMETER_VAL) {
        this.PARAMETER_VAL = PARAMETER_VAL;
    }

    public String getPARAMETER_DETIALVAL() {
        return PARAMETER_DETIALVAL;
    }

    public void setPARAMETER_DETIALVAL(String PARAMETER_DETIALVAL) {
        this.PARAMETER_DETIALVAL = PARAMETER_DETIALVAL;
    }

    public String getFIRSTROWVALUE() {
        return FIRSTROWVALUE;
    }

    public void setFIRSTROWVALUE(String FIRSTROWVALUE) {
        this.FIRSTROWVALUE = FIRSTROWVALUE;
    }

    public String getROWVALUE() {
        return ROWVALUE;
    }

    public void setROWVALUE(String ROWVALUE) {
        this.ROWVALUE = ROWVALUE;
    }
}
