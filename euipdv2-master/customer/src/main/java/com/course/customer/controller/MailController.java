/**
 * Author：周朔鹏
 * 编制时间：2026-10-12
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.mail.MailAccountDto;
import com.course.server.dto.rdms.mail.MailAttachableDto;
import com.course.server.dto.rdms.mail.MailBoxItemDto;
import com.course.server.dto.rdms.mail.MailBoxOpDto;
import com.course.server.dto.rdms.mail.MailDetailDto;
import com.course.server.dto.rdms.mail.MailListQuery;
import com.course.server.dto.rdms.mail.MailPage;
import com.course.server.dto.rdms.mail.MailRichVersionDto;
import com.course.server.dto.rdms.mail.MailSendDto;
import com.course.server.service.rdms.mail.MailAccountService;
import com.course.server.service.rdms.mail.MailActor;
import com.course.server.service.rdms.mail.MailAttachableService;
import com.course.server.service.rdms.mail.MailAttachmentService;
import com.course.server.service.rdms.mail.MailContactService;
import com.course.server.service.rdms.mail.MailService;
import com.course.server.service.util.IpUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 内部邮件。机构与成员取自令牌，所有操作只作用于本人的邮箱条目。
 */
@RestController
@RequestMapping("/mail")
public class MailController {
    public static final String BUSINESS_NAME = "内部邮件";

    @Resource
    private MailService mailService;
    @Resource
    private MailAccountService mailAccountService;
    @Resource
    private MailContactService mailContactService;
    @Resource
    private MailAttachableService mailAttachableService;
    @Resource
    private MailAttachmentService mailAttachmentService;
    @Resource
    private IpUtils ipUtils;

    @GetMapping("/account/getSelf")
    public ResponseDto<MailAccountDto> getSelf() {
        return ok(mailAccountService.getSelf(MailActor.current()));
    }

    @PostMapping("/account/update")
    public ResponseDto<Boolean> updateAccount(@RequestBody Map<String, String> body) {
        mailAccountService.updateSignature(MailActor.current(), body == null ? null : body.get("signature"));
        return ok(true);
    }

    @PostMapping("/contact/list")
    public ResponseDto<Map<String, Object>> contacts(@RequestBody(required = false) Map<String, String> body) {
        return ok(mailContactService.list(MailActor.current(), body == null ? null : body.get("keyword")));
    }

    @PostMapping("/box/list")
    public ResponseDto<MailPage<MailBoxItemDto>> list(@RequestBody(required = false) MailListQuery query) {
        return ok(mailService.list(MailActor.current(), query));
    }

    @GetMapping("/box/count")
    public ResponseDto<Map<String, Integer>> count() {
        return ok(mailService.unreadCount(MailActor.current()));
    }

    @GetMapping("/message/get/{boxId}")
    public ResponseDto<MailDetailDto> get(@PathVariable String boxId) {
        return ok(mailService.get(MailActor.current(), boxId));
    }

    @PostMapping("/draft/save")
    public ResponseDto<Map<String, String>> saveDraft(@RequestBody MailSendDto body) {
        return ok(mailService.saveDraft(MailActor.current(), body));
    }

    @PostMapping("/message/send")
    public ResponseDto<Map<String, String>> send(@RequestBody MailSendDto body) {
        return ok(mailService.send(MailActor.current(), body));
    }

    @PostMapping("/box/mark")
    public ResponseDto<Integer> mark(@RequestBody MailBoxOpDto body) {
        return ok(mailService.mark(MailActor.current(), body));
    }

    @PostMapping("/box/move")
    public ResponseDto<Integer> move(@RequestBody MailBoxOpDto body) {
        return ok(mailService.move(MailActor.current(), body));
    }

    @PostMapping("/box/delete")
    public ResponseDto<Integer> delete(@RequestBody MailBoxOpDto body) {
        return ok(mailService.deleteForever(MailActor.current(), body));
    }

    @PostMapping("/attachable/list")
    public ResponseDto<MailPage<MailAttachableDto>> attachable(@RequestBody Map<String, Object> body) {
        return ok(mailAttachableService.list(MailActor.current(), str(body, "source"), str(body, "keyword"),
                integer(body, "page"), integer(body, "size")));
    }

    @PostMapping("/attachable/rich-version/list")
    public ResponseDto<MailPage<MailRichVersionDto>> richVersions(@RequestBody Map<String, Object> body) {
        return ok(mailAttachableService.listEditedRichVersions(MailActor.current(), str(body, "keyword"),
                integer(body, "page"), integer(body, "size")));
    }

    @PostMapping("/attachment/signed-url")
    public ResponseDto<String> signedUrl(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        boolean preview = Boolean.parseBoolean(str(body, "preview"));
        return ok(mailAttachmentService.signedUrl(MailActor.current(), str(body, "boxId"), str(body, "attachmentId"),
                ipUtils.getIpAddress(request), preview));
    }

    private static <T> ResponseDto<T> ok(T content) {
        ResponseDto<T> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }

    private static String str(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static Integer integer(Map<String, Object> body, String key) {
        Object v = body == null ? null : body.get(key);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return v == null ? null : Integer.valueOf(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
