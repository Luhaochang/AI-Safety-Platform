package com.naic.productionline.domain.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("knowledge_document")
@ApiModel(value = "知识库文档")
public class KnowledgeDocumentPO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String knowledgeBaseId;
    private String name;
    private String size;
    private String status;
    private Integer chunks;
    private String createUser;
    private Date createTime;
}
