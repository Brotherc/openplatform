package com.brotherc.documentcenter.model.entity.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BaseEntity {

    @Column(value = "create_by")
    private Long createBy;

    @Column(value = "create_time")
    private LocalDateTime createTime;

    @Column(value = "update_by")
    private Long updateBy;

    @Column(value = "update_time")
    private LocalDateTime updateTime;

    @Column(value = "is_del")
    private Integer isDel = 0;

}
