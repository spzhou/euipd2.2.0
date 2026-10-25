/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsSystemManager;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsCustomerUserDto;
import com.course.server.dto.rdms.RdmsSystemManagerDto;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.RdmsSystemManagerService;
import com.course.server.util.CopyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/system-manager")
public class SystemManagerController {
    private static final Logger LOG = LoggerFactory.getLogger(SystemManagerController.class);
    public static final String BUSINESS_NAME = "系统经理";

    @Resource
    private RdmsSystemManagerService rdmsSystemManagerService;

    /**
     * 列表查询公司所有员工
     */
    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsSystemManagerDto>> list(@RequestBody PageDto<RdmsSystemManagerDto> pageDto) {
        ResponseDto<PageDto<RdmsSystemManagerDto>> responseDto = new ResponseDto<>();
        rdmsSystemManagerService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/listByCustomerId/{customerId}")
    public ResponseDto<List<String>> listByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<String>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        List<String> rdmsSystemManagerDtos = rdmsSystemManagerService.listByCustomerId(tenantId);
        responseDto.setContent(rdmsSystemManagerDtos);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/getListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsCustomerUserDto>> getListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUserDto>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        List<RdmsCustomerUserDto> systemManagers = rdmsSystemManagerService.getListByCustomerId(tenantId);
        responseDto.setContent(systemManagers);
        return responseDto;
    }

    @PostMapping("/listSystemManagerByCustomerId/{customerId}")
    public ResponseDto<List<RdmsCustomerUser>> listSystemManagerByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUser>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        List<RdmsCustomerUser> rdmsCustomerUsers = rdmsSystemManagerService.listSystemManagerByCustomerId(tenantId);
        responseDto.setContent(rdmsCustomerUsers);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsSystemManagerDto> save(@RequestBody RdmsSystemManagerDto rdmsSystemManagerDto) {
        ResponseDto<RdmsSystemManagerDto> responseDto = new ResponseDto<>();

        String tenantId = TenantAccess.resolveCustomerId(rdmsSystemManagerDto.getCustomerId());
        if (!StringUtils.hasText(tenantId)) {
            tenantId = rdmsSystemManagerDto.getCustomerId();
        }
        rdmsSystemManagerDto.setCustomerId(tenantId);
        List<String> ids = rdmsSystemManagerDto.getSystemManagerIds() == null
                ? Collections.emptyList() : rdmsSystemManagerDto.getSystemManagerIds();
        rdmsSystemManagerDto.setSystemManagerIds(ids);

        String jsonString = JSON.toJSONString(ids);
        RdmsSystemManager rdmsSystemManager = CopyUtil.copy(rdmsSystemManagerDto, RdmsSystemManager.class);
        rdmsSystemManager.setCustomerId(tenantId);
        rdmsSystemManager.setSystemManagerIdListStr(jsonString);
        rdmsSystemManagerService.save(rdmsSystemManager);

        responseDto.setContent(rdmsSystemManagerDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsSystemManagerService.delete(id);
        return responseDto;
    }

}
