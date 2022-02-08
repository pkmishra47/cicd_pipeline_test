package org.apache.nifi.processors.daxoperation.dm;

public class MedicineDetails {
    private String ItemID;
    private String ItemName;
    private String Qty;
    private String Mou;
    private String Pack;
    private String Price;
    private Boolean Status;


    public String getItemID() {
        return ItemID;
    }

    public void setItemID(String itemID) {
        ItemID = itemID;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getQty() {
        return Qty;
    }

    public void setQty(String qty) {
        Qty = qty;
    }

    public String getMou() {
        return Mou;
    }

    public void setMou(String mou) {
        Mou = mou;
    }

    public String getPack() {
        return Pack;
    }

    public void setPack(String pack) {
        Pack = pack;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }
}
