package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author old money
 * @create 2022-03-12 18:34
 */
@Service
public class AppServiceImpl implements AppService {


    @Autowired
    private AppMapper appMapper;

    @Autowired
    private AppCovert appCovert;

    @Autowired
    private MerchantMapper merchantMapper;


    /**
     * 商户下创建应用
     * @param merchantId
     * @param appDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {
        if (merchantId == null||appDTO == null|| StringUtils.isBlank(appDTO.getAppName())){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //1.校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        String auditStatus = merchant.getAuditStatus();
        if (! auditStatus.equals("2")){
            throw new BusinessException(CommonErrorCode.E_200003);
        }

        //应用的名称需要校验唯一性
        QueryWrapper<App> appQueryWrapper = new QueryWrapper<>();
        appQueryWrapper.eq("appName",appDTO.getAppName());
        Integer count = appMapper.selectCount(appQueryWrapper);
        if (count > 0){
            throw new BusinessException(CommonErrorCode.E_200004);
        }
        //2.生成应用ID
        String appId = UUID.randomUUID().toString();

        //3.保存应用信息
        App app = appCovert.dto2entity(appDTO);
        app.setAppId(appId);
        app.setMerchantId(merchantId);
        appMapper.insert(app);

        return appCovert.entity2dto(app);
    }


    /**
     * 根据商户id，查询商户下的应用列表
     * @param merchantId  商户id
     * @return
     * @throws BusinessException
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {

        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.eq("merchantId",merchantId);

        List<App> apps = appMapper.selectList(wrapper);
        ArrayList<AppDTO> appDTOlist = new ArrayList<>();
        for (App app : apps) {
            AppDTO appDTO = appCovert.entity2dto(app);
            appDTOlist.add(appDTO);
        }
        return appDTOlist;
    }





    /**
     * 根据应用id，查询应用信息
     * @param appid 应用id
     * @return
     * @throws BusinessException
     */
    @Override
    public AppDTO getAppById(String appid) throws BusinessException {

        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.eq("appId",appid);
        App app = appMapper.selectOne(wrapper);
        return appCovert.entity2dto(app);
    }




    /**
     *校验应用是否属于商户
     * @param appId
     * @param merchantId
     * @return
     */
    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) {
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.eq("appId",appId);
        wrapper.eq("merchantId",merchantId);
        Integer count = appMapper.selectCount(wrapper);

        return count > 0;
    }
}
