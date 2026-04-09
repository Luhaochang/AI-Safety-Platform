package com.naic.api.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author HuZhenSha
 * @since 2021/11/11
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ErrorLog implements Serializable {

    private String description;

    private LocalDateTime time;

    private String method;

    private String parameter;

    private String error;

}
