package com.campus.mapper;

import com.campus.model.entity.Admin;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;

@Mapper
public interface AdminMapper {

    /**
     * 根据管理员账号查询
     */
    @Select("SELECT * FROM admin WHERE admin_name = #{adminName}")
    Admin findByAdminName(String adminName);

    /**
     * 封禁/解封用户
     * @param userId 用户ID
     * @param status 0-封禁 1-正常
     */
    @Update("UPDATE user SET status = #{status} WHERE user_id = #{userId}")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 审核商品
     * @param itemId 商品ID
     * @param auditStatus 审核状态：1-通过 2-驳回
     * @param adminId 管理员ID
     * @param updateTime 更新时间
     * 暂时放在adminMapper
     */
    @Update("UPDATE item SET audit_status = #{auditStatus}, audit_admin_id = #{adminId}, update_time = #{updateTime} WHERE item_id = #{itemId}")
    int auditItem(@Param("itemId") Long itemId,
                  @Param("auditStatus") Integer auditStatus,
                  @Param("adminId") Long adminId,
                  @Param("updateTime") LocalDateTime updateTime);

    /**
     * 添加管理员
     */
    @Insert("INSERT INTO admin(admin_name, password, real_name, phone, status, create_time) " +
            "VALUES(#{adminName}, #{password}, #{realName}, #{phone}, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "adminId", keyColumn = "admin_id")
    int insertAdmin(Admin admin);
}