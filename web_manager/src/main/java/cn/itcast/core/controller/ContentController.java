package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {
    @Reference
    private ContentService contentService;
    //高级查询，查询所有
    @RequestMapping("/search")
    public PageResult search(@RequestBody Content content, Integer page, Integer rows){
       PageResult pageResult = contentService.search(content,page,rows);
       return pageResult;
    }
    //需要一个findAll
    @RequestMapping("/findAll")
    public List<Content> findAll(){
       List<Content> list = contentService.findAll();
       return list;
    }

    //新增 条目
    @RequestMapping("/add")
    public Result add(@RequestBody Content content){
        try {
            contentService.add(content);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
    //修改时，回显，findOne
    @RequestMapping("/findOne")
    public Content findOne(Long id){
       Content one = contentService.findOne(id);
       return one;
    }

    //修改后返回对象，保存update
    @RequestMapping("/update")
    public Result update(@RequestBody Content content){
        try {
            contentService.update(content);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    //删除根据id
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            contentService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }


}
