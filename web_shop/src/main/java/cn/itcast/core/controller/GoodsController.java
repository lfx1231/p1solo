package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.SolrService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private SolrService solrService;

    //第三方商家添加新的产品sku保存，待审核
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity){
        try {
            //获取，页面中登陆的username 用户名 英文或+字母，唯一的id
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsEntity.getGoods().setSellerId(userName);
            goodsService.add(goodsEntity);
            return new Result(true,"添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败！");
        }
    }
    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows){
        //获取当前登陆的用户信息，并设置到goods对象中
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(userName);
        PageResult pageResult = goodsService.search(goods,page,rows);
        return pageResult;
    }
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){
        GoodsEntity goodsEntity = goodsService.findOne(id);
        return goodsEntity;
    }
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity){
        try {
            goodsService.update(goodsEntity);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            if(ids!=null){
                solrService.deleteSolrByid(ids);
            }
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
}
