package org.apache.nifi.processors.daxoperation.dbo;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBPharmacyBillItems {
	@Property private double mrp;
	@Property private double totalmrp;
	@Property private double discamt;
	@Property private double giftamt;
	@Property private String itemid;
	@Property private int saleqty;
	@Property private String item_name;
	
	 public double getMrp() {
	        return mrp;
	    }

	    public void setMrp(double mrp) {
	        this.mrp = mrp;
	    }

	    public double getTotalmrp() {
	        return totalmrp;
	    }

	    public void setTotalmrp(double totalmrp) {
	        this.totalmrp = totalmrp;
	    }

	    public double getDiscamt() {
	        return discamt;
	    }

	    public void setDiscamt(double discamt) {
	        this.discamt = discamt;
	    }

	    public double getGiftamt() {
	        return giftamt;
	    }

	    public void setGiftamt(double giftamt) {
	        this.giftamt = giftamt;
	    }

	    public String getItemid() {
	        return itemid;
	    }

	    public void setItemid(String itemid) {
	        this.itemid = itemid;
	    }

	    public int getSaleqty() {
	        return saleqty;
	    }

	    public void setSaleqty(int saleqty) {
	        this.saleqty = saleqty;
	    }

	    public String getItem_name() {
	        return item_name;
	    }

	    public void setItem_name(String item_name) {
	        this.item_name = item_name;
	    }
}
