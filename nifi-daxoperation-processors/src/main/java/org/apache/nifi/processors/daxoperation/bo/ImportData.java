package org.apache.nifi.processors.daxoperation.bo;

public class ImportData {
    private String siteApiKey;
    private String siteID;
    private String version;
    private String tableName;
    private String commandId;
    private String data;

    public String getSiteApiKey() {
        return siteApiKey;
    }

    public void setSiteApiKey(String siteApiKey) {
        this.siteApiKey = siteApiKey;
    }

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
