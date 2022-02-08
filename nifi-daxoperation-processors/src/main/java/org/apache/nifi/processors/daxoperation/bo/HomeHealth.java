package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class HomeHealth {
   public ObjectId id;
   public Date checkupDate;
   public Float weight;
   public Integer pulse;
   public Integer bloodPressureSystolic;
   public Integer bloodPressureDiastolic;
   public Integer respiration;
   public Boolean oxygenUse;
   public Integer painScore;
   public Integer spO2;
   public String oxygenDevice;
   public String remarks;
   public Float temperature;
   public Integer oxygenValue;

   public ObjectId getId() {
       return id;
   }

   public void setId(ObjectId objectId) {
       this.id = objectId;
   }

   public Date getCheckupDate() {
       return checkupDate;
   }

   public void setCheckupDate(Date checkupDate ) {
       this.checkupDate = checkupDate;
   }

   public Float getWeight() {
       return weight;
   }

   public void setWeight(Float weight) {
       this.weight = weight;
   }

   public Integer getPulse() {
        return pulse;
    }

    public void setPulse(Integer pulse) {
        this.pulse = pulse;
    }

    public Integer getBloodPressureSystolic() {
        return bloodPressureSystolic;
    }

    public void setBloodPressureSystolic(Integer bloodPressureSystolic) {
        this.bloodPressureSystolic = bloodPressureSystolic;
    }

    public Integer getBloodPressureDiastolic() {
        return bloodPressureDiastolic;
    }

    public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) {
        this.bloodPressureDiastolic = bloodPressureDiastolic;
    }

    public Integer getRespiration() {
        return respiration;
    }

    public void setRespiration(Integer respiration) {
        this.respiration = respiration;
    }

    public Boolean getOxygenUse() {
        return oxygenUse;
    }

    public void setOxygenUse(Boolean oxygenUse) {
        this.oxygenUse = oxygenUse;
    }

    public Integer getPainScore() {
        return painScore;
    }

    public void setPainScore(Integer painScore) {
        this.painScore = painScore;
    }

    public Integer getSpO2() {
        return spO2;
    }

    public void setSpO2(Integer spO2) {
        this.spO2 = spO2;
    }

    public String getOxygenDevice() {
        return oxygenDevice;
    }

    public void setOxygenDevice(String oxygenDevice) {
        this.oxygenDevice = oxygenDevice;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Integer getOxygenValue() {
        return oxygenValue;
    }

    public void setOxygenValue(Integer oxygenValue) {
        this.oxygenValue = oxygenValue;
    }
}
