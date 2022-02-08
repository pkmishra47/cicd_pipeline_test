package org.apache.nifi.processors.daxoperation.models;

public enum IdType {
    pancard("913"),
    aadharcard("914"),
    license("917");

    private String value;

    // getter method
    public String getValue()
    {
        return this.value;
    }

    IdType(String value)
    {
        this.value = value;
    }
}
