/**
 * Author：周朔鹏
 * 编制时间：2026-10-08
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RichDocRequest;
import com.course.server.dto.rdms.RichDocViewDto;
import com.course.server.service.rdms.rich.BizRichDocService;
import com.course.server.service.rdms.rich.RichDocConst;
import com.course.server.util.ValidatorUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 需求、质量问题、缺陷、工单提交属性的内容文档接口（实现方案第 9.2 节）。bizId 为业务记录 ID，工单提交属性为所属工单 ID。
 * 编辑范围按第 8.7 节阶段门禁，由服务层判定。
 */
public abstract class BizRichDocController {

    @Resource
    protected BizRichDocService bizRichDocService;

    protected abstract String bizType();

    @PostMapping("/get")
    public ResponseDto<RichDocViewDto> get(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(bizRichDocService.read(bizType(), bizId(req), req.getVersionId()));
    }

    @PostMapping("/save")
    public ResponseDto<Map<String, Object>> save(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(CharacterRichDocController.saveResult(bizRichDocService.save(bizType(),
                bizId(req), req.getBaseVersionId(), req.sectionMap(), req.getChangeNote(), req.getSource())));
    }

    @PostMapping("/get-versions")
    public ResponseDto<List<Map<String, Object>>> getVersions(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(bizRichDocService.versions(bizType(), bizId(req)));
    }

    @PostMapping("/get-compare")
    public ResponseDto<List<Map<String, Object>>> getCompare(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getFromVersionId(), "对比起始版本");
        ValidatorUtil.require(req.getToVersionId(), "对比目标版本");
        return CharacterRichDocController.ok(bizRichDocService.compare(bizType(), bizId(req), req.getFromVersionId(),
                req.getToVersionId()));
    }

    @PostMapping("/restore")
    public ResponseDto<Map<String, Object>> restore(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getVersionId(), "恢复版本");
        return CharacterRichDocController.ok(CharacterRichDocController.saveResult(
                bizRichDocService.restore(bizType(), bizId(req), req.getVersionId())));
    }

    @PostMapping("/edit-lock")
    public ResponseDto<Map<String, Object>> editLock(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(bizRichDocService.editLock(bizType(), bizId(req), req.getAction(),
                Boolean.TRUE.equals(req.getForce())));
    }

    @PostMapping("/draft/save")
    public ResponseDto<Map<String, Object>> saveDraft(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(bizRichDocService.saveDraft(bizType(), bizId(req), req.getBaseVersionId(),
                req.sectionMap()));
    }

    @PostMapping("/draft/get")
    public ResponseDto<Map<String, Object>> getDraft(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(bizRichDocService.getDraft(bizType(), bizId(req)));
    }

    @PostMapping("/get-lineage")
    public ResponseDto<Map<String, Object>> getLineage(@RequestBody RichDocRequest req) {
        return CharacterRichDocController.ok(bizRichDocService.lineage(bizType(), bizId(req),
                req.getIncludeWork() == null || req.getIncludeWork(), req.depthOrDefault()));
    }

    private static String bizId(RichDocRequest req) {
        ValidatorUtil.require(req.getBizId(), "业务记录");
        return req.getBizId();
    }

    @RestController
    @RequestMapping("/demand/rich-doc")
    public static class Demand extends BizRichDocController {
        @Override
        protected String bizType() {
            return RichDocConst.BIZ_DEMAND;
        }
    }

    @RestController
    @RequestMapping("/quality/rich-doc")
    public static class Quality extends BizRichDocController {
        @Override
        protected String bizType() {
            return RichDocConst.BIZ_QUALITY;
        }
    }

    @RestController
    @RequestMapping("/bugfeedback/rich-doc")
    public static class Bug extends BizRichDocController {
        @Override
        protected String bizType() {
            return RichDocConst.BIZ_BUG;
        }
    }

    @RestController
    @RequestMapping("/jobitem-property/rich-doc")
    public static class JobItemProperty extends BizRichDocController {
        @Override
        protected String bizType() {
            return RichDocConst.BIZ_JOBITEM_PROPERTY;
        }
    }
}
