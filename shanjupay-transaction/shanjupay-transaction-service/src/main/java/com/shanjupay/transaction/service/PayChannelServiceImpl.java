package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author old money
 * @create 2022-03-14 14:33
 */
@Service
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    private PlatformChannelMapper platformChannelMapper;

    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    private PayChannelParamMapper payChannelParamMapper;


    @Autowired
    private Cache cache;




    /**
     * 查询平台所有的服务类型
     * @return 服务类型列表
     * @throws BusinessException
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {

        List<PlatformChannel> platformChannelList = platformChannelMapper.selectList(null);

        ArrayList<PlatformChannelDTO> platformChannelDTOS = new ArrayList<>();

        for (PlatformChannel platformChannel : platformChannelList) {

            PlatformChannelDTO platformChannelDTO = PlatformChannelConvert.INSTANCE.entity2dto(platformChannel);
            platformChannelDTOS.add(platformChannelDTO);
        }

        return platformChannelDTOS;
    }



    /**
     * 为应用绑定平台服务类型
     * @param appId 应用id
     * @param platformChannelCodes 服务类型的code
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {

        //先根据应用的id和服务类型的code进行查询是否已经绑定
        QueryWrapper<AppPlatformChannel> wrapper = new QueryWrapper<>();
        wrapper.eq("appId",appId);
        wrapper.eq("platformChannel",platformChannelCodes);
        AppPlatformChannel selectOne = appPlatformChannelMapper.selectOne(wrapper);
        if (selectOne == null){
            //向AppPlatformChannel插入记录
            AppPlatformChannel appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
        throw new BusinessException(CommonErrorCode.E_300008);

    }





    /**
     * 查看应用是否绑定了某个服务类型
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @return
     * @throws BusinessException
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {

        QueryWrapper<AppPlatformChannel> wrapper = new QueryWrapper<>();
        wrapper.eq("appId",appId);
        wrapper.eq("platformChannel",platformChannel);
        Integer count = appPlatformChannelMapper.selectCount(wrapper);
        if (count > 0){
            return 1;
        }
        return 0;
    }





    /**
     * 根据服务类型查询支付渠道
     * @param platformChannelCode 服务类型编码
     * @return 支付渠道列表
     * @throws BusinessException
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        //调用mapper 查询 platform_pay_channel,pay_channel,platform_channel
        List<PayChannelDTO> payChannelDTOS = platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);

        return payChannelDTOS;
    }





    /**
     *保存 配置的支付渠道参数
     * @param payChannelParamDTO 配置支付渠道参数：包括 商户id 应用id，服务类型code，支付渠道code，配置名称，配置参数（json）
     * @throws BusinessException
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException {

        if (payChannelParamDTO == null || payChannelParamDTO.getParam() ==null){
            throw new BusinessException(CommonErrorCode.E_100106);
        }

        //根据应用，服务类型 查询应用和服务类型的绑定id
        Long aLong = selectIfByAppPlatformChannel(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
        if (aLong == null){
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据应用和服务类型的绑定id和支付渠道查询PayChannelParam的一条记录
        QueryWrapper<PayChannelParam> wrapper = new QueryWrapper<>();
        wrapper.eq("appPlatformChannelId",aLong);
        wrapper.eq("payChannel",payChannelParamDTO.getPayChannel());
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(wrapper);
        //如果存在配置则更新
        if (payChannelParam != null){
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            payChannelParamMapper.updateById(payChannelParam);
        }
        //否则添加配置
        PayChannelParam entity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
        entity.setId(null);
        entity.setAppPlatformChannelId(aLong);
        payChannelParamMapper.insert(entity);

        //向redis缓存支付渠道参数
        updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPlatformChannelCode());
    }





    /**
     * 根据应用id，服务类型，查询对应的支付渠道参数列表
     * @param appId 应用id
     * @param platformChannel 平台服务类型
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayChannelParamDTO> queryChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {

        //先从redis查询，如果查询到则返回，否则从数据库查询，从数据库查询完毕在将数据保存到redis
        String keyBuilder = RedisUtil.keyBuilder(appId, platformChannel);
        Boolean exists = cache.exists(keyBuilder);
        if (exists){
            String payChannelParamDTO = cache.get(keyBuilder);
            List<PayChannelParamDTO> payChannelParamDTOS = JSON.parseArray(payChannelParamDTO, PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }

        Long aLong = selectIfByAppPlatformChannel(appId, platformChannel);
        if (aLong == null){
            return null;
        }
        QueryWrapper<PayChannelParam> wrapper = new QueryWrapper<>();
        wrapper.eq("appPlatformChannelId",aLong);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(wrapper);

        ArrayList<PayChannelParamDTO> payChannelParamDTOS = new ArrayList<>();
        for (PayChannelParam payChannelParam : payChannelParams) {
            PayChannelParamDTO payChannelParamDTO = PayChannelParamConvert.INSTANCE.entity2dto(payChannelParam);
            payChannelParamDTOS.add(payChannelParamDTO);
        }
        updateCache(appId,platformChannel);
        return payChannelParamDTOS;
    }







    /**
     * 根据应用id，服务类型，支付渠道代码，查询对应的支付渠道参数
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @param payChannel 支付渠道代码
     * @return
     * @throws BusinessException
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        Long aLong = selectIfByAppPlatformChannel(appId, platformChannel);
        if (aLong == null){
            return null;
        }
        QueryWrapper<PayChannelParam> wrapper = new QueryWrapper<>();
        wrapper.eq("appPlatformChannelId",aLong);
        wrapper.eq("payChannel",payChannel);
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(wrapper);

        return PayChannelParamConvert.INSTANCE.entity2dto(payChannelParam);
    }


    /**
     * 根据应用id和服务类型 查询 应用与服务类型的绑定id
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIfByAppPlatformChannel(String appId,String platformChannelCode){
        QueryWrapper<AppPlatformChannel> wrapper = new QueryWrapper<>();
        wrapper.eq("appId",appId);
        wrapper.eq("platformChannel",platformChannelCode);
        AppPlatformChannel selectOne = appPlatformChannelMapper.selectOne(wrapper);
        if (selectOne != null){
            return selectOne.getId();
        }
        return null;
    }





    /**
     *根据应用id 和服务类型将查询到的支付渠道参数配置列表写入redis
     * @param appId
     * @param platformChannelCode
     */
    private void updateCache(String appId,String platformChannelCode){
        //得到redis中key（支付渠道参数配置列表的key）
        //格式：JS_PAY_PARAN：应用id：服务类型code
        String keyBuilder = RedisUtil.keyBuilder(appId, platformChannelCode);
        //根据key查询redis
        Boolean exists = cache.exists(keyBuilder);
        if (exists){
            cache.del(keyBuilder);
        }
        //根据应用id和服务类型code查询支付渠道参数
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannelCode);
        //将支付渠道参数列表存储到redis
        if (payChannelParamDTOS != null){
            cache.set(keyBuilder, JSON.toJSON(payChannelParamDTOS).toString());
        }


    }



    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {

        Long aLong = selectIfByAppPlatformChannel(appId, platformChannel);
        if (aLong == null){
            return null;
        }
        QueryWrapper<PayChannelParam> wrapper = new QueryWrapper<>();
        wrapper.eq("appPlatformChannelId",aLong);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(wrapper);

        ArrayList<PayChannelParamDTO> payChannelParamDTOS = new ArrayList<>();
        for (PayChannelParam payChannelParam : payChannelParams) {
            PayChannelParamDTO payChannelParamDTO = PayChannelParamConvert.INSTANCE.entity2dto(payChannelParam);
            payChannelParamDTOS.add(payChannelParamDTO);
        }
        return payChannelParamDTOS;
    }

}
