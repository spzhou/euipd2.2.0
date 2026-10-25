/**
 * Author：周朔鹏
 * 编制时间：2026-10-13
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.ResponseDto;
import com.course.server.service.rdms.BaselineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 基线查询。基线由立项批准、评审通过与产品发布生成，不提供新增与修改。
 */
@RestController
@RequestMapping("/project/baseline")
public class BaselineController {

    @Resource
    private BaselineService baselineService;

    /** body: ownerType（PROJECT/CHARACTER/SUBPROJECT/RELEASE）、ownerId；PROJECT 时列出该项目下全部基线。 */
    @PostMapping("/list")
    public ResponseDto<List<Map<String, Object>>> list(@RequestBody Map<String, String> body) {
        ResponseDto<List<Map<String, Object>>> responseDto = new ResponseDto<>();
        responseDto.setContent(baselineService.list(body == null ? null : body.get("ownerType"),
                body == null ? null : body.get("ownerId")));
        return responseDto;
    }

    @GetMapping("/get/{id}")
    public ResponseDto<Map<String, Object>> get(@PathVariable String id) {
        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>();
        responseDto.setContent(baselineService.get(id));
        return responseDto;
    }
}
