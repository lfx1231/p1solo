package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    List<Brand> findAll();

    PageResult findPage(Integer page, Integer rows);

    void add(Brand brand);

    Brand findOne(Long id);

    void update(Brand brand);

    void del(Long[] ids);

    PageResult search(Brand brand, Integer page, Integer rows);

    List<Map> selectOptionList();
}
