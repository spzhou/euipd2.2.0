/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsProjectManager;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsCustomerUserDto;
import com.course.server.dto.rdms.RdmsProjectManagerDto;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.RdmsProjectManagerService;
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
@RequestMapping("/project-manager")
public class ProjectManagerController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectManagerController.class);
    public static final String BUSINESS_NAME = "项目经理";

    @Resource
    private RdmsProjectManagerService rdmsProjectManagerService;

    /**
     * 列表查询公司所有员工
     */
    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsProjectManagerDto>> list(@RequestBody PageDto<RdmsProjectManagerDto> pageDto) {
        ResponseDto<PageDto<RdmsProjectManagerDto>> responseDto = new ResponseDto<>();
        rdmsProjectManagerService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }


    /**
     * 列表查询
     */
    @PostMapping("/listByCustomerId/{customerId}")
    public ResponseDto<List<String>> list(@PathVariable String customerId) {
        ResponseDto<List<String>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        List<String> rdmsProjectManagerDtos = rdmsProjectManagerService.listByCustomerId(tenantId);
        responseDto.setContent(rdmsProjectManagerDtos);
        return responseDto;
    }

    /**
     *
     */
    @PostMapping("/getProjectManagerList/{customerId}")
    public ResponseDto<List<RdmsCustomerUserDto>> getProjectManagerList(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUserDto>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        List<RdmsCustomerUser> customerUsers = rdmsProjectManagerService.getProjectManagerListByCustomerId(tenantId);
        List<RdmsCustomerUserDto> rdmsCustomerUserDtos = CopyUtil.copyList(customerUsers, RdmsCustomerUserDto.class);
        responseDto.setContent(rdmsCustomerUserDtos);
        return responseDto;
    }


    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsProjectManagerDto> save(@RequestBody RdmsProjectManagerDto rdmsProjectManagerDto) {
        ResponseDto<RdmsProjectManagerDto> responseDto = new ResponseDto<>();

        String tenantId = TenantAccess.resolveCustomerId(rdmsProjectManagerDto.getCustomerId());
        if (!StringUtils.hasText(tenantId)) {
            tenantId = rdmsProjectManagerDto.getCustomerId();
        }
        rdmsProjectManagerDto.setCustomerId(tenantId);
        List<String> ids = rdmsProjectManagerDto.getProjectManagerIds() == null
                ? Collections.emptyList() : rdmsProjectManagerDto.getProjectManagerIds();
        rdmsProjectManagerDto.setProjectManagerIds(ids);

        String jsonString = JSON.toJSONString(ids);
        RdmsProjectManager rdmsProjectManager = CopyUtil.copy(rdmsProjectManagerDto, RdmsProjectManager.class);
        rdmsProjectManager.setCustomerId(tenantId);
        rdmsProjectManager.setProjectManagerIdListStr(jsonString);
        rdmsProjectManagerService.save(rdmsProjectManager);

        responseDto.setContent(rdmsProjectManagerDto);
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
        rdmsProjectManagerService.delete(id);
        return responseDto;
    }

}
