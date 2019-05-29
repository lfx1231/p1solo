package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService,ServletContextAware {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao catDao;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    private ServletContext servletContext;


    //根据前端传过来的goodsId，把item的数据封装并返回到 页面进行展示,
    // 根据service_page项目下的WEB-INF下的item.ftl 模板文件，使用的变量名参考，进行封装。
    @Override
    public Map<String,Object> findGoodsData(Long goodsId){
        Map<String,Object> rootMap = new HashMap<>();
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(query);
        if(goods!=null){
            Long category1Id = goods.getCategory1Id();
            String itemCat1 = catDao.selectByPrimaryKey(category1Id).getName();
            String itemCat2 = catDao.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = catDao.selectByPrimaryKey(goods.getCategory3Id()).getName();
            rootMap.put("itemCat1",itemCat1);
            rootMap.put("itemCat2",itemCat2);
            rootMap.put("itemCat3",itemCat3);
        }
        rootMap.put("goods",goods);
        rootMap.put("goodsDesc",goodsDesc);
        rootMap.put("itemList",itemList);
        return rootMap;
    }
    @Override
    public void createStaticPage(Long goodsId,Map<String, Object> rootMap)throws Exception{
        Configuration conf = freeMarkerConfigurer.getConfiguration();
        Template template = conf.getTemplate("item.ftl");
        String path = goodsId+".html";
        //这个路径不能写死，需要是本项目发布到tomcat后的项目 根目录地址
        String realPath = servletContext.getRealPath(path);
        System.out.println("PageServiceImpl.createStaticPage"+"---->"+realPath);
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(realPath)),"utf-8");

        //生成
        template.process(rootMap,out);
        //关闭流
        out.close();

    }
    //实现 ServletContextAware 的接口，给本类的变量赋值。
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext=servletContext;
    }
}
