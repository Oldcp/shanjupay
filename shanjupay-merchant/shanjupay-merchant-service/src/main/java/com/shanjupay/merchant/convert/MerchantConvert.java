package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import javax.swing.text.html.parser.Entity;

/**
 * MapStruct 对象转换类
 * 定义 DTO 和 entity之间的转换规则
 * @author old money
 * @create 2022-03-11 18:01
 */
@Mapper //对象属性映射
public interface MerchantConvert {

    //转换类实例
    MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

    //把 DTO 转换成 entity
    Merchant dto2entity(MerchantDTO merchantDTO);

    //把 entity 转换为 DTO
    MerchantDTO entity2dto(Merchant merchant);



}
