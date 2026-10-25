/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsGanttLinks;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.GanttIssueResultDto;
import com.course.server.dto.rdms.GanttLoadPreviewDto;
import com.course.server.dto.rdms.RdmsGanttDto;
import com.course.server.dto.rdms.RdmsGanttLinksDto;
import com.course.server.dto.rdms.RdmsJobItemGanttDto;
import com.course.server.enums.rdms.OperateTypeEnum;
import com.course.server.service.rdms.RdmsGanttService;
import com.course.server.service.rdms.RdmsJobItemGanttService;
import com.course.server.service.rdms.RdmsManhourService;
import com.course.server.util.CopyUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 子项目甘特图与计划编制。门禁与校验在服务层：同机构、操作人、计划状态、开始日、连线端点。
 */
@RestController
@RequestMapping("/gantt")
public class GanttController {
    public static final String BUSINESS_NAME = "Gantt图";

    @Resource
    private RdmsGanttService rdmsGanttService;
    @Resource
    private RdmsJobItemGanttService rdmsJobItemGanttService;
    @Resource
    private RdmsManhourService rdmsManhourService;

    @PostMapping("/saveLink")
    public ResponseDto<String> saveLink(@RequestBody RdmsGanttLinksDto rdmsGanttLinksDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsGanttService.saveLink(CopyUtil.copy(rdmsGanttLinksDto, RdmsGanttLinks.class)));
        return responseDto;
    }

    @PostMapping("/delLink/{id}")
    public ResponseDto<String> delLink(@PathVariable String id) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        rdmsGanttService.deleteLink(id);
        responseDto.setContent(id);
        return responseDto;
    }

    @PostMapping("/getGanttLinkListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsGanttLinksDto>> getGanttLinkListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsGanttLinksDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(CopyUtil.copyList(rdmsGanttService.getLinkListBySubprojectId(subprojectId),
                RdmsGanttLinksDto.class));
        return responseDto;
    }

    @PostMapping("/saveTaskPlanJobitem")
    public ResponseDto<String> saveTaskPlanJobitem(@RequestBody RdmsJobItemGanttDto jobItemGanttDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsJobItemGanttService.saveJobitemToPlan(jobItemGanttDto));
        return responseDto;
    }

    @PostMapping("/updatePlanSchedule")
    public ResponseDto<String> updatePlanSchedule(@RequestBody RdmsJobItemGanttDto jobItemGanttDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        rdmsJobItemGanttService.updatePlanSchedule(
                jobItemGanttDto.getId(),
                jobItemGanttDto.getPlanStartTime(),
                jobItemGanttDto.getDuration());
        responseDto.setContent(jobItemGanttDto.getId());
        return responseDto;
    }

    @PostMapping("/deletePlanJobitem/{id}")
    public ResponseDto<String> deletePlanJobitem(@PathVariable String id) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        rdmsJobItemGanttService.delete(id);
        responseDto.setContent(id);
        return responseDto;
    }

    @PostMapping("/issuePlanTaskBySubprojectId/{subprojectId}")
    public ResponseDto<GanttIssueResultDto> issuePlanTaskBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<GanttIssueResultDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsJobItemGanttService.issuePlanTaskBySubprojectId(subprojectId));
        return responseDto;
    }

    @PostMapping("/issuePlanTaskNow/{planId}")
    public ResponseDto<String> issuePlanTaskNow(@PathVariable String planId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsJobItemGanttService.issuePlanTaskNow(planId));
        return responseDto;
    }

    @PostMapping("/plan/loadPreview")
    public ResponseDto<GanttLoadPreviewDto> loadPreview(@RequestBody RdmsJobItemGanttDto jobItemGanttDto) {
        ResponseDto<GanttLoadPreviewDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsJobItemGanttService.loadPreview(jobItemGanttDto));
        return responseDto;
    }

    @PostMapping("/getGanttListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsGanttDto>> getGanttListBySubprojectId(@PathVariable String subprojectId) {
        List<RdmsGanttDto> ganttList = rdmsGanttService.getGanttListBySubprojectId(subprojectId);
        if (!CollectionUtils.isEmpty(ganttList)) {
            for (RdmsGanttDto item : ganttList) {
                if (item.getManhour() != null && item.getManhour() > 0) {
                    item.setManhour(rdmsManhourService.transformToWorkerManhour(item.getManhour(), item.getExecutorId(),
                            item.getCustomerId(), OperateTypeEnum.DEVELOP));
                }
            }
        }
        ResponseDto<List<RdmsGanttDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(ganttList);
        return responseDto;
    }

    @PostMapping("/getJobItemGanttById/{id}")
    public ResponseDto<RdmsJobItemGanttDto> getJobItemGanttById(@PathVariable String id) {
        ResponseDto<RdmsJobItemGanttDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsJobItemGanttService.getPlanForEdit(id));
        return responseDto;
    }
}
