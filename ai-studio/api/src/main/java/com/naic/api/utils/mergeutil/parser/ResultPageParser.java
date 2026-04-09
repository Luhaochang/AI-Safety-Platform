package com.naic.api.utils.mergeutil.parser;


import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description Result解析
 * @author fx
 * @date 2021/9/16
 */
@Component
public class ResultPageParser implements IMergeResultParser{
    @Override
    public List<?> parser(Object methodResult) {
        Result<?> result = (Result<?>)methodResult;
        DataPage<?> dataPage = (DataPage<?>) result.getData();
        return dataPage.getRecords();
    }
}
