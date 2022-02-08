package org.apache.nifi.processors.daxoperation.dbo;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "carepersonaocr", noClassnameStored = true)
public class DBCarePersonaOCR {
    @Id
    private ObjectId id;

    @Property
    private String Textract;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTextract() {
        return this.Textract;
    }

    public void setTextract(String Textract) {
        this.Textract = Textract;
    }
}