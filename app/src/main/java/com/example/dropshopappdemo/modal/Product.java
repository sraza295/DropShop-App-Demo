package com.example.dropshopappdemo.modal;

public class Product
{
    String productId;
    String productDesc;
    int productCode;
    int mrp;
    String customerId;
    String brandName;
    String brandCode;
    int expiry;

    public Product() {
    }

    public Product(String productId, String productDesc, int productCode, int mrp, String customerId, String brandName, String brandCode, int expiry) {
        this.productId = productId;
        this.productDesc = productDesc;
        this.productCode = productCode;
        this.mrp = mrp;
        this.customerId = customerId;
        this.brandName = brandName;
        this.brandCode = brandCode;
        this.expiry = expiry;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public int getProductCode() {
        return productCode;
    }

    public void setProductCode(int productCode) {
        this.productCode = productCode;
    }

    public int getMrp() {
        return mrp;
    }

    public void setMrp(int mrp) {
        this.mrp = mrp;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public int getExpiry() {
        return expiry;
    }

    public void setExpiry(int expiry) {
        this.expiry = expiry;
    }
}
