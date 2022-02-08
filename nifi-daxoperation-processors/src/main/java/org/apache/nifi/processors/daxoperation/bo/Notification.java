package org.apache.nifi.processors.daxoperation.bo;

import java.util.Date;

public class Notification {
    public String message;
    public String siteKey;
    public NotificationType notificationType;
    public NotificationLevel notificationLevel;
    public String user;
    public Date notificationDate;
    public String initiatedBy;
    public String affectedUserObjectId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationLevel getNotificationLevel() {
        return notificationLevel;
    }

    public void setNotificationLevel(NotificationLevel notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(Date notificationDate) {
        this.notificationDate = notificationDate;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getAffectedUserObjectId() {
        return affectedUserObjectId;
    }

    public void setAffectedUserObjectId(String affectedUserObjectId) {
        this.affectedUserObjectId = affectedUserObjectId;
    }
}
