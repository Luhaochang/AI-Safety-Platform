package com.naic.api.utils.mergeutil.core;

import com.naic.api.utils.mergeutil.annonation.Merge;
import com.naic.api.utils.mergeutil.annonation.MergeField;
import com.naic.api.utils.mergeutil.annonation.MergeResult;
import com.naic.api.utils.mergeutil.annonation.Merges;
import com.naic.api.utils.mergeutil.parser.IMergeResultParser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 基于对象的合并
 *
 * @author HuZhenSha
 * @since 2021/11/16
 */
@Component
public class EntityMerge {

    public Object merge(ProceedingJoinPoint pjp, MergeResult annotation) throws Throwable {
        Object proceed = pjp.proceed();
        IMergeResultParser bean = BeanFactoryUtils.getBean(annotation.parser());
        List<?> targetResult = bean.parser(proceed);
        Class<?> clazz = targetResult.get(0).getClass();
        dealMerge(clazz, targetResult);
        return proceed;
    }

    private void dealMerge(Class<?> clazz, List<?> targetResult) {
        if (targetResult.size() == 0) {
            return;
        }
        Merges merges = clazz.getAnnotation(Merges.class);
        if (merges != null) {
            dealMerge(merges, clazz, targetResult);
        }
        Merges superMerges = clazz.getSuperclass().getAnnotation(Merges.class);
        if (superMerges != null) {
            dealMerge(superMerges, clazz, targetResult);
        }
    }

    private void dealMerge(Merges merges, Class<?> clazz, List<?> targetResult) {
        for (Merge merge : merges.value()) {
            try {
                Field paramField = null;
                try {
                    paramField = clazz.getDeclaredField(merge.key());
                } catch (NoSuchFieldException ignored) {
                }
                if (paramField == null) {
                    paramField = clazz.getSuperclass().getDeclaredField(merge.key());
                }
                paramField.setAccessible(true);
                mergeEntity(clazz, paramField, targetResult, merge);
            } catch (Exception ignored) {
            }
        }
    }


    @SuppressWarnings("unchecked")
    private void mergeEntity(Class<?> clazz, Field paramField, List<?> targetResult, Merge merge)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 将请求param放在集合一起请求
        Set<Object> params = new HashSet<>();
        for (Object o : targetResult) {
            Object param = paramField.get(o);
            if (param != null) {
                params.add(param);
            }
        }
        //保存调用结果
        Map<Object, Object> feignResult;
        Object bean = BeanFactoryUtils.getBean(merge.feign());
        Method method = merge.feign().getMethod(merge.method(), List.class);
        feignResult = (Map<Object, Object>) method.invoke(bean, new ArrayList<>(params));
        if (feignResult != null) {
            Field[] fields = clazz.getDeclaredFields();
            if (fields != null) {
                this.fieldSet(paramField, targetResult, merge, fields, feignResult);
            }
            Field[] fieldsSuper = clazz.getSuperclass().getDeclaredFields();
            if (fieldsSuper != null) {
                this.fieldSet(paramField, targetResult, merge, fieldsSuper, feignResult);
            }
        }
    }


    private void fieldSet(Field paramField, List<?> targetResult, Merge merge, Field[] fields, Map<Object, Object> feignResult) {
        // 遍历所有需要填充的field
        for (Field field : fields) {
            MergeField mergeField = field.getAnnotation(MergeField.class);
            if (mergeField != null) {
                field.setAccessible(true);
                targetResult.parallelStream().forEach((o -> {
                    try {
                        Object targetObj = feignResult.getOrDefault(paramField.get(o), null);
                        if (targetObj != null && targetObj.getClass().equals(mergeField.value())) {
                            if (mergeField.isEntity()) {
                                if (StringUtils.isEmpty(mergeField.key()) || mergeField.key().equals(merge.key())) {
                                    // 从entity中取值
                                    String targetFieldName;
                                    if (!StringUtils.isEmpty(mergeField.alias())) {
                                        targetFieldName = mergeField.alias();
                                    } else {
                                        targetFieldName = field.getName();
                                    }
                                    Field targetField = targetObj.getClass().getDeclaredField(targetFieldName);
                                    targetField.setAccessible(true);
                                    field.set(o, targetField.get(targetObj));

                                }
                            } else {
                                if (StringUtils.isEmpty(mergeField.key()) || mergeField.key().equals(merge.key())) {
                                    field.set(o, targetObj);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }));
            }
        }
    }
    
}
