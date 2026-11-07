package com.smartlocal.entity;

/**
 * Status of a retailer's shop approval workflow.
 */
public enum ApprovalStatus {
    PENDING("Pending Verification"),
    APPROVED("Approved & Verified"),
    REJECTED("Rejected");

    private final String displayName;

    ApprovalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
