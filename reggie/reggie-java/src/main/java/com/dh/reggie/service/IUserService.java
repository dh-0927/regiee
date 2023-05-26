package com.dh.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface IUserService extends IService<User> {
    R<String> sendMsg(User user, HttpSession session);

    R<User> login(Map map, HttpSession session);

    R<String> loginout(HttpSession session);
}
