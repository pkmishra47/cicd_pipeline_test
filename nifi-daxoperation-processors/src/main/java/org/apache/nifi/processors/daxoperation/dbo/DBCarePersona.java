package org.apache.nifi.processors.daxoperation.dbo;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(value = "CarePersona", noClassnameStored = true)
public class DBCarePersona implements Comparable<DBCarePersona> {

    @Id
    private ObjectId id;
    @Property
    private String mobileNumber;
    @Property
    private Date dateOfPrescription;
    @Property
    private String prescribedBy;
    @Property
    private Date createdDateTime;
    @Property
    private String orderId;
    @Property
    private String shopId;
    @Property
    private String prescribedById;
    @Reference(value = "prescription")
    private DBPrescription prescription = null;
    @Embedded("customerDetails")
    DBCustomerDetails customerDetails;

    @Override
    public int compareTo(DBCarePersona o) {
        if (o == null)
            throw new IllegalArgumentException("DBPrescription is Null");

        return Comparators.stringCompare(id.toString(), o.getId().toString());
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Date getDateOfPrescription() {
        return dateOfPrescription;
    }

    public void setDateOfPrescription(Date dateOfPrescription) {
        this.dateOfPrescription = dateOfPrescription;
    }

    public String getPrescribedBy() {
        return prescribedBy;
    }

    public void setPrescribedBy(String prescribedBy) {
        this.prescribedBy = prescribedBy;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getPrescribedById() {
        return prescribedById;
    }

    public void setPrescribedById(String prescribedById) {
        this.prescribedById = prescribedById;
    }

    public DBCustomerDetails getCustomerDetails() {
        return customerDetails;
    }

    public void setCustomerDetails(DBCustomerDetails customerDetails) {
        this.customerDetails = customerDetails;
    }

    public DBPrescription getPrescription() {
        return prescription;
    }

    public void setPrescription(DBPrescription prescription) {
        this.prescription = prescription;
    }
}