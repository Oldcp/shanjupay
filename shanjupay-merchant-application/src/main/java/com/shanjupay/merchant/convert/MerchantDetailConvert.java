package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author old money
 * @create 2022-03-12 17:56
 */
@Mapper
public interface MerchantDetailConvert {


    //转换类实例
    MerchantDetailConvert INSTANCE = Mappers.getMapper(MerchantDetailConvert.class);

    //把 DTO 转换成 VO
    MerchantDetailVO dto2vo(MerchantDTO merchantDTO);

    //把 VO 转换为 DTO
    MerchantDTO vo2dto(MerchantDetailVO merchantDetailVO);
}
