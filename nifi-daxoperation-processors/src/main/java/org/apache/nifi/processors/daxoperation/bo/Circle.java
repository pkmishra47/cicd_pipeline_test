package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public class Circle {
    private ObjectId id;
    private String name;
    private String ownerId;
    private String category;
    private List<String> userList;
    private Map<Resources, List<AccessLevel>> permissions;

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getUserList() {
        return this.userList;
    }

    public void setUserList(List<String> userlist) {
        this.userList = userlist;
    }

    public Map<Resources, List<AccessLevel>> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(Map<Resources, List<AccessLevel>> permissions) {
        this.permissions = permissions;
    }
}
