package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    public void sendCode(String phone);

    void add(User user);

    Boolean checkCode(String phone, String smscode);
}
