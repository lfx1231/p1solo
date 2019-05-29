package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentCategoryDao;
import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.ad.ContentCategoryQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private ContentCategoryDao contentCategoryDao;
    @Override
    public PageResult search(ContentCategory contentCategory, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        //创建查询对象。
        ContentCategoryQuery query = new ContentCategoryQuery();
        ContentCategoryQuery.Criteria criteria = query.createCriteria();
        //判断高级查询有没有携带参数 封装在对象中
        if(contentCategory.getName()!=null){
            //有的话，把搜索的关键字放到sql语句的where条件之后。
            criteria.andNameLike("%"+contentCategory.getName()+"%");
        }
        Page<ContentCategory> categoryList =(Page<ContentCategory>) contentCategoryDao.selectByExample(query);
        return new PageResult(categoryList.getTotal(),categoryList.getResult());
    }

    //新增 广告分类。
    @Override
    public void add(ContentCategory contentCategory) {
        contentCategoryDao.insertSelective(contentCategory);
    }

    //修改前，先根据id返回查询内容。
    @Override
    public ContentCategory findOne(Long id) {
        ContentCategory category = contentCategoryDao.selectByPrimaryKey(id);
        return category;
    }
    //修改
    @Override
    public void update(ContentCategory contentCategory) {
        contentCategoryDao.updateByPrimaryKeySelective(contentCategory);
    }
    //删除。
    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                contentCategoryDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<ContentCategory> findAll() {
        List<ContentCategory> list = contentCategoryDao.selectByExample(null);
        return list;
    }


}
