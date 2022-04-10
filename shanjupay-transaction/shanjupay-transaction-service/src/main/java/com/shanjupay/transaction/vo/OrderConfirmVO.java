package com.shanjupay.transaction.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author old money
 * @create 2022-03-20 14:10
 */
@Data
@ApiModel(value = "OrderConfirmVO",description = "订单确认信息")
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmVO {

    private String appId; //应用id
    private String tradeNo; //订单号
    private String openId; //微信openid
    private String storeId; //门店id
    private String channel; //服务类型
    private String body; //订单描述
    private String subject; //订单标题
    private String totalAmount; //金额
}
