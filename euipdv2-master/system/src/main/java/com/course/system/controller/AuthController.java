/**
 * Author：周朔鹏
 * 编制时间：2026-08-01
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.controller;

import com.course.server.dto.LoginUserDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsUserDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.SecurityConstants;
import com.course.system.service.AuthApplicationService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    public static final String BUSINESS_NAME = "登录认证";

    @Resource
    private AuthApplicationService authApplicationService;

    @PostMapping("/login")
    public ResponseDto login(@RequestBody RdmsUserDto userDto, HttpServletRequest request) {
        LoginUserDto loginUserDto = authApplicationService.login(userDto, request);
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(loginUserDto);
        responseDto.setCode("200");
        return responseDto;
    }

    @PostMapping("/refresh")
    public ResponseDto refresh(@RequestBody Map<String, String> body) {
        LoginUserDto dto = authApplicationService.refresh(body == null ? null : body.get("refreshToken"));
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(dto);
        responseDto.setCode("200");
        return responseDto;
    }

    @PostMapping("/logout")
    public ResponseDto logout(HttpServletRequest request, @RequestBody(required = false) Map<String, String> body) {
        String auth = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        String refresh = body == null ? null : body.get("refreshToken");
        authApplicationService.logout(auth, refresh);
        ResponseDto responseDto = new ResponseDto();
        responseDto.setSuccess(true);
        responseDto.setCode("200");
        responseDto.setMessage("退出登录成功");
        return responseDto;
    }

    @PostMapping("/tenant/switch")
    public ResponseDto switchTenant(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String auth = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        if (body == null || !StringUtils.hasText(body.get("tenantId"))) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR);
        }
        LoginUserDto dto = authApplicationService.switchTenant(auth, body.get("tenantId"), request);
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(dto);
        responseDto.setCode("200");
        return responseDto;
    }

    /**
     * 本人修改密码。体为 oldPassword、newPassword，均为 md5(明文 + 盐)。成功后须重新登录。
     */
    @PostMapping("/password")
    public ResponseDto changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String auth = request.getHeader(SecurityConstants.HEADER_AUTHORIZATION);
        authApplicationService.changePassword(auth,
                body == null ? null : body.get("oldPassword"),
                body == null ? null : body.get("newPassword"));
        ResponseDto responseDto = new ResponseDto();
        responseDto.setCode("200");
        responseDto.setMessage("密码已修改，请重新登录");
        return responseDto;
    }

    /**
     * 以初始密码登录后设置新密码。体为 passwordChangeTicket、newPassword；成功后返回登录结果。
     */
    @PostMapping("/password/initial")
    public ResponseDto initialPassword(@RequestBody Map<String, String> body) {
        LoginUserDto dto = authApplicationService.completeInitialPassword(
                body == null ? null : body.get("passwordChangeTicket"),
                body == null ? null : body.get("newPassword"));
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(dto);
        responseDto.setCode("200");
        return responseDto;
    }

    @GetMapping("/jwks")
    public Map<String, Object> jwks() {
        return authApplicationService.jwks();
    }

    @GetMapping("/public-key")
    public Map<String, String> publicKey() {
        Map<String, String> map = new HashMap<>();
        map.put("issuer", "euipd-auth");
        map.put("alg", "RS256");
        return map;
    }
}
