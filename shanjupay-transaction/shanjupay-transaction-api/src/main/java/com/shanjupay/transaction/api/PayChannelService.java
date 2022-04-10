package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

/**
 *
 * 交易服务接口
 *
 * @author old money
 * @create 2022-03-14 14:30
 */
public interface PayChannelService {

    /**
     * 查询平台所有的服务类型
     * @return 服务类型列表
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;


    /**
     * 为应用绑定平台服务类型
     * @param appId 应用id
     * @param platformChannelCodes 服务类型的code
     * @throws BusinessException
     */
    void bindPlatformChannelForApp(String appId,String platformChannelCodes) throws  BusinessException;






    /**
     *查看应用是否绑定了某个服务类型
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @return 已绑定返回1，未绑定返回0
     * @throws BusinessException
     */
    int queryAppBindPlatformChannel(String appId,String platformChannel) throws  BusinessException;





    /**
     *根据服务类型查询对应的支付渠道
     * @param platformChannelCode 服务类型编码
     * @return 支付渠道列表
     * @throws BusinessException
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException;




    /**
     *保存配置的支付渠道参数
     * @param payChannelParamDTO 配置支付渠道参数：包括 商户id 应用id，服务类型code，支付渠道code，配置名称，配置参数（json）
     * @throws BusinessException
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException;




    /**
     * 获取指定应用指定服务类型下所包含的所有支付渠道参数列表
     * @param appId 应用id
     * @param platformChannel 平台服务类型
     * @return 支付渠道参数列表
     */
    List<PayChannelParamDTO> queryChannelParamByAppAndPlatform(String appId,String platformChannel) throws BusinessException;





    /**
     *根据应用，服务类型，支付渠道代码 查询该支付渠道参数
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @param payChannel 支付渠道代码
     * @return 支付渠道参数
     * @throws BusinessException
     */
    PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId,String platformChannel,String payChannel) throws BusinessException;

}
