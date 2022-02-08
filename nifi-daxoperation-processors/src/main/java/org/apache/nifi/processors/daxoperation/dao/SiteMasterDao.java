package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.bo.SiteMaster;
import org.apache.nifi.processors.daxoperation.dbo.DBDietPlan;
import org.apache.nifi.processors.daxoperation.dbo.DBSiteMaster;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component("siteMasterDao")
public class SiteMasterDao extends BasicDAO<DBSiteMaster, ObjectId> {
	private static List<SiteMaster> siteMasterList = new ArrayList<SiteMaster>();

	public SiteMasterDao(MongoClient mongoClient) {
		super(DBSiteMaster.class, new MongoDBUtil(mongoClient).getDb());
	}

	public void loadAll() {
		siteMasterList.clear();
		for(DBSiteMaster dbSiteMaster : find().asList()) {
			siteMasterList.add(newSiteMaster(dbSiteMaster));
		}
	}
	
	public List<SiteMaster> getAll() {
		return siteMasterList;
	}
	
	public String findSiteKeyBySiteName(String siteName) {
		if (siteName!= null && siteName.trim().equalsIgnoreCase("CSV")) {
			return "CSV";
		} else {
			for(SiteMaster site: siteMasterList) {
				if(site != null && site.getSiteName() != null && site.getSiteName().equals(siteName)) {
					return site.getSiteKey();
				}
			}			
		}
		
		return null;
	}
	
	
	public String findSiteNameByLocationId(String locationId) {
		for(SiteMaster site: siteMasterList) {
			if(site != null && site.getLocationId() != null && site.getLocationId().equals(locationId)) {
				return site.getSiteName();
			}
		}
		
		return null;
	}
	
	public String findSiteNameByUhidPrefix(String uhid) {
		for(SiteMaster site: siteMasterList) {
			if(site != null && site.getUhidPrefix() != null && site.getUhidPrefix().equals(uhid)) {
				return site.getSiteName();
			}
		}
		
		return null;
	}
	
	public SiteMaster findSiteByUhidPrefix(String uhid) {
		for(SiteMaster site: siteMasterList) {
			if(site != null && site.getUhidPrefix() != null && site.getUhidPrefix().equals(uhid)) {
				return site;
			}
		}
		
		return null;
	}
	

	public SiteMaster findDBSiteBySiteName(String siteName) {
		for(SiteMaster site: siteMasterList) {
			if(site != null && site.getSiteName() != null && site.getSiteName().equals(siteName)) {
				return site;
			}
		}
		
		return null;
	}
	
	/*public List<SiteMaster> findDBSiteByEntityName(String entityName) {
		List<DBSiteMaster> siteList = createQuery().field("entityName").equal(entityName).asList();
		return siteList;
	}*/
	
	
	public SiteMaster findDBSiteBySiteKey(String siteKey) {
		for(SiteMaster site: siteMasterList) {
			if(site != null && site.getSiteKey() != null && site.getSiteKey().equals(siteKey)) {
				return site;
			}
		}
		
		return null;
	}
	
	public void dropCollection() {
		getCollection().drop();
	}
	
	public static DBSiteMaster newDBSiteMaster(SiteMaster siteMaster) {
		DBSiteMaster dbSiteMaster = new DBSiteMaster();
		if(siteMaster == null)
			return dbSiteMaster;
		
		dbSiteMaster.setDbSiteName(siteMaster.getSiteName());
		dbSiteMaster.setShortName(siteMaster.getShortName());
		dbSiteMaster.setDbSiteKey(siteMaster.getSiteKey());
		dbSiteMaster.setDbSiteType(siteMaster.getSiteType());
		dbSiteMaster.setSiteSupportId(siteMaster.getSiteSupportId());
		dbSiteMaster.setAddress(siteMaster.getAddress());
		dbSiteMaster.setCity(siteMaster.getCity());
		dbSiteMaster.setTestBlackList(siteMaster.getTestBlackList());
		dbSiteMaster.setHcuBlackList(siteMaster.getHcuBlackList());
		dbSiteMaster.setUhidPrefix(siteMaster.getUhidPrefix());
		dbSiteMaster.setLocationId(siteMaster.getLocationId());
		dbSiteMaster.setDebug(siteMaster.isDebug());
		dbSiteMaster.setSms(siteMaster.isSms());
		dbSiteMaster.setSiteDisplayName(siteMaster.getSiteDisplayName());
		dbSiteMaster.setEntityName(siteMaster.getEntityName());
		dbSiteMaster.setShortName(siteMaster.getShortName());
		
		return dbSiteMaster;
	}
	
	public static SiteMaster newSiteMaster(DBSiteMaster dbSiteMaster) {
		SiteMaster siteMaster = new SiteMaster();
		if(dbSiteMaster == null)
			return siteMaster;
		siteMaster.setId(dbSiteMaster.getId());
		siteMaster.setSiteName(dbSiteMaster.getDbSiteName());
		siteMaster.setShortName(dbSiteMaster.getShortName());
		siteMaster.setSiteKey(dbSiteMaster.getDbSiteKey());
		siteMaster.setSiteType(dbSiteMaster.getDbSiteType());
		siteMaster.setSiteSupportId(dbSiteMaster.getSiteSupportId());
		siteMaster.setAddress(dbSiteMaster.getAddress());
		siteMaster.setCity(dbSiteMaster.getCity());
		siteMaster.setTestBlackList(dbSiteMaster.getTestBlackList());
		siteMaster.setHcuBlackList(dbSiteMaster.getHcuBlackList());
		siteMaster.setUhidPrefix(dbSiteMaster.getUhidPrefix());
		siteMaster.setLocationId(dbSiteMaster.getLocationId());
		siteMaster.setDebug(dbSiteMaster.isDebug());
		siteMaster.setSms(dbSiteMaster.isSms());		
		siteMaster.setSiteDisplayName(dbSiteMaster.getSiteDisplayName());
		siteMaster.setEntityName(dbSiteMaster.getEntityName());
		siteMaster.setShortName(dbSiteMaster.getShortName());
		
		
		return siteMaster;
	}
}
