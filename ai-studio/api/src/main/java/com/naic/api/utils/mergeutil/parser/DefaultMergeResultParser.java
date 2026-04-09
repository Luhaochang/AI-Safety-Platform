package com.naic.api.utils.mergeutil.parser;

import java.util.List;

/**
 * description 返回结果解析
 * @author fx
 * @date 2021/9/16
 */
public class DefaultMergeResultParser implements IMergeResultParser {
    @Override
    public List<?> parser(Object methodResult) {
        return (List<?>) methodResult;
    }
}
