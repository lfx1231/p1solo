package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
@Service
public class ItemCatServiceImpl implements ItemCatService {
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //把分类的数据放到redis中，供前端搜索使用，如要搜索，先在前端web_manager先要进入
//        分类页面查一把，把数据缓存到redis中
        List<ItemCat> categorys = itemCatDao.selectByExample(null);
        if (categorys != null) {
            for (ItemCat itemCat : categorys) {
                //以分类名称作为key，以模板id作为value
                redisTemplate.boundHashOps(Constants.REDIS_CATEGORYLIST).put(itemCat.getName(),itemCat.getTypeId());
            }
        }

        List<ItemCat> list = new ArrayList<>();
        if(parentId!=null){
            ItemCatQuery query = new ItemCatQuery();
            query.createCriteria().andParentIdEqualTo(parentId);
            list = itemCatDao.selectByExample(query);
            return list;
        }
        return list;
    }

    @Override
    public ItemCat findOne(Long id) {
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(id);
        return itemCat;
    }

    @Override
    public List<ItemCat> findAll() {
        List<ItemCat> list = itemCatDao.selectByExample(null);
        return list;
    }
}
