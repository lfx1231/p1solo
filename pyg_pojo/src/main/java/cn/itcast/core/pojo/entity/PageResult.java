package cn.itcast.core.pojo.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {//因为要用dubbox，所以要实现Serializabel接口
    private Long total;//总记录数
    private List rows;//当前页结果集合
    //满仓构造。
    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
