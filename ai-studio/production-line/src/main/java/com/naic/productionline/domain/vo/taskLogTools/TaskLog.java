package com.naic.productionline.domain.vo.taskLogTools;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 任务日志对象VO task_log
 *
 * @author wangyunong
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value="任务日志对象")
@Document(indexName = "filebeat-8.15.0")
public class TaskLog {

    private static final long serialVersionUID = 1L;

    @JsonProperty("@timestamp")
    @ApiModelProperty(value = "time")
    @Field("@timestamp")
    private String timestamp;

    @Field("message")
    @ApiModelProperty(value = "message")
    private String message;
//
//    @Field("kubernetes.job.name")
//    @ApiModelProperty(value = "job name")
//    private String jobName;
}
