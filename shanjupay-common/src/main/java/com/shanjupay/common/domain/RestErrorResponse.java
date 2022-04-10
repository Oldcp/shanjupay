package com.shanjupay.common.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 返回给前端的异常响应参数
 *
 * @author old money
 * @create 2022-03-12 14:39
 */
@ApiModel(value = "RestErrorResponse",description = "异常响应参数包装")
@Data
public class RestErrorResponse {

    private String errCode;

    private String errMessage;

    public RestErrorResponse(String errCode,String errMessage){
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

}
