package com.campus.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserTask {
    private Long id;
    private Long userId;
    private String taskCode;
    private LocalDate taskDate;
    private Integer status;
    private LocalDateTime completeTime;
    private LocalDateTime rewardTime;
}
