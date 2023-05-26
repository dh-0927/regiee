package com.dh.reggie.interceptors;

import com.alibaba.fastjson.JSON;
import com.dh.reggie.common.BaseContext;
import com.dh.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.dh.reggie.constants.BackendConstants.LOGIN_SESSION;
import static com.dh.reggie.constants.BackendConstants.LOGIN_USER_SESSION;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long rootId = (Long) request.getSession().getAttribute(LOGIN_SESSION);

        if (rootId == null) {

            Long userId = (Long) request.getSession().getAttribute(LOGIN_USER_SESSION);
            if (userId == null) {
                log.info("拦截到请求：" + request.getRequestURL());
                response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
                return false;
            }
            BaseContext.set(userId);
            return true;
        }

        // 将用户id存进ThreadLocal中
        BaseContext.set(rootId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.remove();
    }
}
