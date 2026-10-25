/**
 * Author：周朔鹏
 * 编制时间：2026-10-05
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.domain.RdmsCharacter;
import com.course.server.domain.RdmsRichBranch;
import com.course.server.domain.RdmsRichDocVersion;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RichDocRequest;
import com.course.server.dto.rdms.RichDocViewDto;
import com.course.server.service.rdms.revision.CharacterRevisionFlowService;
import com.course.server.service.rdms.rich.CharacterRichDocService;
import com.course.server.service.rdms.rich.RichBranchService;
import com.course.server.service.rdms.rich.RichDocService;
import com.course.server.util.ValidatorUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 特性定义内容文档接口（实现方案第 10 节）。
 */
@RestController
@RequestMapping("/character/rich-doc")
public class CharacterRichDocController {

    @Resource
    private CharacterRichDocService characterRichDocService;
    @Resource
    private CharacterRevisionFlowService characterRevisionFlowService;

    @PostMapping("/get")
    public ResponseDto<RichDocViewDto> get(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.read(req.getCharacterId(), req.getVersionId(), req.getBranchId(),
                req.getPinType(), req.getRefId()));
    }

    @PostMapping("/get-summaries")
    public ResponseDto<Map<String, Map<String, Object>>> getSummaries(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.summaries(req.getCharacterIds(), req.getSectionCodes()));
    }

    @PostMapping("/save")
    public ResponseDto<Map<String, Object>> save(@RequestBody RichDocRequest req) {
        return ok(saveResult(characterRichDocService.save(req.getCharacterId(), req.getBranchId(),
                req.getBaseVersionId(), req.sectionMap(), req.getChangeNote(), req.getSource())));
    }

    @PostMapping("/get-versions")
    public ResponseDto<List<Map<String, Object>>> getVersions(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.versions(req.getCharacterId(), req.getDevVersion()));
    }

    @PostMapping("/get-compare")
    public ResponseDto<List<Map<String, Object>>> getCompare(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.compare(req.getFromVersionId(), req.getToVersionId()));
    }

    @PostMapping("/restore")
    public ResponseDto<Map<String, Object>> restore(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getVersionId(), "恢复版本");
        return ok(saveResult(characterRichDocService.restore(req.getCharacterId(), req.getVersionId(),
                req.getBranchId())));
    }

    @PostMapping("/edit-lock")
    public ResponseDto<Map<String, Object>> editLock(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.editLock(req.getCharacterId(), req.getBranchId(), req.getAction()));
    }

    @PostMapping("/draft/save")
    public ResponseDto<Map<String, Object>> saveDraft(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.saveDraft(req.getCharacterId(), req.getBranchId(), req.getBaseVersionId(),
                req.sectionMap()));
    }

    @PostMapping("/draft/get")
    public ResponseDto<Map<String, Object>> getDraft(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.getDraft(req.getCharacterId(), req.getBranchId()));
    }

    @PostMapping("/branch/create")
    public ResponseDto<Map<String, Object>> createBranch(@RequestBody RichDocRequest req) {
        RdmsRichBranch b = characterRichDocService.createBranch(req.getCharacterId(), req.getName(), req.getReason());
        return ok(characterRichDocService.branchRowOf(b));
    }

    @PostMapping("/get-branches")
    public ResponseDto<List<Map<String, Object>>> getBranches(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.listBranches(req.getCharacterId(), req.getStatus()));
    }

    @PostMapping("/branch/abandon")
    public ResponseDto<Map<String, Object>> abandonBranch(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getBranchId(), "分支");
        RdmsRichBranch b = characterRichDocService.abandonBranch(req.getBranchId(), req.getReason());
        return ok(characterRichDocService.branchRowOf(b));
    }

    @PostMapping("/branch/rebase")
    public ResponseDto<Map<String, Object>> rebase(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getBranchId(), "分支");
        Map<String, String> resolved = req.sectionMap();
        RichBranchService.RebaseResult r = characterRichDocService.rebase(req.getBranchId(),
                resolved.isEmpty() ? null : resolved);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("conflicts", r.getConflicts());
        out.put("version", versionInfo(r.getVersion()));
        return ok(out);
    }

    @PostMapping("/get-merge-preview")
    public ResponseDto<Map<String, Object>> getMergePreview(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getBranchId(), "分支");
        return ok(characterRichDocService.mergePreview(req.getBranchId()));
    }

    @PostMapping("/branch/approve-merge")
    public ResponseDto<Map<String, Object>> approveMerge(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getBranchId(), "分支");
        ValidatorUtil.require(req.getReviewedVersionId(), "评审通过的版本");
        return ok(versionInfo(characterRichDocService.merge(req.getBranchId(), req.getReviewedVersionId(),
                req.getConfirmNote(), req.getRepinJobitemIds())));
    }

    /** 派发人确认修订：characterId 为修订编辑记录。 */
    @PostMapping("/revision/approve-confirm")
    public ResponseDto<Map<String, Object>> confirmRevision(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getCharacterId(), "修订记录");
        RdmsCharacter original = characterRevisionFlowService.confirmRevision(req.getCharacterId(), req.getConfirmNote());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("characterId", original.getId());
        out.put("iterationVersion", original.getIterationVersion());
        return ok(out);
    }

    /** 派发人退回修订：characterId 为修订编辑记录。 */
    @PostMapping("/revision/approve-return")
    public ResponseDto<Boolean> returnRevision(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getCharacterId(), "修订记录");
        ValidatorUtil.require(req.getReason(), "退回原因");
        characterRevisionFlowService.returnRevision(req.getCharacterId(), req.getReason());
        return ok(Boolean.TRUE);
    }

    /** 纳入项目：返回纳入分支，前端据此发起变更评审。 */
    @PostMapping("/adoption/create")
    public ResponseDto<Map<String, Object>> createAdoption(@RequestBody RichDocRequest req) {
        ValidatorUtil.require(req.getCharacterId(), "特性");
        return ok(characterRichDocService.startAdoption(req.getCharacterId()));
    }

    @PostMapping("/get-lineage")
    public ResponseDto<Map<String, Object>> getLineage(@RequestBody RichDocRequest req) {
        return ok(characterRichDocService.lineage(req.getCharacterId(), req.getVersionId(),
                req.getIncludeWork() == null || req.getIncludeWork(), req.depthOrDefault()));
    }

    static Map<String, Object> saveResult(RichDocService.SaveResult r) {
        Map<String, Object> out = versionInfo(r.getVersion());
        out.put("changed", r.isChanged());
        out.put("changedSections", r.getChangedSections());
        return out;
    }

    static Map<String, Object> versionInfo(RdmsRichDocVersion v) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (v != null) {
            out.put("versionId", v.getId());
            out.put("versionLabel", v.getVersionLabel());
            out.put("kind", v.getKind());
            out.put("lineId", v.getLineId());
            out.put("branchId", v.getBranchId());
        }
        return out;
    }

    static <T> ResponseDto<T> ok(T content) {
        ResponseDto<T> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }
}
