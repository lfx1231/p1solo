package cn.itcast.core.service;


import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map paramMap) {
        String categoryName = String.valueOf(paramMap.get("category"));
        Map<String, Object> resultMap = heightPageQuery(paramMap);
        //根据 消费者输入的关键字，自动匹配分类信息。
        List<String> categoryList = findCategoryByKeywords(paramMap);
        resultMap.put("categoryList",categoryList);
        //判断如果消费者 点击了分类，展示相应分类的 品牌和规格，如果没有点击或选择，则默认显示 分类的第一个类型。categoryList.get(0)
        if(categoryName!=null && !"".equals(categoryName)){
            Map<String, List> bsMap = findBrandSpecList(categoryName);
            resultMap.putAll(bsMap);
        }else{
            if(categoryList!=null && categoryList.size()>0){
                Map<String, List> brandSpecMap = findBrandSpecList(categoryList.get(0));
                resultMap.putAll(brandSpecMap);
            }
        }



        return resultMap;
    }
    public List<String> findCategoryByKeywords(Map paramMap){
        //增加 匹配分类并去掉重复
        String keywords = String.valueOf(paramMap.get("keywords"));
        if (keywords != null) {
            keywords = keywords.replaceAll(" ","");
        }
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        GroupPage<Item> items = solrTemplate.queryForGroupPage(query, Item.class);
        //需要把items中隐含的分类属性，赋值给List<String>
        List<String> resultList = new ArrayList<>();
        GroupResult<Item> item_category = items.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        if (content != null) {
            for (GroupEntry<Item> itemGroupEntry : content) {
                String groupValue = itemGroupEntry.getGroupValue();
                resultList.add(groupValue);
            }
        }


        return resultList;

    }

    public Map<String, Object> heightPageQuery(Map paramMap){
        //与之前的高级查询 的套路类似，看传过来的参数，先获取，并添加相应的查询条件
        String keywords =  String.valueOf(paramMap.get("keywords"));
        if (keywords != null) {
            keywords = keywords.replaceAll(" ","");
        }
        //排序的字段，比如价格，销量，好评度等
        String sortField =  String.valueOf(paramMap.get("sortField"));
        //排序的类型，升序还是降序 ASC DESC
        String sortType =  String.valueOf(paramMap.get("sort"));
        String category = String.valueOf(paramMap.get("category"));
        String brand = String.valueOf(paramMap.get("brand"));
        String spec = String.valueOf(paramMap.get("spec"));
        String price = String.valueOf(paramMap.get("price"));



        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //is是可以切分词 来查询，查询更加全面。
        if(pageNo == null || "".equals(pageNo)  || pageNo < 1){
            pageNo = 1;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        query.addCriteria(criteria);



        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        if(sortField!=null && sortType!=null &&!"".equals(sortField) &&!"".equals(sortType)){
            //升序
            if("ASC".equals(sortType)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            //降序
            if("DESC".equals(sortType)){
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }
        //过滤查询
        //根据选定的 分类查询
        if(category!=null && !"".equals(category)){
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(category);
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //根据选定的 品牌查询
        if(brand!=null && !"".equals(brand)){
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //根据选定的规格查询 {"网络":"4G","机身内存":"128G"}
        if(spec!=null && !"".equals(spec)){
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            if(specMap!=null && specMap.size()>0){
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    //field  item_spec_*  + key=网络，内存等，  value 4G，128G，等
                    Criteria filterCriteria = new Criteria("item_spec"+entry.getKey()).is(entry.getValue());
                    filterQuery.addCriteria(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
            }
        }
        //价格区间 过滤   0-500,500-1000,1000-1500,1500-2000.2000-3000,3000-*
        if(price!=null && !"".equals(price)){
            String[] split = price.split("-");
            if(split[0]!=null && !"0".equals(split[0])){
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(split[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }
            if(split[1]!=null && !"*".equals(split[1])){
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(split[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }



        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        List<Item> resultList = new ArrayList<>();
        if(highlighted!=null){
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
                Item item = itemHighlightEntry.getEntity();
                List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
                if (highlights!=null && highlights.size()>0){
                    List<String> snipplets = highlights.get(0).getSnipplets();
                    if(snipplets!=null && snipplets.size()>0){
                        String highTitle = snipplets.get(0);
                        if(highTitle!=null && !"".equals(highTitle)){
                            item.setTitle(highTitle);
                        }
                    }
                }
                resultList.add(item);
            }
        }

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("rows",items.getContent());
        resultMap.put("total",items.getTotalElements());
        resultMap.put("totalPages",items.getTotalPages());
        return resultMap;
    }
    //根据分类id，从redis中查询返回 品牌 和 规格 集合
    public Map<String,List> findBrandSpecList(String categoryName){
        if(categoryName!=null && !"".equals(categoryName)){
            //根据 分类名字 获取 模板id
            Long typeId = (Long)redisTemplate.boundHashOps(Constants.REDIS_CATEGORYLIST).get(categoryName);
            //根据模板id 获取品牌 集合
            List<Map> brandList =(List<Map>) redisTemplate.boundHashOps(Constants.REDIS_BRANDLIST).get(typeId);
            //根据模板id获取 规格 集合
            List<Map> specList =(List<Map>) redisTemplate.boundHashOps(Constants.REDIS_SPECLIST).get(typeId);
            Map<String, List> bsMap = new HashMap<>();
            bsMap.put("brandList",brandList);
            bsMap.put("specList",specList);

            return bsMap;
        }
        return null;
    }
    public Map<String,Object> findPage(Map paramMap){
        /*//与之前的高级查询 的套路类似，看传过来的参数，先获取，并添加相应的查询条件
        String keywords =  String.valueOf(paramMap.get("keywords"));
        //排序的字段，比如价格，销量，好评度等
        String sortField =  String.valueOf(paramMap.get("sortField"));
        //排序的类型，升序还是降序 ASC DESC
        String sortType =  String.valueOf(paramMap.get("sort"));

        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        //is是可以切分词 来查询，查询更加全面。
        if(pageNo == null || "".equals(pageNo)  || pageNo < 1){
            pageNo = 1;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        query.addCriteria(criteria);
        ScoredPage<Item> items = solrTemplate.queryForPage(query, Item.class);

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("rows",items.getContent());
        resultMap.put("total",items.getTotalElements());
        resultMap.put("totalPages",items.getTotalPages());*/
        return null;
    }

}
