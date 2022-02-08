package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


/**
 * Some rework is needed on this class for Medication detail,
 * This and prescription are overlapping with each other
 * @author Manigandan
 *
 */
@Entity(value = "SleepPattern", noClassnameStored = true)
public class DBSleepPattern  implements Comparable<DBSleepPattern>{
	@Id private ObjectId id;
	@Property private String sleepQuality;
	
	@Property private String note;
	@Property private String hrs;
	@Property private String min;
    @Property private Date Date;

	
		
		
		
		public ObjectId getId() {
			return id;
		}
		public void setId(ObjectId id) {
			this.id = id;
		}
		public void setsleepQuality(String sleepQuality) {
			this.sleepQuality = sleepQuality;
		}
		public String getsleepQuality() {
			return sleepQuality;
		}

		public void setDate(Date Date) {
			this.Date = Date;
		}
		public Date getDate() {
			return Date;
		}

		public void setnote(String note) {
			this.note = note;
		}
		public String getnote() {
			return note;
		}

		public void sethrs(String hrs) {
			this.hrs = hrs;
		}
		public String gethrs() {
			return hrs;
		}
		
		public void setmin(String min) {
			this.min = min;
		}
		public String getmin() {
			return min;
		}

		@Override
		public int compareTo(DBSleepPattern o) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}