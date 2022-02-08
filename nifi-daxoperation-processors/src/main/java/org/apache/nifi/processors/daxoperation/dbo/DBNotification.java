package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

//import com.healthhiway.businessobject.NotificationLevel;
//import com.healthhiway.businessobject.NotificationType;

/*
 * The logic:
 * 
 * There is one row for each notification for each user, say for example if user1 is having 3 notification
 * there would be three rows for each notification and each one of them will have the value of isRead to false;
 * Once the user consumes the notification this isRead would be marked as true.
 * 
 * A pruning logic would go ahead and delete all events that are consumed. Currently this notification is made to be
 * simple, that is, there is no event type mentioned, this would be combined with the logging framework and they both
 * would work towards a bigger notification and even propagation mechanism..
 * 
 * 
 * No need of timestamp as we can get timestamp from the ObjectId, mongodb guarantees that it will have 
 * a valid timestamp for each objectId
 * 
 * Mani
 * 
 */
@Entity(value = "Notification", noClassnameStored = true)
public class DBNotification {
	@Id private ObjectId id;

	@Property private String userId;
	@Property private String userName;
	@Property private String message;
	@Property private Boolean isRead = false;
	@Property private NotificationLevel notificationLevel;
	@Property private NotificationType notificationType;
	@Property private Date notificationDate;
	@Property private String siteKey;
	@Property private String initiatedUserId;
	@Property private String initiatedUserName;
	@Property private String uhid;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public NotificationLevel getNotificationLevel() {
		return notificationLevel;
	}

	public void setNotificationLevel(NotificationLevel notificationLevel) {
		this.notificationLevel = notificationLevel;
	}

	public Date getNotificationDate() {
		return notificationDate;
	}

	public void setNotificationDate(Date notificationDate) {
		this.notificationDate = notificationDate;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public String getSiteKey() {
		return siteKey;
	}

	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}

	public String getInitiatedUserId() {
		return initiatedUserId;
	}

	public void setInitiatedUserId(String initiatedUserId) {
		this.initiatedUserId = initiatedUserId;
	}

	public String getInitiatedUserName() {
		return initiatedUserName;
	}

	public void setInitiatedUserName(String initiatedUserName) {
		this.initiatedUserName = initiatedUserName;
	}

	public String getUhid() {
		return uhid;
	}

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}
}
