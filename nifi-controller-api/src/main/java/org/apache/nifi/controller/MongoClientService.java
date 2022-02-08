package org.apache.nifi.controller;
import com.mongodb.MongoClient;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;

@Tags({"dax", "mongo", "database", "connection"})
@CapabilityDescription("Provides Mongo Database Client Service.")
public interface MongoClientService extends ControllerService {
    MongoClient getMongoClient();
}