/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsFileApply;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsFileApplyDto;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsFileApplyService;
import com.course.server.util.ValidatorUtil;
import com.course.customer.flow.FileFlowBridge;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 档案申请的引擎入口。对外地址仍是 /file/rdms，由 file 服务在开关命中时转到这里。
 * 路径归文件模块的更新权限，与原申请、同意、驳回接口一致。
 */
@RestController
@RequestMapping("/file-apply")
public class FileApplyFlowController {

    @Resource
    private FileFlowBridge fileFlowBridge;

    @Resource
    private RdmsFileApplyService rdmsFileApplyService;

    @PostMapping("/route")
    public ResponseDto<String> route(@RequestBody RdmsFileApplyDto dto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        String customerId = customerId(dto == null ? null : dto.getCustomerId());
        responseDto.setContent(fileFlowBridge.engineForNew(customerId) ? "ENGINE" : "LEGACY");
        return responseDto;
    }

    @PostMapping("/apply")
    public ResponseDto<RdmsFileApplyDto> apply(@RequestBody RdmsFileApplyDto dto) {
        ResponseDto<RdmsFileApplyDto> responseDto = new ResponseDto<>();
        if (!fileFlowBridge.engineForNew(customerId(dto.getCustomerId()))) {
            responseDto.setMessage(FileFlowBridge.LEGACY);
            return responseDto;
        }
        fileFlowBridge.submit(dto);
        responseDto.setContent(dto);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/agree/{applyId}")
    public ResponseDto<String> agree(@PathVariable String applyId) {
        return decide(applyId, true);
    }

    @PostMapping("/reject/{applyId}")
    public ResponseDto<String> reject(@PathVariable String applyId) {
        return decide(applyId, false);
    }

    private ResponseDto<String> decide(String applyId, boolean approved) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsFileApply current = rdmsFileApplyService.selectByPrimaryKey(applyId);
        if (current == null || !fileFlowBridge.engineForExisting(customerId(current.getCustomerId()), applyId)) {
            responseDto.setMessage(FileFlowBridge.LEGACY);
            return responseDto;
        }
        current = rdmsFileApplyService.requirePendingApply(applyId);
        String operatorId = TenantContext.getMembershipId();
        ValidatorUtil.require(operatorId, "操作者");
        if (approved) {
            fileFlowBridge.agree(current, operatorId);
        } else {
            fileFlowBridge.reject(current, operatorId);
        }
        responseDto.setContent(applyId);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private String customerId(String fallback) {
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            customerId = fallback;
        }
        return customerId;
    }
}
