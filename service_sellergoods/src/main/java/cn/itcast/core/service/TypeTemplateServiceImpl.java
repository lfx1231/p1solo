package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private SpecificationDao specDao;
    @Autowired
    private SpecificationOptionDao optionDao;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public PageResult findPage(TypeTemplate typeTemplate, Integer page, Integer rows) {
        //缓存模板中的数据到redis中，供前端搜索使用。
        List<TypeTemplate> typelist = typeTemplateDao.selectByExample(null);
        if (typelist != null) {
            for (TypeTemplate template : typelist) {
                String brandJsonStr = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandJsonStr, Map.class);
                redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).put(template.getId(),brandList);
            //获取规格和规格选项集合，放到redis中
//                String specJsonStr = template.getSpecIds();
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps(Constants.REDIS_SPECLIST).put(template.getId(),specList);
            }
        }


        //分页查询。
        PageHelper.startPage(page,rows);
        TypeTemplateQuery query = new TypeTemplateQuery();
        if(typeTemplate.getName()!=null){
            query.createCriteria().andNameLike("%"+typeTemplate.getName()+"%");
            Page<TypeTemplate> typeTemplates = (Page<TypeTemplate>)typeTemplateDao.selectByExample(query);
            return new PageResult(typeTemplates.getTotal(),typeTemplates.getResult());
        }
        Page<TypeTemplate> typeTemplates = (Page<TypeTemplate>)typeTemplateDao.selectByExample(null);

        return new PageResult(typeTemplates.getTotal(),typeTemplates.getResult());
    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public TypeTemplate findOne(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        return typeTemplate;
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }


    //根据模板id查询规格选项。
    @Override
    public List<Map> findBySpecList(Long id) {
//        List<Map> maps = new ArrayList<>();
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specJsonStr = typeTemplate.getSpecIds();
        List<Map> maps = JSON.parseArray(specJsonStr, Map.class);
        if(maps!=null){
            for (Map specmap : maps) {
                Long sid = Long.parseLong(String.valueOf(specmap.get("id")));
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(sid);
                List<SpecificationOption> options = optionDao.selectByExample(query);
                specmap.put("options",options);
            }
        }

        return maps;
    }
}
