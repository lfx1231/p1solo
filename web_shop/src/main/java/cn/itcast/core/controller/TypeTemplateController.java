package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("typeTemplate")
public class TypeTemplateController {

    @Reference
    private TypeTemplateService typeTemplateService;


    @RequestMapping("findOne")
    public TypeTemplate findOne(Long id){
        TypeTemplate typeTemplate = typeTemplateService.findOne(id);
        return typeTemplate;
    }

    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id){
        List<Map> maps = typeTemplateService.findBySpecList(id);
        return maps;

    }




}
