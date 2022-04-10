package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 将商户注册的 VO 和 DTO 进行转换
 * @author old money
 * @create 2022-03-11 18:19
 */
@Mapper
public interface MerchantRegisterConvert {

    //转换类实例
    MerchantRegisterConvert INSTANCE = Mappers.getMapper(MerchantRegisterConvert.class);

    //把 DTO 转换成 VO
    MerchantRegisterVO dto2vo(MerchantDTO merchantDTO);

    //把 VO 转换为 DTO
    MerchantDTO vo2dto(MerchantRegisterVO merchantRegisterVO);


}
