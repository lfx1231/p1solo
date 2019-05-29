package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        boolean result = PhoneFormatCheckUtils.isChinaPhoneLegal(phone);
        if(!result){
            return new Result(false,"请输入合法的手机号码");
        }
        try {
            userService.sendCode(phone);
            return new Result(true,"发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"发送失败。");
        }
    }
    @RequestMapping("/add")
    public Result add(String smscode,@RequestBody User user){
        try {
            if (smscode==null || "".equals(smscode)){
                return new Result(false,"请填写验证码!");
            }
          Boolean result =  userService.checkCode(user.getPhone(),smscode);
          if(!result){
              return new Result(false,"您输入的验证码有误");
          }

            userService.add(user);
            return new Result(true,"注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }


}
