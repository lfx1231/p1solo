package cn.itcast.core.test;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.service.BrandService;
import cn.itcast.core.service.BrandServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/applicationContext*.xml"})
public class Test01 {

    @Autowired
    BrandDao brandDao;

    @Test
    public void test(){

        Brand brand = brandDao.selectByPrimaryKey(1L);
        System.out.println(brand);
    }
    @Test
    public void test02(){
        //按照id降序 desc （升序asc，）拼接sql语句
        //并模糊查询 有关键字 华的品牌。
        BrandQuery query = new BrandQuery();
        BrandQuery.Criteria criteria = query.createCriteria();
        criteria.andIdBetween(3L,6L);
        query.setDistinct(true);
        query.setOrderByClause("id desc");
        List<Brand> brands = brandDao.selectByExample(query);
        System.out.println(brands);

    }

}
