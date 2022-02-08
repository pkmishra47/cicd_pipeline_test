package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "PharmacyBills", noClassnameStored = true)
public class DBPharmacyBills  {
	@Id private ObjectId id;
	@Property private Date billdatetime;
	@Property private String customer_name;
	@Property private String mobileno;
	@Property private String partner_tracking;
	@Property private String bill_no;
	@Property private String site_id;
	@Property private String partner_id;
	@Property private String site_name;
	@Property private String site_location;
	@Property private String state;
	@Property private String city;
	@Property private String region;
	@Property private String address;
	
	@Embedded private List<DBPharmacyBillItems> lineItems = new ArrayList<DBPharmacyBillItems>();
	
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getBilldatetime() {
        return billdatetime;
    }

    public void setBilldatetime(Date billdatetime) {
        this.billdatetime = billdatetime;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getMobileno() {
        return mobileno;
    }

    public void setMobileno(String mobileno) {
        this.mobileno = mobileno;
    }

    public String getPartner_tracking() {
        return partner_tracking;
    }

    public void setPartner_tracking(String partner_tracking) {
        this.partner_tracking = partner_tracking;
    }

    public String getBill_no() {
        return bill_no;
    }

    public void setBill_no(String bill_no) {
        this.bill_no = bill_no;
    }

    public String getSite_id() {
        return site_id;
    }

    public void setSite_id(String site_id) {
        this.site_id = site_id;
    }

    public String getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(String partner_id) {
        this.partner_id = partner_id;
    }

    public String getSite_name() {
        return site_name;
    }

    public void setSite_name(String site_name) {
        this.site_name = site_name;
    }

    public String getSite_location() {
        return site_location;
    }

    public void setSite_location(String site_location) {
        this.site_location = site_location;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<DBPharmacyBillItems> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<DBPharmacyBillItems> lineItems) {
        this.lineItems = lineItems;
    }

	
}