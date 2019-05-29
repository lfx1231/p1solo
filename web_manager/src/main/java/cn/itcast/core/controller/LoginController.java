package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/showName")
    public Map showName(){
        User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String,String> map = new HashMap<>();
        map.put("username",user.getUsername());
        return map;
    }


}
