package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;
@Service
public class SolrServiceImpl implements SolrService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private ItemDao itemDao;
    //根据goodsid保存 item的库存信息。
    @Override
    public void addItemToSolr(Long[] ids) {
        if(ids!=null && ids.length>0){
            for (Long goodsId : ids) {
                ItemQuery query = new ItemQuery();
                query.createCriteria().andGoodsIdEqualTo(goodsId);
                List<Item> items = itemDao.selectByExample(query);
                if(items!=null){
                    for (Item item : items) {
                        String specJsonStr = item.getSpec();
                        Map<String,String> specMaps = JSON.parseObject(specJsonStr, Map.class);
                        item.setSpecMap(specMaps);
                    }
                }
                solrTemplate.saveBeans(items);
                solrTemplate.commit();
            }
        }
    }

    @Override
    public void deleteSolrByid(Long[] ids) {
        if(ids!=null){
            for (Long goodsId : ids) {
                SimpleQuery query = new SimpleQuery();

                Criteria criteria = new Criteria("item_goodsid").is(goodsId);
                query.addCriteria(criteria);
                solrTemplate.delete(query);
                solrTemplate.commit();

            }
        }

    }
}
