/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RichDocRequest;
import com.course.server.dto.rdms.RichDocViewDto;
import com.course.server.service.rdms.rich.CbbRichDocService;
import com.course.server.service.rdms.rich.RichSimpleDocService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * CBB 内容文档（模块 CBB，实现方案第 9.2 节）。CBB 内容只经 CBB 定义工单与迭代建议修改，本接口只读。
 */
@RestController
@RequestMapping("/cbb/rich-doc")
public class CbbRichDocController {

    @Resource
    private CbbRichDocService cbbRichDocService;
    @Resource
    private RichSimpleDocService richSimpleDocService;

    @PostMapping("/get")
    public ResponseDto<RichDocViewDto> get(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(cbbRichDocService.read(req.getBizId(), req.getVersionId()));
    }

    @PostMapping("/get-versions")
    public ResponseDto<List<Map<String, Object>>> getVersions(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(cbbRichDocService.versions(req.getBizId()));
    }

    @PostMapping("/get-compare")
    public ResponseDto<List<Map<String, Object>>> getCompare(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(richSimpleDocService.compare(req.getFromVersionId(), req.getToVersionId()));
    }

    @PostMapping("/get-lineage")
    public ResponseDto<Map<String, Object>> getLineage(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(cbbRichDocService.lineage(req.getBizId(),
                Boolean.TRUE.equals(req.getIncludeWork()), req.depthOrDefault()));
    }
}
