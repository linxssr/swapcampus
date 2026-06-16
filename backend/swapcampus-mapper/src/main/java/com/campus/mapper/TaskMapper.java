package com.campus.mapper;

import com.campus.model.entity.UserTask;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {

    @Select("SELECT task_code, task_name, description, reward, task_type, sort FROM task_config ORDER BY sort ASC")
    List<Map<String, Object>> listAllTaskConfigs();

    @Select("""
            SELECT tc.task_code, tc.task_name, tc.description, tc.reward, tc.task_type, tc.sort,
                   ut.status, ut.complete_time, ut.reward_time
            FROM task_config tc
            LEFT JOIN user_task ut
              ON tc.task_code = ut.task_code
             AND ut.user_id = #{userId}
             AND ut.task_date = #{today}
            WHERE tc.task_type = 1
            ORDER BY tc.sort ASC
            """)
    List<Map<String, Object>> listDailyTasksWithStatus(@Param("userId") Long userId,
                                                        @Param("today") LocalDate today);

    @Select("""
            SELECT tc.task_code, tc.task_name, tc.description, tc.reward, tc.task_type, tc.sort,
                   ut.status, ut.complete_time, ut.reward_time
            FROM task_config tc
            LEFT JOIN user_task ut
              ON tc.task_code = ut.task_code
             AND ut.user_id = #{userId}
            WHERE tc.task_type = 2
            ORDER BY tc.sort ASC
            """)
    List<Map<String, Object>> listAchievementsWithStatus(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*) FROM user_task
            WHERE user_id = #{userId} AND task_code = #{taskCode} AND task_date = #{today}
            """)
    int checkTaskDoneToday(@Param("userId") Long userId,
                           @Param("taskCode") String taskCode,
                           @Param("today") LocalDate today);

    @Select("""
            SELECT COUNT(*) FROM user_task
            WHERE user_id = #{userId} AND task_code = #{taskCode}
            """)
    int checkAchievementDone(@Param("userId") Long userId, @Param("taskCode") String taskCode);

    @Insert("""
            INSERT INTO user_task(user_id, task_code, task_date, status, complete_time)
            VALUES(#{userId}, #{taskCode}, #{taskDate}, 1, NOW())
            ON DUPLICATE KEY UPDATE status = IF(status = 0, 1, status), complete_time = IF(status = 0, NOW(), complete_time)
            """)
    int markTaskComplete(@Param("userId") Long userId,
                         @Param("taskCode") String taskCode,
                         @Param("taskDate") LocalDate taskDate);

    @Update("""
            UPDATE user_task SET status = 2, reward_time = NOW()
            WHERE user_id = #{userId} AND task_code = #{taskCode} AND task_date = #{taskDate} AND status = 1
            """)
    int claimReward(@Param("userId") Long userId,
                    @Param("taskCode") String taskCode,
                    @Param("taskDate") LocalDate taskDate);

    @Select("SELECT reward FROM task_config WHERE task_code = #{taskCode}")
    Integer getTaskReward(@Param("taskCode") String taskCode);

    @Select("SELECT task_type FROM task_config WHERE task_code = #{taskCode}")
    Integer getTaskType(@Param("taskCode") String taskCode);

    @Select("""
            SELECT COUNT(*) FROM sign_record WHERE user_id = #{userId}
            """)
    int countTotalSignDays(@Param("userId") Long userId);
}
