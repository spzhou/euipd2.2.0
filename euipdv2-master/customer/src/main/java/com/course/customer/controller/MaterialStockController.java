/**
 * Author：周朔鹏
 * 编制时间：2026-10-09
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsMaterialStockLog;
import com.course.server.domain.RdmsMaterialWarehouse;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsMaterialStockDto;
import com.course.server.dto.rdms.RdmsMaterialStockImportDto;
import com.course.server.dto.rdms.RdmsMaterialStockLogDto;
import com.course.server.dto.rdms.RdmsMaterialStockLogQueryDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsCustomerUserService;
import com.course.server.service.rdms.RdmsMaterialManageService;
import com.course.server.service.rdms.RdmsMaterialStockService;
import com.course.server.service.rdms.RdmsMaterialWarehouseService;
import com.course.server.util.CopyUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 研发物料库存：入库录入、Excel 导入、库存调整与台账查询，限物料管理员与最终管理人 */
@RestController
@RequestMapping("/material-stock")
public class MaterialStockController {

    @Resource
    private RdmsMaterialStockService rdmsMaterialStockService;

    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;

    @Resource
    private RdmsMaterialWarehouseService rdmsMaterialWarehouseService;

    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;

    @PostMapping("/list/{customerId}")
    public ResponseDto<List<RdmsMaterialStockDto>> list(@PathVariable String customerId) {
        String tenantId = requireSupervisor(customerId);
        ResponseDto<List<RdmsMaterialStockDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsMaterialStockService.listView(tenantId));
        return responseDto;
    }

    @PostMapping("/inbound")
    public ResponseDto<String> inbound(@RequestBody RdmsMaterialStockDto stockDto) {
        stockDto.setCustomerId(requireSupervisor(stockDto.getCustomerId()));
        stockDto.setOperatorId(TenantContext.getMembershipId());
        rdmsMaterialStockService.inbound(stockDto);
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setMessage("入库成功");
        return responseDto;
    }

    /** 路径不得含 /import：网关会推断为 IMPORT 动作，而库存模块只授予增删改查与审批 */
    @PostMapping("/inbound-batch")
    public ResponseDto<String> importRows(@RequestBody RdmsMaterialStockImportDto importDto) {
        String tenantId = requireSupervisor(importDto.getCustomerId());
        String batch = rdmsMaterialStockService.importRows(tenantId, TenantContext.getMembershipId(), importDto.getRows());
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(batch);
        responseDto.setMessage("导入成功，共 " + importDto.getRows().size() + " 行");
        return responseDto;
    }

    @PostMapping("/adjust")
    public ResponseDto<String> adjust(@RequestBody RdmsMaterialStockDto stockDto) {
        String tenantId = requireSupervisor(stockDto.getCustomerId());
        rdmsMaterialStockService.adjust(tenantId, TenantContext.getMembershipId(), stockDto);
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(stockDto.getId());
        responseDto.setMessage("调整成功");
        return responseDto;
    }

    @PostMapping("/logs")
    public ResponseDto<List<RdmsMaterialStockLogDto>> logs(@RequestBody RdmsMaterialStockLogQueryDto query) {
        String tenantId = requireSupervisor(query.getCustomerId());
        query.setCustomerId(tenantId);
        List<RdmsMaterialStockLog> logs = rdmsMaterialStockService.listLogs(query);
        Map<String, String> warehouseNames = new HashMap<>();
        for (RdmsMaterialWarehouse warehouse : rdmsMaterialWarehouseService.list(tenantId)) {
            warehouseNames.put(warehouse.getCode(), warehouse.getName());
        }
        Map<String, String> operatorNames = new HashMap<>();
        List<RdmsMaterialStockLogDto> rows = CopyUtil.copyList(logs, RdmsMaterialStockLogDto.class);
        for (RdmsMaterialStockLogDto row : rows) {
            row.setWarehouseName(warehouseNames.get(row.getWarehouseCode()));
            if (row.getOperatorId() != null) {
                row.setOperatorName(operatorNames.computeIfAbsent(row.getOperatorId(), id -> {
                    RdmsCustomerUser user = rdmsCustomerUserService.selectByPrimaryKey(id);
                    return user == null ? id : user.getTrueName();
                }));
            }
        }
        ResponseDto<List<RdmsMaterialStockLogDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rows);
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
