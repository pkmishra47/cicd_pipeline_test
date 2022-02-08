package org.apache.nifi.processors.daxoperation.dm;

public class Procedure {
    private Procedure procedure;
    private String id;
    private String uhid;
    private String name;
    private String doctor_name;
    private String procedure_date;

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getProcedure_date() {
        return procedure_date;
    }

    public void setProcedure_date(String procedure_date) {
        this.procedure_date = procedure_date;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
