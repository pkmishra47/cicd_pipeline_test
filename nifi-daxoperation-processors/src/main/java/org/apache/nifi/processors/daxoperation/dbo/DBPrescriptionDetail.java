package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;


@Embedded
public class DBPrescriptionDetail {
	@Property private String name;
	@Property private String strength;
	@Property private int route;
	@Embedded private List<Integer> howOften = new ArrayList<Integer>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStrength() {
		return strength;
	}
	public void setStrength(String strength) {
		this.strength = strength;
	}
	public int getRoute() {
		return route;
	}
	public void setRoute(int route) {
		this.route = route;
	}
	public List<Integer> getHowOften() {
		return howOften;
	}
	public void setHowOften(List<Integer> howOften) {
		this.howOften = howOften;
	}
}