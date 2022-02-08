package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "AuthData", noClassnameStored = true)
public class DBAuthToken implements Comparable<DBAuthToken> {
	
	@Reference 
	private DBUser dbUser;
	@Reference 
	private DBIdentity identity;
	
	@Id private ObjectId id;
	@Property private String mobileNumber;
	@Property private String authToken;
	@Property private String isOtp;
	@Property private Date Date;
	@Property private String email;
	@Property private String uhid;
	@Property private List<String> assocUhid = new ArrayList<String>();

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setDate(Date Date) {
		this.Date = Date;
	}

	public Date getDate() {
		return Date;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAuthToken() {
		return authToken;
	}
	public List<String> getAssocUhid() {
		return assocUhid;
	}
	public void setAssocUhid(List<String> assocUhid) {
		this.assocUhid = assocUhid;
	}
	public DBUser getDbUser() {
		return dbUser;
	}

	public void setDbUser(DBUser dbUser) {
		this.dbUser = dbUser;
	}


	@Override
	public int compareTo(DBAuthToken o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public DBIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(DBIdentity identity) {
		this.identity = identity;
	}

	public String getIsOtp() {
		return isOtp;
	}

	public void setIsOtp(String isOtp) {
		this.isOtp = isOtp;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
}