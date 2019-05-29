package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    //查找所有的 brand品牌信息
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        List<Brand> brands = brandService.findAll();
        return brands;
    }

    //用pageHelper插件，获取分页信息
    @RequestMapping("/findPage")
    public PageResult findPage(Integer page,Integer rows) {
        PageResult pageResult = brandService.findPage(page, rows);
        return pageResult;
    }
    //新建，新增一个品牌
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }
    //修改品牌信息，因为要根据id来修改，所以要先根据id获取该brand品牌的所有信息
    @RequestMapping("/findOne")
    public Brand findOne(Long id){
       Brand brand = brandService.findOne(id);
       return brand;
    }
    //修改后，保存，返回一个brand对象
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.del(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }

    }
    //高级查询，带搜索框的 关键字模糊查询
    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand,Integer page,Integer rows){
        PageResult pageResult = brandService.search(brand,page,rows);
        return pageResult;
    }
    //添加模板的 下拉菜单选择项目
    //需要返回的格式如下：{"id":11,"text":"诺基亚"},{"id":12,"text":"锤子"}
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        List<Map> mapList = brandService.selectOptionList();
        return mapList;
    }

}
