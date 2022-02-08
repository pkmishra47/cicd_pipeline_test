package org.apache.nifi.processors.daxoperation.dbo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Embedded
public class DBUserPreferenceInfo 
{
	@Id ObjectId id = new ObjectId();
	@Property private String smsAlert;
	@Property private String emailAlert;
	@Property private List<String> dashboardToolList;
	
//	@Embedded 	private List<Widget> widgetList;
	
	
//	public List<Widget> getWidgetList() {
//		return widgetList;
//	}
//	public void setWidgetList(List<Widget> widgetList) {
//		this.widgetList = widgetList;
//	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getSmsAlert() {
		return smsAlert;
	}
	public void setSmsAlert(String smsAlert) {
		this.smsAlert = smsAlert;
	}
	public String getEmailAlert() {
		return emailAlert;
	}
	public void setEmailAlert(String emailAlert) {
		this.emailAlert = emailAlert;
	}
	public List<String> getDashboardToolList() {
		return dashboardToolList;
	}
	public void setDashboardToolList(List<String> dashboardToolList) {
		this.dashboardToolList = dashboardToolList;
	}
	
}
