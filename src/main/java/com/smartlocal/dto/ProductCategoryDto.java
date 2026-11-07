package com.smartlocal.dto;

/**
 * DTO representing a product category with display icons and counts.
 */
public class ProductCategoryDto {
    private String id;
    private String name;
    private String iconClass;
    private int itemCount;

    public ProductCategoryDto() {
    }

    public ProductCategoryDto(String id, String name, String iconClass, int itemCount) {
        this.id = id;
        this.name = name;
        this.iconClass = iconClass;
        this.itemCount = itemCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
