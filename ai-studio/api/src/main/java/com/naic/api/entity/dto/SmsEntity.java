package com.naic.api.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wangyunong
 * @since 2024-3-8
 */
@Data
@Accessors(chain = true)
public class SmsEntity {

    private String code;

}
