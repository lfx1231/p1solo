package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    //高级搜索功能 search
    @RequestMapping("/search")
    public PageResult search(@RequestBody Specification spec, Integer page, Integer rows){
        PageResult pageResult = specificationService.search(spec,page,rows);
        return pageResult;
    }

    //add
    @RequestMapping("/add")
    public Result add(@RequestBody SpecEntity specEntity){
        try {
            specificationService.add(specEntity);
            return new Result(true,"添加成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败！");
        }
    }
    @RequestMapping("/findOne")
    public SpecEntity findOne(Long id){
        SpecEntity specEntity = specificationService.findOne(id);
        return specEntity;
    }
    @RequestMapping("/update")
    public Result update(@RequestBody SpecEntity specEntity){
        try {
            specificationService.update(specEntity);
            return new Result(true,"修改成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败！");
        }
    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败！");
        }
    }
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        List<Map> maps = specificationService.selectOptionList();
        return maps;
    }


}
