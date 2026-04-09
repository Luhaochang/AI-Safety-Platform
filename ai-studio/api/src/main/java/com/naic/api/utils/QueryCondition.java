package com.naic.api.utils;

/**
 * @author HuZhenSha
 * @since 2021/9/16
 */
public interface QueryCondition {

    String IGNORE = "Ignore";
    String EQUAL = "Eq";
    String NOT_EQUAL = "NotEq";
    String NOT_LIKE = "NotLike";
    String GE = "Ge";
    String LE = "Le";
    String GT = "Gt";
    String LT = "Lt";
    String NULL = "Null";
    String NOT_NULL = "NotNull";
    String LIKE = "Like";
    String IN = "In";

    String JSON = "Json";

    String ORDER_BY_ASC = "orderByAsc";

    String ORDER_BY_DESC = "orderByDesc";

}
