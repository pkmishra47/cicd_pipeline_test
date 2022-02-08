package org.apache.nifi.processors.daxoperation.bo;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

public class QueryResult {
public String dbName;
public String collection;
public List<Map<string,string>> rows;
public long count;
}
