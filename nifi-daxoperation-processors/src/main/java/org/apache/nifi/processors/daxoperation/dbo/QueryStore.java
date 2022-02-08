package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;
import org.mongodb.morphia.utils.IndexDirection;

@Entity(value = "QueryStoreDB", noClassnameStored = true)
public class QueryStore {

    @Id private ObjectId id;
    
    @Indexed(value = IndexDirection.ASC, name = "CampaignIndex", unique = true, dropDups = true)
    private String campaignName;            // Campaign name is unique
    @Property private String hospitalID;    // For which hospital we are creating this campaign   
	@Property private String campaignQuery; // The logic of selection
    @Version long version;
    
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getCampaignName() {
		return campaignName;
	}
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public long getVersion() {
		return version;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public String getHospitalID() {
		return hospitalID;
	}
	public void setHospitalID(String hospitalID) {
		this.hospitalID = hospitalID;
	}
	public String getCampaignQuery() {
		return campaignQuery;
	}
	public void setCampaignQuery(String campaignQuery) {
		this.campaignQuery = campaignQuery;
	}
}
