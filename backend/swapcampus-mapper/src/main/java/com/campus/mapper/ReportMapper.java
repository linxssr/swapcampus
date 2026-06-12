package com.campus.mapper;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    /**
     * 获取举报列表
     * @param handleStatus 处理状态：0-待处理 1-已处理（null表示全部）
     */
    @Select("<script>" +
            "SELECT r.*, i.title as item_title, u.username as report_user_name " +
            "FROM report r " +
            "LEFT JOIN item i ON r.item_id = i.item_id " +
            "LEFT JOIN user u ON r.report_user = u.user_id " +
            "<where>" +
            "<if test='handleStatus != null'>AND r.handle_status = #{handleStatus}</if>" +
            "</where>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    List<Map<String, Object>> selectReportList(@Param("handleStatus") Integer handleStatus);

    /**
     * 处理举报
     */
    @Update("UPDATE report SET handle_status = 1, handle_admin_id = #{adminId}, handle_result = #{result}, update_time = #{updateTime} WHERE report_id = #{reportId}")
    int handleReport(@Param("reportId") Long reportId,
                     @Param("adminId") Long adminId,
                     @Param("result") String result,
                     @Param("updateTime") LocalDateTime updateTime);
}