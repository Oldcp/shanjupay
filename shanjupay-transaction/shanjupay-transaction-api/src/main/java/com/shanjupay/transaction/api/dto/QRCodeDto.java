package com.shanjupay.transaction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author old money
 * @create 2022-03-17 17:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QRCodeDto {

    private Long merchantId;
    private String appId;
    private Long storeId;
    private String subject; //商品标题
    private String body; //订单描述

}
