package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author old money
 * @create 2022-03-10 17:40
 */
@Service
class MerchantServiceImpl implements MerchantService{

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private StoreStaffMapper storeStaffMapper;

    @Reference
    private TenantService tenantService;



    /**
     * 根据id查询商户信息
     * @param id
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long id) {

        Merchant merchant = merchantMapper.selectById(id);
        MerchantDTO merchantDTO = MerchantConvert.INSTANCE.entity2dto(merchant);

        return merchantDTO;
    }


    /**
     * 注册商户
     * 调用Saas接口新增租户，用户，绑定租户和用户的关系，初始化权限
     * @param merchantDTO 商户注册信息
     * @return
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {

        //校验参数的合法性
        if (merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if (StringUtils.isNotBlank(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        //校验手机号是否合法
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        if (StringUtils.isBlank(merchantDTO.getPassword())){
            throw new BusinessException(CommonErrorCode.E_100111);
        }

        //校验手机号的唯一性
        QueryWrapper<Merchant> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile",merchantDTO.getMobile());
        Integer integer = merchantMapper.selectCount(wrapper);
        if (integer > 0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }


        //调用Saas接口
        //构造调用的参数
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");//租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant");//套餐，根据套餐进行分派权限
        createTenantRequestDTO.setName(merchantDTO.getUsername());

        //如果租户在SaaS已经存在，Saas直接返回次租户信息，否则进行添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        //获取租户的id
        if (tenantAndAccount == null||tenantAndAccount.getId() == null){
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        Long tenantId = tenantAndAccount.getId();
        //根据租户的id从商户表查询如果存在记录则不允许添加商户
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        wrapper.eq("tenantId",tenantId);
        Integer count = merchantMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(CommonErrorCode.E_200017);
        }


        //使用MapStruct进行对象转换
        Merchant merchant = MerchantConvert.INSTANCE.dto2entity(merchantDTO);

        //设置商户对应的租户id
        merchant.setTenantId(tenantId);

        //审核状态为 0  表示未进行资质申请
        merchant.setAuditStatus("0");
        merchantMapper.insert(merchant);

        //新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId());
        StoreDTO store = createStore(storeDTO);
        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchant.getMobile());
        staffDTO.setUsername(merchant.getUsername());
        staffDTO.setStoreId(storeDTO.getId());
        staffDTO.setMerchantId(merchant.getId());
        StaffDTO staff = createStaff(staffDTO);

        //为门店设置管理员（门店和员工的绑定关系）
        bindStaffToStore(store.getId(),staff.getId());

        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }




    /**
     * 保存商户资质申请信息
     * @param merchantId 商户id
     * @param merchantDTO 资质申请信息
     * @throws BusinessException
     */
    @Transactional
    @Override
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {

        if (merchantId == null || merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //校验 merchantId的合法性，查询商户表，如果查询不到记录，认为非法
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null){
            throw new BusinessException(CommonErrorCode.E_100104);
        }

        Merchant entity = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile()); //因为资质申请中，手机号是不能修改的，所以还是需要使用原来数据中的手机号
        entity.setAuditStatus("1");
        entity.setTenantId(merchant.getTenantId());
        merchantMapper.updateById(entity);
    }





    /**
     * 新增门店
     * @param storeDTO 门店信息
     * @return
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {

        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        storeMapper.insert(store);

        return StoreConvert.INSTANCE.entity2dto(store);
    }




    /**
     * 商户新增员工
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //参数合法性校验
        if (staffDTO == null || staffDTO.getMobile() == null || staffDTO.getUsername() == null || staffDTO.getStoreId() == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }

        //在同一个商户下员工的账号唯一
        boolean byUserName = isExistStaffByUserName(staffDTO.getUsername(), staffDTO.getMerchantId());
        if (byUserName){
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        //在同一个商户下员工的手机号唯一
        boolean staffByMobile = isExistStaffByMobile(staffDTO.getMobile(), staffDTO.getMerchantId());
        if (staffByMobile){
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        Staff staff = StaffConvert.INSTANCE.dto2entity(staffDTO);
        staffMapper.insert(staff);

        return StaffConvert.INSTANCE.entity2dto(staff);
    }





    /**
     * 为门店设置管理员 (门店和员工的绑定关系)
     * @param storeId 门店id
     * @param staffId 员工id
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {

        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);
        storeStaff.setStoreId(storeId);
        storeStaffMapper.insert(storeStaff);
    }







    /**
     * 根据租户id查询商户信息
     * @param tenantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) {
        QueryWrapper<Merchant> wrapper = new QueryWrapper<>();
        wrapper.eq("tenantId",tenantId);
        Merchant merchant = merchantMapper.selectOne(wrapper);

        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }




    /**
     * 分页查询门店列表
     * @param storeDTO 查询条件，必要参数：商户id
     * @param pageNo 页码
     * @param pageSize 每页记录数
     * @return
     */
    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) {

        //分页的条件
        Page<Store> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Store> wrapper = new QueryWrapper<>();

        if (storeDTO != null || storeDTO.getMerchantId() != null){
            wrapper.eq("merchantId",storeDTO.getMerchantId());
        }
        if (storeDTO != null || storeDTO.getStoreName() != null || storeDTO.getStoreName() != ""){
            wrapper.eq("storeName",storeDTO.getStoreName());
        }
        if (storeDTO != null || storeDTO.getStoreNumber() != null){
            wrapper.eq("storeNumber",storeDTO.getStoreNumber());
        }
        if (storeDTO != null || storeDTO.getStoreAddress() != null){
            wrapper.eq("storeAddress",storeDTO.getStoreAddress());
        }
        IPage<Store> storeIPage = storeMapper.selectPage(page, wrapper);
        List<Store> records = storeIPage.getRecords();
        List<StoreDTO> storeDTOS = StoreConvert.INSTANCE.listentity2dto(records);
        return new PageVO(storeDTOS,storeIPage.getTotal(),pageNo,pageSize);
    }




    /**
     * 校验门店是否属于商户
     * @param storeId
     * @param merchantId
     * @return
     */
    @Override
    public Boolean queryStoreInMerchant(Long storeId, Long merchantId) {
        QueryWrapper<Store> wrapper = new QueryWrapper<>();
        wrapper.eq("id",storeId);
        wrapper.eq("merchantId",merchantId);
        Integer count = storeMapper.selectCount(wrapper);

        return count > 0;
    }



    /**
     * 根据手机号判断员工是否已在指定商户存在
     * @param mobile
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByMobile(String mobile,Long merchantId){
        QueryWrapper<Staff> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile",mobile);
        wrapper.eq("merchantId",merchantId);
        Integer count = staffMapper.selectCount(wrapper);
        return count > 0;
    }






    /**
     *根据用户名判断员工是否已在指定商户存在
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUserName(String userName,Long merchantId){
        QueryWrapper<Staff> wrapper = new QueryWrapper<>();
        wrapper.eq("username",userName);
        wrapper.eq("merchantId",merchantId);
        Integer count = staffMapper.selectCount(wrapper);
        return count > 0;
    }


}
