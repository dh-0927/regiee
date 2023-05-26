package com.dh.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> handle(SQLIntegrityConstraintViolationException ex) {
        String msg = ex.getMessage();
        log.error(msg);
        if (msg.contains("Duplicate entry")) {
            return R.error(msg.split(" ")[2] + "已存在！");
        }
        return R.error("未知错误");
    }
}
