package com.naic.api.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huluming
 */
public class PageUtils {

    private static int firstPageNo = 0;

    public static int[] transToStartEnd(int pageNo, int pageSize) {
        int start = getStart(pageNo, pageSize);
        return new int[]{start, getEndByStart(start, pageSize)};
    }

    public static int getStart(int pageNo, int pageSize) {
        if (pageNo < firstPageNo) {
            pageNo = firstPageNo;
        }

        if (pageSize < 1) {
            pageSize = 0;
        }

        return (pageNo - firstPageNo) * pageSize;
    }

    private static int getEndByStart(int start, int pageSize) {
        if (pageSize < 1) {
            pageSize = 0;
        }

        return start + pageSize;
    }

    public static <T> List<T> sub(List<T> list, int start, int end) {
        return sub(list, start, end, 1);
    }

    public static <T> List<T> sub(List<T> list, int start, int end, int step) {
        if (list == null) {
            return null;
        } else if (list.isEmpty()) {
            return new ArrayList(0);
        } else {
            int size = list.size();
            if (start < 0) {
                start += size;
            }

            if (end < 0) {
                end += size;
            }

            if (start == size) {
                return new ArrayList(0);
            } else {
                if (start > end) {
                    int tmp = start;
                    start = end;
                    end = tmp;
                }

                if (end > size) {
                    if (start >= size) {
                        return new ArrayList(0);
                    }

                    end = size;
                }

                if (step < 1) {
                    step = 1;
                }

                List<T> result = new ArrayList();

                for(int i = start; i < end; i += step) {
                    result.add(list.get(i));
                }

                return result;
            }
        }
    }

}
