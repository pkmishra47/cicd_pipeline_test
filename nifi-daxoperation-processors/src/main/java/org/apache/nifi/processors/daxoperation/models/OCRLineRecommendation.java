package org.apache.nifi.processors.daxoperation.models;

public class OCRLineRecommendation {
    private String id;
    private String match;
    private Integer score;
    private Integer index;
    private String itemname;
    private String recommendationrank;
    private Object variants;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatch() {
        return this.match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public Integer getScore() {
        return this.score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getItemname() {
        return this.itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public String getRecommendationrank() {
        return this.recommendationrank;
    }

    public void setRecommendationrank(String recommendationrank) {
        this.recommendationrank = recommendationrank;
    }

    public Object getVariants() {
        return this.variants;
    }

    public void setVariants(Object variants) {
        this.variants = variants;
    }
}
