package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;

import java.util.List;

public interface ContentService {
    PageResult search(Content content, Integer page, Integer rows);

    void add(Content content);

    Content findOne(Long id);

    void update(Content content);

    void delete(Long[] ids);

    List<Content> findAll();

    List<Content> findByCategoryId(Long categoryId);

    List<Content> findByCategoryIdFromRedis(Long categoryId);
}
