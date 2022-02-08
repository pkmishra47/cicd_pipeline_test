package org.apache.nifi.processors.daxoperation.models;

import java.util.List;
import java.util.Map;

public class TextractModel {
    public String blockType;
    public Object confidence;
    public String text;
    public String textType;
    public Object rowIndex;
    public Object columnIndex;
    public Object rowSpan;
    public Object columnSpan;
    public String id;
    public Map<String, Object> geometry;
    public List<Map<String, Object>> relationships;
    public Object entityTypes;
    public Object selectionStatus;
    public Object page;
}
