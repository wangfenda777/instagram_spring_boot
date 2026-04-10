package com.example.instagram.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Boolean hasMore;
    private Long lastId;

    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setHasMore((long) page * pageSize < total);
        return result;
    }
}
