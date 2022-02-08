package org.apache.nifi.processors.daxoperation.models;

public class OCRLineCoordinates {
    private Double width;
    private Double height;
    private Double left;
    private Double top;

    public Double getWidth() {
        return this.width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return this.height;
    }
    
    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getLeft() {
        return this.left;
    }
    
    public void setLeft(Double left) {
        this.left = left;
    }

    public Double getTop() {
        return this.top;
    }
    
    public void setTop(Double top) {
        this.top = top;
    }
}