package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.User;
import com.dh.reggie.mapper.UserMapper;
import com.dh.reggie.service.IUserService;
import com.dh.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.dh.reggie.constants.BackendConstants.*;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public R<String> sendMsg(User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isBlank(phone)) {
            return R.error("");
        }
        // 生成随机的6位校验码
        String code = ValidateCodeUtils.generateValidateCode(6).toString();
        // 调用阿里云的短信服务API完成发送短信
//        SMSUtils.sendMessage(phone, code);
        log.info("验证码为：" + code);
//        // 需要将生成的验证码以key为手机号的格式保存在session中
//        session.setAttribute(phone, code);
        // 保存到redis中
        stringRedisTemplate.opsForValue().set(USER_LOGIN_CODE + phone, code, 5, TimeUnit.MINUTES);
        return R.success("短信发送成功");
    }

    @Override
    public R<User> login(Map map, HttpSession session) {
        // 获取用户传过来的手机号
        String phone = map.get("phone").toString();
        // 获取用户输入的验证码
        String code = map.get("code").toString();
//        // 取出session中手机号对应的验证码
//        String codeWithSession = session.getAttribute(phone).toString();
        // 在redis中查找对应的验证码
        String key = USER_LOGIN_CODE + phone;
        String codeWithRedis = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(codeWithRedis)) {
            return R.error("手机号不匹配");
        }
        if (!StringUtils.isNotBlank(code)) {
            return R.error("验证码不能为空");
        }
        // 两个验证码进行比较
        if (!code.equals(codeWithRedis)) {
            // 不同，返回验证码错误的信息
            return R.error("验证码有误");
        }
        // 相同，则去查询数据库，看用户是否第一次登陆
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getPhone, phone);
        User user = getOne(lqw);
        // 如果user为空，则将用户信息保存进数据库
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        // 将用户的id保存进session
        session.setAttribute(LOGIN_USER_SESSION, user.getId());
        // 用户登陆成功，删除redis中该手机号对应的验证码
        stringRedisTemplate.delete(key);
        return R.success(user);
    }

    @Override
    public R<String> loginout(HttpSession session) {
        // 将用户的session清除
        session.removeAttribute(LOGIN_USER_SESSION);
        return R.success("退出成功");

    }

    public User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setName(USER_NICK_NAME_PREFIX + UUID.randomUUID().toString().substring(0, 8) /*"傻逼林锐"*/);
        if (save(user)) {
            log.debug("添加新用户成功。");
        }
        return user;
    }
}
