package com.naic.api.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.naic.api.constant.StringPool;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/9/16
 */
public class Condition {

    public static <T> QueryWrapper<T> getQueryWrapper(Map<String, Object> query, Class<T> clazz) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        //qw.setEntity(BeanUtil.newInstance(clazz));
        qw.setEntityClass(clazz);
        buildCondition(query, qw);
        return qw;
    }

    private static void buildCondition(Map<String, Object> query, QueryWrapper<?> qw) {
        if (!Func.isEmpty(query)) {
            query.remove("pageNo");
            query.remove("pageSize");
            query.forEach((k, v) -> {
                if (!Func.hasEmpty(k, v) && !k.endsWith(QueryCondition.IGNORE)) {
                    if (k.endsWith(QueryCondition.NOT_EQUAL)) {
                        String column = getColumn(k, QueryCondition.NOT_EQUAL);
                        qw.and(w -> w.ne(column, v).or().isNull(column));
                    } else if (k.endsWith(QueryCondition.EQUAL)) {
                        qw.eq(getColumn(k, QueryCondition.EQUAL), v);
                    } else if (k.endsWith(QueryCondition.NOT_LIKE)) {
                        qw.notLike(getColumn(k, QueryCondition.NOT_LIKE), v);
                    } else if (k.endsWith(QueryCondition.GE)) {
                        qw.ge(getColumn(k, QueryCondition.GE), v);
                    } else if (k.endsWith(QueryCondition.LE)) {
                        qw.le(getColumn(k, QueryCondition.LE), v);
                    } else if (k.endsWith(QueryCondition.GT)) {
                        qw.gt(getColumn(k, QueryCondition.GT), v);
                    } else if (k.endsWith(QueryCondition.LT)) {
                        qw.lt(getColumn(k, QueryCondition.LT), v);
                    } else if (k.endsWith(QueryCondition.NOT_NULL) && StringPool.TRUE.equals(v)) {
                        qw.isNotNull(getColumn(k, QueryCondition.NOT_NULL));
                    } else if (k.endsWith(QueryCondition.NULL) && StringPool.TRUE.equals(v)) {
                        qw.isNull(getColumn(k, QueryCondition.NULL));
                    } else if (k.endsWith(QueryCondition.LIKE)){
                        qw.like(getColumn(k, QueryCondition.LIKE), v);
                    } else if (k.endsWith(QueryCondition.IN)){
                        qw.like(getColumn(k, QueryCondition.IN), v);
                    }   else if (k.endsWith(QueryCondition.JSON)){
//                        qw.apply("JSON_CONTAINS(`{0}`, '{1}')", getColumn(k, QueryCondition.JSON), v);
                        StringBuilder builder=new StringBuilder("JSON_CONTAINS(`");
                        builder.append(getColumn(k, QueryCondition.JSON));
                        builder.append("`, '");
                        builder.append(v);
                        builder.append("')");
                        String sql =builder.toString();
                        qw.apply(sql);
                    }
                }
            });
            if (query.containsKey(QueryCondition.ORDER_BY_ASC)){
                Object object = query.get(QueryCondition.ORDER_BY_ASC);
                if (object.getClass().isArray()){
                    int len = Array.getLength(object);
                    String []items = new String[len];
                    for(int i = 0; i < len; i++) {
                        items[i] =  SpringUtils.humpToUnderline(Array.get(object, i).toString());

                    }
                    qw.orderByAsc(items);
                }else{
                    qw.orderByAsc(Arrays.stream(query.get(QueryCondition.ORDER_BY_ASC).toString().split(",")).map(SpringUtils::humpToUnderline).toArray(String[]::new));
                }
                //qw.orderByAsc(Arrays.stream(query.get(QueryCondition.ORDER_BY_ASC).toString().split(",")).map(StringUtils::humpToUnderline).toArray(String[]::new));
            }
            if (query.containsKey(QueryCondition.ORDER_BY_DESC)){
                Object object = query.get(QueryCondition.ORDER_BY_DESC);
                if (object.getClass().isArray()){
                    int len = Array.getLength(object);
                    String []items = new String[len];
                    for(int i = 0; i < len; i++) {
                        items[i] =  SpringUtils.humpToUnderline(Array.get(object, i).toString());

                    }
                    qw.orderByDesc(items);
                }else{
                    qw.orderByDesc(Arrays.stream(query.get(QueryCondition.ORDER_BY_DESC).toString().split(",")).map(SpringUtils::humpToUnderline).toArray(String[]::new));
                }
                //qw.orderByDesc(Arrays.stream(query.get(QueryCondition.ORDER_BY_DESC).toString().split(",")).map(StringUtils::humpToUnderline).toArray(String[]::new));
            }
        }
    }

    private static String getColumn(String column, String keyword) {
        return SpringUtils.humpToUnderline(SpringUtils.removeSuffix(column, keyword));
    }


}
