package com.dh.reggie.controller;

import com.dh.reggie.common.R;
import com.dh.reggie.entiry.User;
import com.dh.reggie.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        return userService.sendMsg(user, session);
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        return userService.login(map, session);
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        return userService.loginout(session);
    }
}
