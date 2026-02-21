package com.nuliyang.common.po;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nuliyang.common.config.CustomTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

@Data
@TableName("resource_embedding")
public class EmbeddingPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务资源ID，比如视频ID、文档ID */
    private String resourceId;

    /**
     * pgvector 向量
     * 形如：[0.0123, -0.998, 0.441, ...]
     */
    @TableField(
            typeHandler = CustomTypeHandler.class,
            jdbcType = JdbcType.OTHER
    )
    private float[] embedding;
}
