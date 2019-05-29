package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentCategoryService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {

    @Reference
    private ContentCategoryService contentCategoryService;

    //高级查询
    @RequestMapping("/search")
    public PageResult search(@RequestBody ContentCategory contentCategory,Integer page,Integer rows){
       PageResult pageResult = contentCategoryService.search(contentCategory,page,rows);
       return pageResult;
    }
    //查询所有的分类，以便广告修改所属分类，content里面修改时需要
    @RequestMapping("/findAll")
    public List<ContentCategory> findAll(){
        List<ContentCategory> list = contentCategoryService.findAll();
        return list;
    }

    //新增
    @RequestMapping("/add")
    public Result add(@RequestBody ContentCategory contentCategory){
        try {
            contentCategoryService.add(contentCategory);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    //修改，findOne
    @RequestMapping("/findOne")
    public ContentCategory findOne(Long id){
       ContentCategory one = contentCategoryService.findOne(id);
       return one;
    }

    //修改内容并保存操作，update
    @RequestMapping("/update")
    public Result update(@RequestBody ContentCategory contentCategory){
        try {
            contentCategoryService.update(contentCategory);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"修改失败");
        }
    }
    //根据id删除
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentCategoryService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }



}
