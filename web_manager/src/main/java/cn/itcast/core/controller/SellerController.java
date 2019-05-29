package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
    private SellerService sellerService;

    //运营商管理，查看第三方卖家列表
    @RequestMapping("/search")
    public PageResult findPage(@RequestBody Seller seller,Integer page,Integer rows){
        PageResult pageResult = sellerService.findPage(seller,page,rows);
        return pageResult;

    }

    //修改时，根据id回显第三方某一个卖家
    @RequestMapping("/findOne")
    public Seller findOne(String id){
        Seller one = sellerService.findOne(id);
        return one;
    }
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId,String status){
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"状态修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"状态修改失败");
        }
    }
}
