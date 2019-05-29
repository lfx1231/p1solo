package cn.itcast.core.controller;

import cn.itcast.core.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemsearch")
public class SearchController {
    @Reference
    private SearchService searchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map paramMap){
        Map<String,Object> maps = searchService.search(paramMap);
        return maps;
    }

}
