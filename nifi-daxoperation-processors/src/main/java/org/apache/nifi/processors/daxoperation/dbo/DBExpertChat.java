package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "ExpertChat", noClassnameStored = true)
public class DBExpertChat {
	@Id private ObjectId id;
  	@Property private String description;
  	@Property private String expertChatType;
  	@Embedded private List<DBExpertChatPrice> prices = new ArrayList<DBExpertChatPrice>();
  
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setExpertChatType(String expertChatType) {
		this.expertChatType = expertChatType;
	}
	public String getExpertChatType() {
		return expertChatType;
	}
	public void setPrices(List<DBExpertChatPrice> prices) {
		this.prices = prices;
	}
	public List<DBExpertChatPrice> getPrices() {
		return prices;
	}
}