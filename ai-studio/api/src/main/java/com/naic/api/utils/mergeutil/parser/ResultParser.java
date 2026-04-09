package com.naic.api.utils.mergeutil.parser;

import com.naic.api.api.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * description Result解析
 * @author fx
 * @date 2021/9/16
 */
@Component
public class ResultParser implements IMergeResultParser{
    @Override
    public List<?> parser(Object methodResult) {
        Result<?> result = (Result<?>)methodResult;
        List<Object> list = new ArrayList<>();
        list.add(result.getData());
        return list;
    }
}
