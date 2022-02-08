package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;


/*
 *  A lab test can be conducted over a period of time
 *  each time a new test is taken the values are set
 *  over a period of time we can plot a graph of the lab results
 *
 */
@Entity(value = "LabTest", noClassnameStored = true)
public class DBLabTest implements Comparable<DBLabTest> {
    @Id
    private ObjectId id;
    @Property
    private Date testDate;
    @Property
    private String referredBy;
    @Property
    private String consultId;
    @Property
    private String tag;
    @Property
    private String orderId;
    @Property
    private String billId;
    @Property
    private String siteKey;
    @Property
    private Date dateImported;
    @Property
    private Date createdDateTime;
    @Property
    private Date updatedDateTime;
    @Property
    private String uhid;
    @Property
    private String packageId;
    @Property
    private String packageName;
    @Property
    private int ownedBy;         //0 - user, 1-corporate, 2-both
    @Embedded
    private List<DBTest> tests = new ArrayList<DBTest>();
    @Embedded
    private List<DBAttachement> labTestFiles = new ArrayList<DBAttachement>();
    @Embedded
    private List<DBAttachement> labTestSmartReports = new ArrayList<DBAttachement>();
    @Property
    private String source;
    @Property
    private String identifier;
    @Property
    private String visitId;
    @Property
    private String mobileNumber;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public Date getDateImported() {
        return dateImported;
    }

    public void setDateImported(Date dateImported) {
        this.dateImported = dateImported;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(Date updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }

    public List<DBTest> getTests() {
        return tests;
    }

    public void setTests(List<DBTest> tests) {
        this.tests = tests;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(int ownedBy) {
        this.ownedBy = ownedBy;
    }

    @Override
    public int compareTo(DBLabTest dbLabTest) {
        if (dbLabTest == null)
            throw new IllegalArgumentException("DBLabTest is Null");

        return Comparators.stringCompare(getId().toString(), dbLabTest.getId().toString());
    }

    public String getConsultId() {
        return consultId;
    }

    public void setConsultId(String consultId) {
        this.consultId = consultId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<DBAttachement> getLabTestFiles() {
        return labTestFiles;
    }

    public void setLabTestFiles(List<DBAttachement> labTestFiles) {
        this.labTestFiles = labTestFiles;
    }

    public List<DBAttachement> getLabTestSmartReports() {
        return labTestSmartReports;
    }

    public void setLabTestSmartReports(List<DBAttachement> labTestSmartReports) {
        this.labTestSmartReports = labTestSmartReports;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}