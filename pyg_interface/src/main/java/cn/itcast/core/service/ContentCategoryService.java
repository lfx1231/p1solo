package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentCategoryService {
    PageResult search(ContentCategory contentCategory, Integer page, Integer rows);

    void add(ContentCategory contentCategory);

    ContentCategory findOne(Long id);

    void update(ContentCategory contentCategory);

    void delete(Long[] ids);

    List<ContentCategory> findAll();
}
