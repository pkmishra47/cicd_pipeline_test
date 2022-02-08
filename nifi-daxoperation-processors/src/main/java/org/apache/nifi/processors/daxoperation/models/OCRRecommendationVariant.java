package org.apache.nifi.processors.daxoperation.models;

public class OCRRecommendationVariant {
    private String id;
    private String itemname;
    private Integer variantRank;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemname() {
        return this.itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public Integer getVariantRank() {
        return this.variantRank;
    }

    public void setVariantRank(Integer variantRank) {
        this.variantRank = variantRank;
    }
}
