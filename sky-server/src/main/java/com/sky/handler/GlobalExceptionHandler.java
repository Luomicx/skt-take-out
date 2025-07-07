package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        // Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 处理密码错误异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(PasswordErrorException ex) {
        log.error("密码错误：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


    /**
     * 处理运行时异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(RuntimeException ex) {
        log.error("运行时异常：{}", ex.getMessage());
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 处理其他异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex) {
        log.error("其他异常：{}", ex.getMessage());
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
