package com.campus.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    @Length(max = 100, message = "标题长度不能超过100字符")
    private String title;

    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal price;

    @NotNull(message = "请选择成色")
    @Min(value = 1, message = "成色值无效")
    @Max(value = 4, message = "成色值无效")
    private Integer quality;

    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    @NotBlank(message = "封面图不能为空")
    private String coverUrl;

    private List<String> imageUrls;
}
