package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import io.swagger.models.auth.In;

/**
 * 商户接口
 *
 * @author old money
 * @create 2022-03-10 17:34
 */
public interface MerchantService {

    /**
     * 根据id查询当前商户
     * @param id
     * @return
     */
    public MerchantDTO queryMerchantById(Long id);


    /**
     *注册商户
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;


    /**
     * 保存商户资质申请信息
     * @param merchantId 商户id
     * @param merchantDTO 资质申请信息
     */
    void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;



    /**
     * 新增门店
     * @param storeDTO 门店信息
     * @return 新增的门店信息
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;





    /**
     *商户新增员工
     * @param staffDTO 员工信息
     * @return 新增成功的员工信息
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;





    /**
     *为门店设置管理员（绑定员工和门店）
     * @param storeId 门店id
     * @param staffId 员工id
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId,Long staffId) throws BusinessException;




    /**
     * 根据租户id查询商户信息
     * @param tenantId
     * @return
     */
    public MerchantDTO queryMerchantByTenantId(Long tenantId);





    /**
     *分页查询门店列表
     * @param storeDTO 查询条件，必要参数：商户id
     * @param pageNo 页码
     * @param pageSize 每页记录数
     * @return
     */
    PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo,Integer pageSize);





    /**
     * 校验门店是否属于商户
     * @param StoreId
     * @param merchantId
     * @return
     */
    Boolean queryStoreInMerchant(Long StoreId,Long merchantId);
}
