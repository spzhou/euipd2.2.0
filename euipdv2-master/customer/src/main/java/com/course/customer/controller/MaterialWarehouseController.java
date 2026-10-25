/**
 * Author：周朔鹏
 * 编制时间：2026-10-09
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsMaterialWarehouse;
import com.course.server.dto.ResponseDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsMaterialManageService;
import com.course.server.service.rdms.RdmsMaterialWarehouseService;
import com.course.server.util.ValidatorUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/** 研发物料仓库维护，限物料管理员与最终管理人 */
@RestController
@RequestMapping("/material-warehouse")
public class MaterialWarehouseController {

    @Resource
    private RdmsMaterialWarehouseService rdmsMaterialWarehouseService;

    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;

    @PostMapping("/list/{customerId}")
    public ResponseDto<List<RdmsMaterialWarehouse>> list(@PathVariable String customerId) {
        String tenantId = requireSupervisor(customerId);
        ResponseDto<List<RdmsMaterialWarehouse>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsMaterialWarehouseService.list(tenantId));
        return responseDto;
    }

    @PostMapping("/save")
    public ResponseDto<String> save(@RequestBody RdmsMaterialWarehouse warehouse) {
        String tenantId = requireSupervisor(warehouse.getCustomerId());
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsMaterialWarehouseService.save(tenantId, warehouse));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/update-status")
    public ResponseDto<String> updateStatus(@RequestBody RdmsMaterialWarehouse warehouse) {
        ValidatorUtil.require(warehouse.getId(), "仓库Id");
        ValidatorUtil.require(warehouse.getStatus(), "仓库状态");
        String tenantId = requireSupervisor(warehouse.getCustomerId());
        rdmsMaterialWarehouseService.updateStatus(tenantId, warehouse.getId(), warehouse.getStatus());
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(warehouse.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private String requireSupervisor(String customerId) {
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        if (!rdmsMaterialManageService.isMaterialSupervisor(tenantId, TenantContext.getMembershipId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        return tenantId;
    }
}
