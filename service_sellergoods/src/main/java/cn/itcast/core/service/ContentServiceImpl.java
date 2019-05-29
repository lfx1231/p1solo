package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;
    @Autowired
    private RedisTemplate redisTemplate;

    //高级查询。。
    @Override
    public PageResult search(Content content, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        if(content.getTitle()!=null){
            criteria.andTitleLike("%"+content.getTitle()+"%");
        }
        Page<Content> contentlist = (Page<Content>) contentDao.selectByExample(query);
        return new PageResult(contentlist.getTotal(),contentlist.getResult());
    }

    @Override
    public void add(Content content) {
        //增加redis
        contentDao.insertSelective(content);
        //根据该id删除，下次没有会自动存入到redis
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
    }

    @Override
    public Content findOne(Long id) {
       Content one = contentDao.selectByPrimaryKey(id);
        return one;
    }
    //修改
    @Override
    public void update(Content content) {
        //1. 根据广告主键id, 查询mysql数据库中广告对象(没有更新前的老对象)
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        //2. 根据老的广告对象中的分类id, 清除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(oldContent.getCategoryId());
        //3. 根据页面传入进来的新广告对象中的分类id, 清除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
        //4. 将页面传入的新的广告对象更新到mysql数据库中
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long[] ids) {
        if(ids != null){
            for (Long id : ids) {
                Content content = contentDao.selectByPrimaryKey(id);
                //先在redis中清空，该分类id在删除数据库的内容。
                redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        //增加redis 缓存
        ContentQuery query = new ContentQuery();
        query.setOrderByClause("sort_order desc");
        query.createCriteria().andCategoryIdEqualTo(categoryId);

        List<Content> list = contentDao.selectByExample(query);
        return list;
    }

    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {
        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).get(categoryId);
        if (contentList == null || contentList.size() == 0) {
            contentList = findByCategoryId(categoryId);
            redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).put(categoryId,contentList);
        }
        return contentList;
    }


}
