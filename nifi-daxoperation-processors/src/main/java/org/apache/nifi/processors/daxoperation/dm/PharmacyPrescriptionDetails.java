package org.apache.nifi.processors.daxoperation.dm;

import java.util.List;

public class PharmacyPrescriptionDetails {
    private String OrderId;
    private String ShopId;
    private String DoctorName;
    private String OrderDate;
    private String UserId;
    private CustomerDetails CustomerDetails;
    private List<MedicineDetails> ItemDetails = null;

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public String getShopId() {
        return ShopId;
    }

    public void setShopId(String shopId) {
        ShopId = shopId;
    }

    public String getDoctorName() {
        return DoctorName;
    }

    public void setDoctorName(String doctorName) {
        DoctorName = doctorName;
    }

    public String getOrderDate() {
        return OrderDate;
    }

    public void setOrderDate(String orderDate) {
        OrderDate = orderDate;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public org.apache.nifi.processors.daxoperation.dm.CustomerDetails getCustomerDetails() {
        return CustomerDetails;
    }

    public void setCustomerDetails(org.apache.nifi.processors.daxoperation.dm.CustomerDetails customerDetails) {
        CustomerDetails = customerDetails;
    }

    public List<MedicineDetails> getItemDetails() {
        return ItemDetails;
    }

    public void setItemDetails(List<MedicineDetails> itemDetails) {
        ItemDetails = itemDetails;
    }
}
