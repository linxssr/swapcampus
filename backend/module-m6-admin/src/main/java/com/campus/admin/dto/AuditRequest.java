package com.campus.admin.dto;

public class AuditRequest {
    private Long itemId;
    private Integer auditStatus;  // 1-通过 2-驳回

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Integer getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }
}