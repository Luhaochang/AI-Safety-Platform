package com.naic.api.api;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author HuZhenSha
 * @date 2021/3/29 14:53
 */
@ApiModel("DataPage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DataPage<T> {

    @ApiModelProperty("总数量")
    private long total;

    @ApiModelProperty("记录")
    private List<T> records;

    public static <T> DataPage<T> rest(IPage<T> page){
        return new DataPage<T>(page.getTotal(), page.getRecords());
    }

    public static <T> DataPage<T> rest(List<T> list,int pageNo,int pageSize) {
        DataPage<T> result = new DataPage<>();
        result.setTotal(list.size());
        int[] loc = PageUtil.transToStartEnd(pageNo-1, pageSize);
        result.setRecords(ListUtil.sub(list,loc[0],loc[1]));
        return result;
    }

}
