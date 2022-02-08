package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.apache.nifi.processors.daxoperation.utils.BCrypt;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.utils.IndexDirection;


@Entity(value = "Identity", noClassnameStored = true)
public class DBIdentity implements Comparable<DBIdentity>{
	@Id private ObjectId id;
	
	@Reference 
	private DBUser dbUser; // Instead this can be private ObjectId dbUserObjID;
	
	
	public static enum UhidSource {
		APP247,
		HOSPITAL
	}
	//Vijay: To satisfy this constraint we have to ensure that uhid & sitekey should be assigned to userId.
	@Indexed(value=IndexDirection.ASC, name="userIDIndex", unique=true, dropDups=true) 
	@Property private String userId;
	@Property private String password;
	@Property private String uhid;
	@Property private String activationCode;
	@Property private String otp;
	@Property private Date otpTime;
	@Property private String siteKey;
    @Property private Date registrationDate;
	@Property private String resetPasswordLinkKey;
	@Property private Date resetPasswordLinkGeneratedDate;
	@Property private Date lastLogOn;
	@Property private Date blockedTime;
	@Property private String isIndian;
	@Property private boolean askPasswordOnLogin ;
	@Property private String qalBloodPressureId;
	@Property private String qalBmiId;
	@Property private String mobileNumber;
	@Property private String activation_source;
	@Property private int otpAttempts;
	@Property private String googleId;
	@Property private String email;
	@Property private UhidSource uhidSource;
	
	private boolean isPasswordHashed = false;
	
	public Date getResetPasswordLinkGeneratedDate() {
		return resetPasswordLinkGeneratedDate;
	}

	public void setResetPasswordLinkGeneratedDate(
			Date resetPasswordLinkGeneratedDate) {
		this.resetPasswordLinkGeneratedDate = resetPasswordLinkGeneratedDate;
	}

	public Date getLastLogOn() {
		return lastLogOn;
	}

	public void setOtpTime(Date otpTime) {
		this.otpTime = otpTime;
	}
	public Date getOtpTime() {
		return otpTime;
	}

	public void setLastLogOn(Date lastLogOn) {
		this.lastLogOn = lastLogOn;
	}
	public String getResetPasswordLinkKey() {
		return resetPasswordLinkKey;
	}

	public void setResetPasswordLinkKey(String resetPasswordLinkKey) {
		this.resetPasswordLinkKey = resetPasswordLinkKey;
	}

	@Override
	public int compareTo(DBIdentity o) {
		return 0;
	}

	public DBUser getDbUser() {
		return dbUser;
	}

	public void setDbUser(DBUser dbUser) {
		this.dbUser = dbUser;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if(!isPasswordHashed)
			this.password = BCrypt.hashpw(password, BCrypt.gensalt());
		else
			this.password = password;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	
	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}
	
	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
	
	public String getSiteKey() {
		return siteKey;
	}

	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public boolean isAskPasswordOnLogin() {
		return askPasswordOnLogin;
	}

	public void setAskPasswordOnLogin(boolean askPasswordOnLogin) {
		this.askPasswordOnLogin = askPasswordOnLogin;
	}

	public boolean isPasswordHashed() {
		return isPasswordHashed;
	}

	public void setPasswordHashed(boolean isPasswordHashed) {
		this.isPasswordHashed = isPasswordHashed;
	}

	public String getQalBloodPressureId() {
		return qalBloodPressureId;
	}

	public void setQalBloodPressureId(String qalBloodPressureId) {
		this.qalBloodPressureId = qalBloodPressureId;
	}

	public String getQalBmiId() {
		return qalBmiId;
	}

	public void setQalBmiId(String qalBmiId) {
		this.qalBmiId = qalBmiId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	
	public String getActivation_Source() {
		return activation_source;
	}

	public void setActivation_Source(String activation_source) {
		this.activation_source = activation_source;
	}

	public int getOtpAttempts() {
		return otpAttempts;
	}

	public void setOtpAttempts(int otpAttempts) {
		this.otpAttempts = otpAttempts;
	}

	public Date getBlockedTime() {
		return blockedTime;
	}

	public void setBlockedTime(Date blockedTime) {
		this.blockedTime = blockedTime;
	}

	public String getGoogleId() {
		return googleId;
	}

	public void setGoogleId(String googleId) {
		this.googleId = googleId;
	}

	public String getIsIndian() {
		return isIndian;
	}

	public void setIsIndian(String isIndian) {
		this.isIndian = isIndian;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UhidSource getUhidSource() {
		return uhidSource;
	}

	public void setUhidSource(UhidSource uhidSource) {
		this.uhidSource = uhidSource;
	}


}
