/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsBossDto;
import com.course.server.service.rdms.RdmsBossService;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/boss")
public class BossController {
    private static final Logger LOG = LoggerFactory.getLogger(BossController.class);
    public static final String BUSINESS_NAME = "最终管理人";

    @Resource
    private RdmsBossService rdmsBossService;

    /**
     * 分页查询所有最终管理人信息
     * 支持分页参数，返回分页后的最终管理人列表数据
     * 
     * @param pageDto 分页查询参数，包含页码、每页大小等信息
     * @return 返回分页后的最终管理人列表数据
     */
    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsBossDto>> list(@RequestBody PageDto<RdmsBossDto> pageDto) {
        ResponseDto<PageDto<RdmsBossDto>> responseDto = new ResponseDto<>();
        rdmsBossService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据客户ID查询对应的最终管理人信息
     * 通过客户ID获取该客户关联的最终管理人详细信息
     * 
     * @param customerId 客户ID，用于查询关联的最终管理人信息
     * @return 返回该客户对应的最终管理人信息，如果不存在则返回空的最终管理人对象
     */
    @PostMapping("/getBossByCustomerId/{customerId}")
    public ResponseDto<RdmsBossDto> listByCustomerId(@PathVariable String customerId) {
        ValidatorUtil.require(customerId, "客户ID");
        ResponseDto<RdmsBossDto> responseDto = new ResponseDto<>();
        RdmsBossDto bossByCustomerId = rdmsBossService.getBossByCustomerId(customerId);
        if (bossByCustomerId != null) {
            responseDto.setContent(bossByCustomerId);
        } else {
            responseDto.setContent(new RdmsBossDto());
        }
        return responseDto;
    }

    // 最终管理人只能由平台超管经 /system/rdms/customer/saveBoss 更换，机构侧不提供新增、修改与删除
}
