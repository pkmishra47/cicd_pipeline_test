package org.apache.nifi.processors.daxoperation.dbo;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


public class DBEmergencyContact implements Comparable<DBEmergencyContact>{
	@Id ObjectId id = new ObjectId();
	@Property private String salutation;
	@Property private String firstName;    
	@Property private String middleName; 
	@Property private String lastName;
	@Property private String phone1; 
	@Property private String phone2; 
	@Property private String email1; 


	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public String getPhone2() {
		return phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	public String getEmail1() {
		return email1;
	}

	public void setEmail1(String email1) {
		this.email1 = email1;
	} 

	@Override
	public int compareTo(DBEmergencyContact arg0) {
		if(arg0 == null)
			throw new IllegalArgumentException("DBEmergencyContact is Null");

		return Comparators.stringCompare(id.toString(), arg0.getId().toString());
	}

}
