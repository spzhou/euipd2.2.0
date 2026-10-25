/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsDepartment;
import com.course.server.dto.RdmsDepartmentTreeDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.dto.rdms.statistics.OrgNodeDto;
import com.course.server.dto.rdms.statistics.OrgRelationDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.RdmsCustomerUserService;
import com.course.server.service.rdms.RdmsDepartmentService;
import com.course.server.service.rdms.RdmsOrgService;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {
    private static final Logger LOG = LoggerFactory.getLogger(DepartmentController.class);
    public static final String BUSINESS_NAME = "员工";

    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsDepartmentService rdmsDepartmentService;
    @Resource
    private RdmsOrgService rdmsOrgService;

    /**
     * 列表查询
     */
    @PostMapping("/listDepartment/{customerId}")
    public ResponseDto listDepartment(@PathVariable String customerId) {
        customerId = TenantAccess.resolveCustomerId(customerId);
        ResponseDto responseDto = new ResponseDto();
        List<RdmsDepartmentDto> rdmsDepartmentDtos = rdmsDepartmentService.listDepartment(customerId);
        responseDto.setContent(rdmsDepartmentDtos);
        return responseDto;
    }

    @PostMapping("/getDepartmentMemberList/{departmentId}")
    public ResponseDto getDepartmentMemberList(@PathVariable String departmentId) {
        requireDepartment(departmentId);
        ResponseDto responseDto = new ResponseDto();
        List<RdmsCustomerUser> memberListByDepartmentId = rdmsCustomerUserService.getMemberListByDepartmentId(departmentId);
        responseDto.setContent(memberListByDepartmentId);
        return responseDto;
    }

    /**
     * 新建部门，并把 memberIdList 中的人员归入新部门
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto save(@RequestBody RdmsDepartmentDto departmentDto) throws Exception {
        ResponseDto responseDto = new ResponseDto();
        validate(departmentDto);
        String customerId = TenantAccess.resolveCustomerId(departmentDto.getCustomerId());
        departmentDto.setCustomerId(customerId);
        requireMember(customerId, departmentDto.getManagerId());
        rdmsDepartmentService.assertParentValid(customerId, departmentDto.getId(), departmentDto.getParent());

        String departmentId = rdmsDepartmentService.save(departmentDto);
        departmentDto.setId(departmentId);
        rdmsDepartmentService.assignMembers(customerId, departmentId, departmentDto.getMemberIdList());
        rdmsOrgService.bumpVersion(customerId);

        responseDto.setContent(departmentDto);
        return responseDto;
    }

    /**
     * 编辑部门。memberIdList 非空时成员按整表替换，移出者改为未分配；为 null 时不改成员
     */
    @PostMapping("/edit")
    @Transactional
    public ResponseDto edit(@RequestBody RdmsDepartmentDto departmentDto) throws Exception {
        ResponseDto responseDto = new ResponseDto();
        validate(departmentDto);
        RdmsDepartment existing = requireDepartment(departmentDto.getId());
        String customerId = existing.getCustomerId();
        departmentDto.setCustomerId(customerId);
        departmentDto.setFixed(existing.getFixed());
        departmentDto.setCreateTime(existing.getCreateTime());
        if (Integer.valueOf(1).equals(existing.getFixed())) {
            departmentDto.setDepartName(existing.getDepartName());
            departmentDto.setName(existing.getDepartName());
            departmentDto.setParent(existing.getParent());
        }
        requireMember(customerId, departmentDto.getManagerId());
        rdmsDepartmentService.assertParentValid(customerId, departmentDto.getId(), departmentDto.getParent());

        rdmsDepartmentService.edit(departmentDto);
        if (departmentDto.getMemberIdList() != null) {
            rdmsDepartmentService.replaceMembers(customerId, departmentDto.getId(), departmentDto.getMemberIdList());
        }
        rdmsOrgService.bumpVersion(customerId);

        responseDto.setContent(departmentDto);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/get-departments")
    public ResponseDto getDepartments(@RequestBody RdmsResponseDto respDto) {
        ResponseDto responseDto = new ResponseDto();
        RdmsDepartmentTreeDto departmentsTree = rdmsDepartmentService.getDepartmentsTree(TenantAccess.resolveCustomerId(respDto.getCustomerId()));
        responseDto.setContent(departmentsTree);
        return responseDto;
    }

    /** 部门树：负责人姓名、直属人数、含下级人数，末尾附“未分配”节点 */
    @PostMapping("/getOrgTree")
    public ResponseDto<OrgNodeDto> getOrgTree() {
        ResponseDto<OrgNodeDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsOrgService.getTree(TenantAccess.requireTenantId()));
        return responseDto;
    }

    @PostMapping("/listUnassigned")
    public ResponseDto<List<RdmsCustomerUser>> listUnassigned() {
        ResponseDto<List<RdmsCustomerUser>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsOrgService.listUnassigned(TenantAccess.requireTenantId()));
        return responseDto;
    }

    /** 汇报关系：上级链与直接下级 */
    @PostMapping("/getSuperior/{customerUserId}")
    public ResponseDto<OrgRelationDto> getSuperior(@PathVariable String customerUserId) {
        String customerId = TenantAccess.requireTenantId();
        OrgRelationDto relation = rdmsOrgService.getRelation(customerId, customerUserId);
        if (relation.getPerson() == null) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        ResponseDto<OrgRelationDto> responseDto = new ResponseDto<>();
        responseDto.setContent(relation);
        return responseDto;
    }

    /** 把人员批量归入部门：id 为部门编号，memberIdList 为人员编号 */
    @PostMapping("/assignMembers")
    @Transactional
    public ResponseDto assignMembers(@RequestBody RdmsDepartmentDto departmentDto) {
        ValidatorUtil.require(departmentDto.getId(), "部门");
        RdmsDepartment department = requireDepartment(departmentDto.getId());
        rdmsDepartmentService.assignMembers(department.getCustomerId(), department.getId(), departmentDto.getMemberIdList());
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(department.getId());
        return responseDto;
    }

    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsDepartmentService.delete(id);
        return responseDto;
    }

    private RdmsDepartment requireDepartment(String departmentId) {
        RdmsDepartment department = rdmsDepartmentService.selectByPrimaryKey(departmentId);
        if (department == null || Integer.valueOf(1).equals(department.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(department.getCustomerId());
        return department;
    }

    private void requireMember(String customerId, String customerUserId) {
        RdmsCustomerUser user = rdmsCustomerUserService.selectByPrimaryKey(customerUserId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted()) || !customerId.equals(user.getCustomerId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
    }

    private void validate(RdmsDepartmentDto departmentDto) {
        ValidatorUtil.require(departmentDto.getDepartName(), "部门名称");
        ValidatorUtil.require(departmentDto.getManagerId(), "部门负责人ID");
        ValidatorUtil.require(departmentDto.getParent(), "上级部门");
        ValidatorUtil.require(departmentDto.getDepartDescription(), "部门描述");

        ValidatorUtil.length(departmentDto.getDepartName(), "部门名称", 2, 20);
        ValidatorUtil.length(departmentDto.getManagerId(), "部门负责人ID", 8, 8);
        ValidatorUtil.length(departmentDto.getParent(), "上级部门", 8, 8);
        ValidatorUtil.length(departmentDto.getDepartDescription(), "部门描述", 2, 100);
    }
}
