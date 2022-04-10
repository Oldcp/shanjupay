package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/**
 * 应用管理相关的接口
 * @author old money
 * @create 2022-03-12 18:32
 */
public interface AppService {


    /**
     * 商户下创建应用
     * @param merchantId
     * @param appDTO
     * @return
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId ,AppDTO appDTO) throws BusinessException;



    /**
     * 查询商户下的应用列表
     * @param merchantId  商户id
     * @return
     * @throws BusinessException
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;




    /**
     * 根据应用的id 查询应用信息
     * @param appid 应用id
     * @return
     * @throws BusinessException
     */
    AppDTO getAppById(String appid) throws BusinessException;




    /**
     * 校验应用是否属于商户
     * @param appId
     * @param merchantId
     * @return
     */
    Boolean queryAppInMerchant(String appId,Long merchantId);


}
