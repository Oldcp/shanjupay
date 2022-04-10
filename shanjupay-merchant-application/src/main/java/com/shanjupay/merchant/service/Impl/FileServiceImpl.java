package com.shanjupay.merchant.service.Impl;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import com.shanjupay.merchant.service.FileService;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author old money
 * @create 2022-03-12 16:17
 */
public class FileServiceImpl implements FileService {


    /**
     * 从nacos配置中心中获取 上传文件所需的参数
     */
    @Value("${oss.qiniu.url}")
    private String qiniuUrl;
    @Value("${oss.qiniu.accessKey}")
    private String accessKey;
    @Value("${oss.qiniu.secretKey}")
    private String secretKey;
    @Value("${oss.qiniu.bucket}")
    private String bucket;


    /**
     * 上传文件
     * @param bytes 文件的字节数组
     * @param fileName 文件名
     * @return
     * @throws BusinessException
     */
    @Override
    public String upload(byte[] bytes, String fileName) throws BusinessException {

        //调用common下的工具类
        try {
            QiniuUtils.upload2qiniu(accessKey,secretKey,bucket,bytes,fileName);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }

        //上传成功后返回文件的访问地址（绝对路径）
        return qiniuUrl+fileName;
    }
}
