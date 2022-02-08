package org.apache.nifi.processors.daxoperation.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Constants {
    public static final String DB_CONNECTION_ERROR = "Unable to get DB connection";

    public static final String UNSUPPORTED_DATABASE = "DB Unsupported";

    public static final String UNINITIALIZED_FLOWFILE_ERROR_MESSAGE = "FlowFile not received";

    public static final String SUCSSESS_RELATIONSHIP = "SUCCESS";

    public static final String FAILURE_RELATIONSHIP = "FAILURE";

    public static final String INVALID_FLOWFILE_ATTRIBUTES_PROVIDED = "Invalid flowfile attributes provided";

    public static final String SITE_MASTER_MAP_NULL = "SiteMasterMap is null.";

    public static final String SITE_DETAILS_NULL = "SiteDetails is null.";

    public static final String SITE_NAME = "site_name";

    public static final String ENTITY_NAME = "entity_name";

    public static final String SITE_DETAILS = "site_details";

    public static final String CLUSTER_NAME = "cluster_name";

    public static final String SPECIAL_RESULT_OBJID = "special_result_obj_id";

    public static final String PATIENT_OBJID = "patient_obj_id";

    public static final String UHID = "uhid";

    public static final String CLUSTER_LOCID_MAP = "{\"Delhi\":[\"10701\",\"10702\"],\"Chennai\":[\"13001\",\"14101\",\"10107\",\"10109\",\"10108\",\"10110\",\"10341\",\"10101\",\"15104\",\"10332\",\"10321\",\"10331\",\"10111\",\"10103\",\"10301\",\"10102\",\"10311\",\"10113\",\"10105\",\"10104\",\"10302\"],\"Kolkata\":[\"10802\",\"10801\",\"10401\"],\"Nashik\":[\"10381\",\"12102\"],\"Mumbai\":[\"11001\",\"10552\",\"10551\"],\"Hyderabad\":[\"17002\",\"10901\",\"10209\",\"10361\",\"17001\",\"12101\",\"10362\",\"10202\",\"10201\",\"10204\",\"10203\"],\"Bangalore\":[\"10601\",\"10391\",\"10371\",\"10351\"]}";
}