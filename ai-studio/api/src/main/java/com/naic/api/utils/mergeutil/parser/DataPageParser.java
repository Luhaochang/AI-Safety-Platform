package com.naic.api.utils.mergeutil.parser;


import com.naic.api.api.DataPage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description dataPage解析
 * @author fx
 * @date 2021/9/16
 */
@Component
public class DataPageParser implements IMergeResultParser{
    @Override
    public List<?> parser(Object methodResult) {
        DataPage<?> result = (DataPage<?>)methodResult;
        return result.getRecords();
    }
}
