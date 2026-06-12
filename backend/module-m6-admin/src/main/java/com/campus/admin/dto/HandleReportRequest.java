package com.campus.admin.dto;

public class HandleReportRequest {
    private Long reportId;
    private String result;  // 处理结果

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}