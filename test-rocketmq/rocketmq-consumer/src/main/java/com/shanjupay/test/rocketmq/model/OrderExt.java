package com.shanjupay.test.rocketmq.model;



import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author old money
 * @create 2022-03-21 14:06
 */
@Data
@NoArgsConstructor
@ToString
public class OrderExt implements Serializable {

    private String id;

    private Date createTime;

    private Long money;

    private String title;
}
