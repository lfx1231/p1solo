package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;

import cn.itcast.core.service.PageService;
import cn.itcast.core.service.SolrService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;
    @Reference
    private SolrService solrService;
    @Reference
    private PageService pageService;

    //高级查询，所有
    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(username);
        PageResult pageResult = goodsService.search(goods,page,rows);
        return pageResult;
    }
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){
        GoodsEntity one = goodsService.findOne(id);
        return one;
    }
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {

            goodsService.updateStatus(ids,status);
//            if("1".equals(status)&& ids!=null ){
//                solrService.addItemToSolr(ids);
//            }
//            if(ids!=null){
//                for (Long id : ids) {
//                    //状态该成功后生成静态页面。
//                    Map<String, Object> rootMap = pageService.findGoodsData(id);
//                    pageService.createStaticPage(id,rootMap);
//                }
//            }
            return new Result(true,"修改状态成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改状态失败");
        }

    }

    @RequestMapping("/delete")
    public  Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
//            if(ids!=null){
//                solrService.deleteSolrByid(ids);
//            }
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
//    @RequestMapping("/page")
//    public Result test(Long goodsId){
//        try {
//            Map<String, Object> rootMap = pageService.findGoodsData(goodsId);
//            pageService.createStaticPage(goodsId,rootMap);
//            return new Result(true,"生成成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Result(false,"生成失败");
//        }
//    }

}
