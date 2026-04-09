package com.naic.api.utils.mergeutil.parser;

import java.util.List;

/**
 * description 将返回结果解析为list
 * @author fx
 * @date 2021/9/16
 */
public interface IMergeResultParser {
    /**
     * parser
     * @param methodResult
     * @return
     */
    List<?> parser(Object methodResult);
}
