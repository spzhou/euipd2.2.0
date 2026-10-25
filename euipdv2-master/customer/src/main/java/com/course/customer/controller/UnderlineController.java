/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsCustomerUser;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsCustomerUserDto;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.RdmsOrgService;
import com.course.server.service.rdms.statistics.OrgTree;
import com.course.server.util.CopyUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/underline")
public class UnderlineController {
    public static final String BUSINESS_NAME = "下属员工";

    @Resource
    private RdmsOrgService rdmsOrgService;

    /**
     * 本人的下级：所负责部门及其全部下级部门中的人员（不含本人）。路径中的人员编号只在无登录上下文时使用。
     */
    @PostMapping("/getUnderline/{workerId}")
    public ResponseDto<List<RdmsCustomerUserDto>> getUnderline(@PathVariable String workerId) {
        workerId = TenantAccess.resolveMemberId(workerId);
        String customerId = TenantAccess.requireTenantId();
        OrgTree tree = rdmsOrgService.loadTree(customerId);
        List<RdmsCustomerUserDto> subordinates = new ArrayList<>();
        for (String id : tree.subordinates(workerId)) {
            RdmsCustomerUser user = tree.user(id);
            if (user != null) {
                subordinates.add(CopyUtil.copy(user, RdmsCustomerUserDto.class));
            }
        }
        ResponseDto<List<RdmsCustomerUserDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(subordinates);
        return responseDto;
    }
}
