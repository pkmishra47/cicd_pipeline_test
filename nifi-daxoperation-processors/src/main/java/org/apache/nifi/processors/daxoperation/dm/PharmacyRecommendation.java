package org.apache.nifi.processors.daxoperation.dm;

public class PharmacyRecommendation {
    private String MOBILENO;
    private String ITEMS;

    public String getMOBILENO() {
        return MOBILENO;
    }

    public void setMOBILENO(String MOBILENO) {
        this.MOBILENO = MOBILENO;
    }

    public String getITEMS() {
        return this.ITEMS;
    }

    public void setITEMS(String ITEMS) {
        this.ITEMS = ITEMS;
    }
}
