package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;
    @Override
    public List<Brand> findAll() {
        List<Brand> brands = brandDao.selectByExample(null);
        return brands;
    }

    @Override
    public PageResult findPage(Integer page, Integer rows) {
        PageHelper.startPage(page, rows);
        //查询并返回结果
        Page<Brand> brandList = (Page<Brand>)brandDao.selectByExample(null);
        //从分页助手集合对象中提取我们需要的数据, 封装成PageResult对象返回
        return new PageResult(brandList.getTotal(), brandList.getResult());
    }

    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);

    }

    @Override
    public Brand findOne(Long id) {
        Brand brand = brandDao.selectByPrimaryKey(id);
        return brand;
    }

    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public void del(Long[] ids) {
        if(ids!=null){
            for (long id : ids) {

                brandDao.deleteByPrimaryKey(id);
            }


        }
    }

    @Override
    public PageResult search(Brand brand, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        BrandQuery query = new BrandQuery();
        BrandQuery.Criteria criteria = query.createCriteria();
        if(brand!=null){
            if(brand.getName()!=null && !"".equals(brand.getName())){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if(brand.getFirstChar()!=null && !"".equals(brand.getFirstChar())){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        Page<Brand> brands = (Page<Brand>)brandDao.selectByExample(query);
        return new PageResult(brands.getTotal(),brands.getResult());
    }
        //要返回的格式：{"id":11,"text":"诺基亚"},{"id":12,"text":"锤子"}
    @Override
    public List<Map> selectOptionList() {
        //需要自定义dao的的语句查询方法，和自定义返回数据  的格式。
        List<Map> maps = brandDao.selectOptionList();

        return maps;
    }


}
