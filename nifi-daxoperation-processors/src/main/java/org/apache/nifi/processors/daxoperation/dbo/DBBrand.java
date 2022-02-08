package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Brand", noClassnameStored = true)
public class DBBrand {
	@Id private ObjectId id;
	@Property private String name;
	@Property private String logoUrl;
	@Property private String mobileNumber;
	@Property private String reportLogoUrl;
	@Property private String loginUrl;
	@Property private String copyright;
	@Property private int brandWeight;
	

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getlogoUrl() {
		return logoUrl;
	}

	public void setlogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getreportLogoUrl() {
		return reportLogoUrl;
	}

	public void setreportLogoUrl(String reportLogoUrl) {
		this.reportLogoUrl = reportLogoUrl;
	}

	public String getloginUrl() {
		return loginUrl;
	}

	public void setloginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public int getbrandWeight() {
		return brandWeight;
	}

	public void setbrandWeight(int brandWeight) {
		this.brandWeight = brandWeight;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}	
}
