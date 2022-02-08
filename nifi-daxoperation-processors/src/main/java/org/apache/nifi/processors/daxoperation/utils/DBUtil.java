package org.apache.nifi.processors.daxoperation.utils;

//import com.healthhiway.businessobject.Address;
//import com.healthhiway.businessobject.Attachment;
//import com.healthhiway.dbo.DBAddress;
//import com.healthhiway.dbo.DBAttachement;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import com.azure.storage.file.share.models.ShareStorageException;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.commons.codec.binary.Base64;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.dbo.DBAttachement;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.bson.types.ObjectId;
import org.slf4j.event.Level;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class DBUtil {
    private LogUtil logUtil = null;
    public static String filesBucket = "HHAppData.Files";
    public static GridFS gridStore;
    private Map<String, Object> logMetaData;
    private String azureConnStr;
    private String azureFileShare;
    private String azureBaseDirName;

    public DBUtil(MongoClient mongoClient) {
        gridStore = new GridFS(new MongoDBUtil(mongoClient).getDb().getDB(), filesBucket);
    }

    public DBUtil(MongoClient mongoClient, String azureConnStr, String shareName, String baseName, Map<String, Object> logMetaData) {
        gridStore = new GridFS(new MongoDBUtil(mongoClient).getDb().getDB(), filesBucket);
        this.azureConnStr = azureConnStr;
        this.azureFileShare = shareName;
        this.azureBaseDirName = baseName;
        this.logMetaData = logMetaData;
    }

    public DBUtil(MongoClient mongoClient, Map<String, Object> logMetaData) {
        gridStore = new GridFS(new MongoDBUtil(mongoClient).getDb().getDB(), filesBucket);
        this.logMetaData = logMetaData;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

//	public static void executeJS(String cmd) {
//		Runtime run = Runtime.getRuntime();
//		Process pr = null;
//		try {
//			pr = run.exec("mongo HHAppData js/" + cmd);
//		} catch (IOException e) {
//			log.error("Exception while running JS Commd: " + cmd, e);
//		}
//		try {
//			pr.waitFor();
//		} catch (InterruptedException e) {
//			log.error("Exception while running JS Commd: " + cmd, e);
//		}
//		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//		String line = "";
//		try {
//			while ((line=buf.readLine())!=null) {
//				System.out.println(line);
//			}
//		} catch (IOException e) {
//			log.error("Exception while running JS Commd: " + cmd, e);
//		}
//	}

//	public static Attachment findAttachedFile(String fileId) {
//		Attachment attachment = new Attachment();
//		try {
//			if (ObjectId.isValid(fileId)) {
//				log.info("PHR | DBUtil | findAttachedFile | fileId is valid ");
//				GridFSDBFile imageForOutput = gridStore.findOne(new ObjectId(fileId));
//				attachment.setMimeType(imageForOutput.getContentType());
//				byte[] value = null;
//				if(imageForOutput.getLength() != 0) {
//					value = gridFileToByteArray(imageForOutput);
//				}
//				attachment.setContent(value);
//			}
//		}
//		catch(Exception e) {
//			log.error("PHR | DBUtil | findAttachedFile | Error while finding attached File" , e);
//		}
//		return attachment;
//	}

    public String deleteFile(String fileId) {
        String result = "failure";
        try {
            if (ObjectId.isValid(fileId)) {
                this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "PHR | DBUtil | deleteFile | fileId is valid ", Level.INFO, null);
                gridStore.remove(new ObjectId(fileId));
                result = "success";
            }
        } catch (Exception e) {
            this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
        return result;
    }


    public static void backupDB() {
    }

    public static byte[] LoadImage(String filePath) throws Exception {
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] buffer = new byte[size];

        FileInputStream in = new FileInputStream(file);
        in.read(buffer);
        in.close();
        return buffer;
    }

//	public static DBAddress newDBAddress(Address address) {
//		DBAddress dbAddress = new DBAddress();
//
//		if (address == null)
//			return dbAddress;
//
//		dbAddress.setTag(address.tag);
//		dbAddress.setDoorNumber(address.doorNumber);
//		dbAddress.setHouseName(address.buildingName);
//		dbAddress.setStreetName_1(address.streetName_1);
//		dbAddress.setStreetName_2(address.streetName_2);
//		dbAddress.setCityName(address.cityName);
//		dbAddress.setStateName(address.stateName);
//		dbAddress.setCountryName(address.countryName);
//		dbAddress.setPinCode(address.pinCode);
//		return dbAddress;
//	}

    public static byte[] serializeObj(Object obj) throws IOException {
        ByteArrayOutputStream baOStream = new ByteArrayOutputStream();
        ObjectOutputStream objOStream = new ObjectOutputStream(baOStream);

        objOStream.writeObject(obj);
        objOStream.flush();
        objOStream.close();
        return baOStream.toByteArray();
    }

    public static byte[] gridFileToByteArray(GridFSDBFile dbFile) throws Exception {
        byte[] data = new byte[0];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        dbFile.writeTo(bout);
        data = bout.toByteArray();
        return data;
    }

//	public static Address newAddress(DBAddress dbAddress) {
//		Address address = new Address();
//
//		if (dbAddress == null)// Temp hack
//			return address;
//
//		address.setId(dbAddress.getId().toString());
//		address.tag = dbAddress.getTag();
//		address.doorNumber = dbAddress.getDoorNumber();
//		address.buildingName = dbAddress.getHouseName();
//		address.streetName_1 = dbAddress.getStreetName_1();
//		address.streetName_2 = dbAddress.getStreetName_2();
//		address.cityName = dbAddress.getCityName();
//		address.stateName = dbAddress.getStateName();
//		address.countryName = dbAddress.getCountryName();
//		address.pinCode = dbAddress.getPinCode();
//		return address;
//	}


    public static List<Attachment> readDBAttachement(List<DBAttachement> dbAttachementList) {
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        for (DBAttachement dbAttachement : dbAttachementList) {
            Attachment attach = readDBAttachement(dbAttachement);
            attachmentList.add(attach);
        }

        return attachmentList;
    }

    public static List<DBAttachement> writeDBAttachement(List<Attachment> attachemntList) {

        List<DBAttachement> dbAttachementList = new ArrayList<DBAttachement>();

        for (Attachment attachement : attachemntList) {
            DBAttachement dbAttachement = writeDBAttachement(attachement);
            dbAttachementList.add(dbAttachement);
        }
        return dbAttachementList;
    }


    // old one
//	public static DBAttachement writeDBAttachement(Attachment attachment) {
//
////		System.out.println("attachment obj "+GsonUtil.getGson().toJson(attachment));
//		DBAttachement dbAttachement = new DBAttachement();
//		if ( attachment.getId() == null){
//			ByteBuffer value = ByteBuffer.wrap(attachment.getContent().getBytes()) ;
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			out.write(value.array(), value.arrayOffset() + value.position(), value.remaining());
//			GridFSInputFile localFile = gridStore.createFile(out.toByteArray());
//			localFile.setContentType(attachment.getMimeType());
//			localFile.save();
//			dbAttachement.setFileAttached(localFile.getId());
//			dbAttachement.setDateCreated(new Date());
//		} else {
//			dbAttachement.setFileAttached(attachment.getId());
//			dbAttachement.setDateCreated(new Date(attachment.getDateCreated().getTime()));
//		}
//
//		dbAttachement.setFileName(attachment.getFileName());
//		dbAttachement.setMimeType(attachment.getMimeType());
//		return dbAttachement;
//	}

    public static Attachment readDBAttachement(DBAttachement dbAttachement) {
        Attachment attachment = new Attachment();
        attachment.setFileName(dbAttachement.getFileName());
        attachment.setMimeType(dbAttachement.getMimeType());
        attachment.setId(dbAttachement.getId());
        attachment.setDateCreated(dbAttachement.getDateCreated());
        return attachment;
    }

//	public static List<com.healthhiway.businessobject.Attachment> readDBAttachement(List<DBAttachement> dbAttachementList) {
//		List<Attachment> attachmentList = new ArrayList<Attachment>();
//		for (DBAttachement dbAttachement : dbAttachementList) {
//			Attachment attach = readDBAttachement(dbAttachement);
//			attachmentList.add(attach);
//		}
//
//		return attachmentList;
//	}

//	public static List<DBAttachement> writeDBAttachement(List<com.healthhiway.businessobject.Attachment> attachemntList) {
//
//		List<DBAttachement> dbAttachementList = new ArrayList<DBAttachement>();
//
//		for(com.healthhiway.businessobject.Attachment attachement : attachemntList) {
//			DBAttachement dbAttachement = writeDBAttachement(attachement);
//			dbAttachementList.add(dbAttachement);
//		}
//		return dbAttachementList;
//	}


    public static DBAttachement writeDBAttachement(Attachment attachment) {

//		System.out.println("attachment obj "+GsonUtil.getGson().toJson(attachment));
        DBAttachement dbAttachement = new DBAttachement();
        if (attachment.getId() == null) {
//            ByteBuffer value = ByteBuffer.wrap(attachment.getContent().getBytes());
            ByteBuffer value = ByteBuffer.wrap(attachment.getContent());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(value.array(), value.arrayOffset() + value.position(), value.remaining());
            GridFSInputFile localFile = gridStore.createFile(out.toByteArray());
            localFile.setContentType(attachment.getMimeType());
            localFile.save();
            dbAttachement.setFileAttached(localFile.getId());
            dbAttachement.setDateCreated(new Date());
        } else {
            dbAttachement.setFileAttached(attachment.getId());
            dbAttachement.setDateCreated(new Date(attachment.getDateCreated().getTime()));
        }

        dbAttachement.setFileName(attachment.getFileName());
        dbAttachement.setMimeType(attachment.getMimeType());
        return dbAttachement;
    }

    public DBAttachement writeDBAttachment(String dirName, Attachment attachment) {
        if (attachment.getId() == null || attachment.getId().toString().trim().length() == 0) {
            DBAttachement dbAttachement = new DBAttachement();
            ByteBuffer value = ByteBuffer.wrap(attachment.getContent());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(value.array(), value.arrayOffset() + value.position(), value.remaining());

            // save in Azure storage
            String savedLoc = uploadFile(dirName, attachment.getFileName(), out.toByteArray());
            dbAttachement.setAzurePath(savedLoc);
            if (savedLoc == null) {
                GridFSInputFile localFile = gridStore.createFile(out.toByteArray());
                localFile.setContentType(attachment.getMimeType());
                localFile.save();
                dbAttachement.setFileAttached(new ObjectId(localFile.getId().toString()));
                this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "uhid: " + dirName + " - Attachment Uploaded successfully to Mongo fileStorage with attachment id " + dbAttachement.getFileAttached().toString(), Level.INFO, null);
            }
            dbAttachement.setDateCreated(new Date());
            dbAttachement.setFileName(attachment.getFileName());
            dbAttachement.setMimeType(attachment.getMimeType());

            return dbAttachement;
        }

        return null;
    }

    private String uploadFile(String dirName, String fileName, byte[] data) {
        ShareFileClient fileClient = null;
        ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(this.azureConnStr)
                .shareName(this.azureFileShare).resourcePath(this.azureBaseDirName).buildDirectoryClient();
        ShareDirectoryClient subDirClient = createSubDirectory(dirClient, dirName);
        if (subDirClient != null) {
            fileClient = subDirClient.getFileClient(fileName);
        } else {
            String finalPath = this.azureBaseDirName + "/" + dirName;
            dirClient = new ShareFileClientBuilder().connectionString(this.azureConnStr)
                    .shareName(this.azureFileShare).resourcePath(finalPath).buildDirectoryClient();
            fileClient = dirClient.getFileClient(fileName);
        }
        fileClient.create(data.length);
        fileClient.upload(new ByteArrayInputStream(data), data.length);
        String filePath = dirName + "/" + fileName;
        this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Uploaded successfully to Azure at -> " + filePath, Level.INFO, null);
        return filePath;
    }

    private ShareDirectoryClient createSubDirectory(ShareDirectoryClient dirClient, String dirName) {
        try {
            ShareDirectoryClient subDirClient = dirClient.createSubdirectory(dirName);
            return subDirClient;
        } catch (ShareStorageException e) {
            this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "subDirectory already exists... subdirectory creation failed...", Level.WARN, null);
            return null;
        }
    }

    public byte[] readFile(String dirName, String fileName) {
        try {
            ShareFileClient fileClient = null;
            ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(this.azureConnStr)
                    .shareName(this.azureFileShare).resourcePath(this.azureBaseDirName).buildDirectoryClient();
            String finalPath = this.azureBaseDirName + "/" + dirName;
            String filePath = fileName;
            dirClient = new ShareFileClientBuilder().connectionString(this.azureConnStr).shareName(this.azureFileShare)
                    .resourcePath(finalPath).buildDirectoryClient();
            fileClient = dirClient.getFileClient(fileName);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            fileClient.download(bout);
            this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "downloaded successfully from azure", Level.WARN, null);
            return bout.toByteArray();
        } catch (Exception e) {
            this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.WARN, null);
            return null;
        }
    }

    public void deleteAzureFile(String dirName, String fileName) {
        try {
            String finalPath = this.azureBaseDirName + "/" + dirName;
            ShareDirectoryClient dirClient = new ShareFileClientBuilder().connectionString(this.azureConnStr).shareName(this.azureFileShare)
                    .resourcePath(finalPath).buildDirectoryClient();
            dirClient.deleteFile(fileName);
            this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "deleted file from azure storage - filename: " + fileName, Level.INFO, null);
        } catch (Exception e) {
            this.getLogUtil().logMessage(this.logMetaData, LogStatus.SUCCESS, LogType.GENERAL, Utility.stringifyException(e), Level.WARN, null);
        }
    }

//	public static Attachment readDBAttachement(DBAttachement dbAttachement) {
//		Attachment attachment = new Attachment();
//		try {
//			attachment.setFileName(dbAttachement.getFileName());
//			attachment.setMimeType(dbAttachement.getMimeType());
//			attachment.setId(String.valueOf(dbAttachement.getFileAttached()));
//			attachment.setDateCreated(dbAttachement.getDateCreated().getTime());
//		}
//		catch(Exception e) {
//			log.error("PHR | DBUtil | readDBAttachement | Error ",e); // Temporary hack , to handle this graceful
//		}
//		return attachment;
//	}
}