package com.example.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private java.util.List<T> records;

    public static <T> PageResult<T> of(Long total, Long pageNum, Long pageSize, java.util.List<T> records) {
        PageResult<T> pr = new PageResult<>();
        pr.setTotal(total);
        pr.setPageNum(pageNum);
        pr.setPageSize(pageSize);
        pr.setRecords(records);
        return pr;
    }
}
