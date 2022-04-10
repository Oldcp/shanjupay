package com.shanjupay.merchant.common.intercept;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.RestErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * 全局异常处理器
 * @author old money
 * @create 2022-03-12 14:46
 */

@ControllerAdvice // 与 @Exceptionhandler 配合使用实现全局异常处理
public class GlobalExceptionHandler {

    //捕获Exception异常
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse processExcetion(HttpServletRequest request, HttpServletResponse response,
                                             Exception e){

        //解析异常信息
        //如果是系统自定义异常，之间取出errCode和ErrMessage
        if (e instanceof BusinessException){
            BusinessException businessException = (BusinessException) e;
            ErrorCode errorCode = businessException.getErrorCode();
            int code = errorCode.getCode();

            String desc = errorCode.getDesc();

            return new RestErrorResponse(String.valueOf(code),desc);
        }

        //统一定义为99999系统未知错误异常
        return new RestErrorResponse(String.valueOf(CommonErrorCode.UNKOWN.getCode()),CommonErrorCode.UNKOWN.getDesc());
    }

}
