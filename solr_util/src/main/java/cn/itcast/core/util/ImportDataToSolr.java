package cn.itcast.core.util;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ImportDataToSolr {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SolrTemplate solrTemplate;

    /*
    将库存表中 审核通过 的数据 导入到索引库中
     */
    public void importDataToSolr(){
        ItemQuery query = new ItemQuery();
        query.createCriteria().andStatusEqualTo("1");
        List<Item> items = itemDao.selectByExample(query);
        if(items!=null){
            //遍历的目的是 给pojo的类Item新增的specMap属性 赋值
            for (Item item : items) {
                String specJsonStr = item.getSpec();
                //将json字符串转换成java的map集合类型数据 添加到索引库
                Map<String,String> specMap = JSON.parseObject(specJsonStr, Map.class);
                item.setSpecMap(specMap);

            }
            //添加到solr索引库中
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }
    }

    public static void main(String[] args) {
        //获取 spring框架的顶级容器。。
        ApplicationContext context =
                //因为 要通过dao调用pojo，进行数据的处理和封装，所以必须要有*号。
                new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        //获取当前类的 实例化Bean对象
        ImportDataToSolr importDataToSolr =context.getBean(ImportDataToSolr.class);
        //类的对象变量名 调用本类中的方法 执行。
        importDataToSolr.importDataToSolr();
    }
}
