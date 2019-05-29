package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTempDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQTopic topicPageAndSolr;
    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;


    //依照 数据库item表的字段进行设置。每一条item信息为第三方商家上架产品(spu)的sku 每一个库存信息
    @Override
    public void add(GoodsEntity goodsEntity) {
        goodsEntity.getGoods().setAuditStatus("0");
        if(goodsEntity.getGoods().getPrice()==null){
            goodsEntity.getGoods().setPrice(new BigDecimal(999999));
        }
        goodsDao.insertSelective(goodsEntity.getGoods());


        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        goodsDescDao.insertSelective(goodsEntity.getGoodsDesc());
        saveItemList(goodsEntity);

    }
    public void saveItemList(GoodsEntity goodsEntity){
        //设置规格
        if("1".equals(goodsEntity.getGoods().getIsEnableSpec())){
            if(goodsEntity.getItemList()!=null){
                for (Item item : goodsEntity.getItemList()) {
                    String title = goodsEntity.getGoods().getGoodsName();
                    String specJsonStr = item.getSpec();
                    Map<String,String> specMap = JSON.parseObject(specJsonStr, Map.class);
                    if(specMap!=null){
                        Collection<String> values = specMap.values();
                        if(values!=null){
                            for (String specValue : values) {
                                title+=""+specValue;
                            }
                        }
                    }
                    item =setItemInfo(item,goodsEntity);
                    item.setTitle(title);

                    itemDao.insertSelective(item);
                }
            }else {
                Item item = new Item();
                item.setTitle(goodsEntity.getGoods().getGoodsName());
                item.setPrice(new BigDecimal(9999999));
                item.setSpec("{ }");
                item.setNum(0);
                item.setIsDefault("1");
                itemDao.insertSelective(item);
            }
        }
    }

    private Item setItemInfo(Item item,GoodsEntity goodsEntity) {

        //设置分类id：通过商品信息中的第三个分类，查询在分类总表中的 分类id
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());
        item.setCategoryid(itemCat.getId());
        //设置分类名称： 分类总表中获取分类中文名称
        item.setCategory(itemCat.getName());
        //设置第三方商家id：获取第三方商家 的username  id  sellerId
        item.setSellerId(goodsEntity.getGoods().getSellerId());
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        //设置第三方卖家名称：
        item.setSeller(seller.getName());
        //设置品牌信息：通过商品信息获取，品牌id，再查询返回一个品牌对象，get品牌名称。
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //设置审核状态：
        item.setStatus("0");
        //设置创建时间：
        item.setCreateTime(new Date());
        //设置商品id
        item.setGoodsId(goodsEntity.getGoods().getId());
        //设置更新时间
        item.setUpdateTime(new Date());
        //设置示例图片信息
        String imgJsonStr = goodsEntity.getGoodsDesc().getItemImages();
        List<Map> imgList = JSON.parseArray(imgJsonStr, Map.class);
        if(imgList!=null && imgList.size()>0){
            item.setImage(String.valueOf(imgList.get(0).get("url")));
        }
        return item;
    }

    //search 查询所有第三方商品信息。
    @Override
    public PageResult search(Goods goods, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        GoodsQuery query = new GoodsQuery();
        GoodsQuery.Criteria criteria = query.createCriteria();
        if(goods.getGoodsName()!=null){
            criteria.andGoodsNameEqualTo("%"+goods.getGoodsName()+"%");
   }
   if(goods.getAuditStatus()!=null && !"".equals(goods.getAuditStatus())){
       criteria.andAuditStatusEqualTo(goods.getAuditStatus());
   }
   //判断账户只能查询自己的 所属商品
        if(goods.getSellerId()!=null && !"".equals(goods.getSellerId()) && !"admin".equals(goods.getSellerId())
                && !"wc".equals(goods.getSellerId())){
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }
        criteria.andIsDeleteIsNull();

        Page<Goods> goodsList = (Page<Goods>)goodsDao.selectByExample(query);
        return new PageResult(goodsList.getTotal(),goodsList.getResult());
    }
    //根据 goods id回显 商品信息，以预备完成修改。返回GoodsEntity对象。
    @Override
    public GoodsEntity findOne(Long id) {
        Goods goods = goodsDao.selectByPrimaryKey(id);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goods.getId());
        List<Item> items = itemDao.selectByExample(query);
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(items);
        return goodsEntity;
    }

    @Override
    public void update(GoodsEntity goodsEntity) {

            //
        if(goodsEntity.getGoods().getPrice()==null){
            goodsEntity.getGoods().setPrice(new BigDecimal(999999));
        }

        //如果修改了内容，需要重新审核
        goodsEntity.getGoods().setAuditStatus("0");
            goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());

            goodsDescDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());
            //先删掉之前的选项，再添加新的选项。
//            if(goodsEntity.getItemList()!=null){
//                for (Item item : goodsEntity.getItemList()) {
//                    itemDao.deleteByPrimaryKey(item.getId());
//                }
//            }
        ItemQuery query = new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(query);
        saveItemList(goodsEntity);
    }

    @Override
    public void delete(Long[] ids) {
        //不应该是物理删除，而是逻辑删除，就是把状态码修改为1
        if(ids!=null && ids.length>0){
            for (final Long id : ids) {
                Goods goods = new Goods();
                goods.setId(id);
                goods.setIsDelete("1");
                goodsDao.updateByPrimaryKeySelective(goods);

                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });
            }
        }
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        if(ids!=null && ids.length>0){
            for (final Long id : ids) {
                Goods goods = new Goods();
                goods.setId(id);
                goods.setAuditStatus(status);
                goodsDao.updateByPrimaryKeySelective(goods);

            //库存状态随着商品的状态一起修改、变动
            Item item = new Item();
            item.setStatus(status);

            //库存要指定此商品的id
            ItemQuery query = new ItemQuery();
            query.createCriteria().andGoodsIdEqualTo(id);
            itemDao.updateByExampleSelective(item,query);

            //如果审核通过时，消息中间件对外发送 goodsId.
                if("1".equals(status)){
                    jmsTemplate.send(topicPageAndSolr, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                            return textMessage;
                        }
                    });
                }
            }
        }
    }


}
