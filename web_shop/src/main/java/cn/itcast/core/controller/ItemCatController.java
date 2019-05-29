package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference
    private ItemCatService itemCatService;
    //根据父级id查询所有的分类商品信息， 注意参数一定要和前端传过来的变量名相同，否则获取不到参数。
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId){
       List<ItemCat> list = itemCatService.findByParentId(parentId);
       return list;
    }
    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){
        ItemCat itemCat = itemCatService.findOne(id);
        return itemCat;
    }
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
        List<ItemCat> list = itemCatService.findAll();
        return list;
    }


}
