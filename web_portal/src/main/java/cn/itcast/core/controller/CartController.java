package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.BuyerCartService;
import cn.itcast.core.util.Constants;
import cn.itcast.core.util.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Reference
    private BuyerCartService buyerCartService;


    @CrossOrigin(origins = "http://localhost:8081",allowCredentials = "true")
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList( Long itemId, Integer num){
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<BuyerCart> cartList =  findCartList();
            cartList = buyerCartService.addItemToCartList(cartList, itemId, num);
            if("anonymousUser".equals(username)){
                String cartListJsonStr =JSON.toJSONString(cartList);
                CookieUtil.setCookie(request,response,Constants.COOKIE_CARTLIST,cartListJsonStr,3600*24*30,"utf-8");
            }else{
                buyerCartService.setCartListToRedis(username,cartList);

            }
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }



    //展示购物车。首先到cookie中看有没有已经存在的购物车，以电脑为单位，不管是谁添加的，反正是
    // 这个电脑用户之前添加的，一律获取。
    @RequestMapping("/findCartList")
    public List<BuyerCart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListJsonStr = CookieUtil.getCookieValue(request, Constants.COOKIE_CARTLIST, "utf-8");

        if(cartListJsonStr==null || "".equals(cartListJsonStr)){
            cartListJsonStr = "[]";
        }
        List<BuyerCart> cookieCartList = JSON.parseArray(cartListJsonStr, BuyerCart.class);
        if("anonymousUser".equals(username)){
            return cookieCartList;
        }else{
            List<BuyerCart> redisCartList = buyerCartService.getCartListByRedis(username);

            if(redisCartList==null){
                redisCartList = new ArrayList<BuyerCart>();
            }
            if(cookieCartList != null && cookieCartList.size() > 0){
                redisCartList = buyerCartService.mergeCookieRedis(cookieCartList,redisCartList);
                //合并后删除 cookie中的购物车信息
                CookieUtil.deleteCookie(request,response,Constants.COOKIE_CARTLIST);
                //把合并后的redisCartList 添加到redis中
                buyerCartService.setCartListToRedis(username,redisCartList);
            }
            return redisCartList;
        }

    }

}
