/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.file2.flow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsFileApplyDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * file 进程没有流程引擎。开关命中时，把档案申请的状态流转转到 customer，仍使用调用人的登录凭证。
 */
@Component
public class FileFlowRemote {

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${rdms.flow.customer-base-url:http://127.0.0.1:18005/customer}")
    private String customerBaseUrl;

    public boolean engine(RdmsFileApplyDto dto, HttpServletRequest request) {
        ResponseDto<String> response = post("/file-apply/route", JSON.toJSONString(dto), request,
                new TypeReference<ResponseDto<String>>() {
                });
        return "ENGINE".equals(response.getContent());
    }

    public ResponseDto<RdmsFileApplyDto> apply(RdmsFileApplyDto dto, HttpServletRequest request) {
        return post("/file-apply/apply", JSON.toJSONString(dto), request,
                new TypeReference<ResponseDto<RdmsFileApplyDto>>() {
                });
    }

    public ResponseDto<String> agree(String applyId, HttpServletRequest request) {
        return post("/file-apply/agree/" + applyId, "{}", request, new TypeReference<ResponseDto<String>>() {
        });
    }

    public ResponseDto<String> reject(String applyId, HttpServletRequest request) {
        return post("/file-apply/reject/" + applyId, "{}", request, new TypeReference<ResponseDto<String>>() {
        });
    }

    private <T> ResponseDto<T> post(String path, String body, HttpServletRequest incoming, TypeReference<ResponseDto<T>> type) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(customerBaseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        String authorization = incoming == null ? null : incoming.getHeader("Authorization");
        if (StringUtils.hasText(authorization)) {
            builder.header("Authorization", authorization);
        }
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
            }
            ResponseDto<T> parsed = JSON.parseObject(response.body(), type);
            if (parsed == null) {
                throw new BusinessException(BusinessExceptionCode.EXTERNAL_SERVICE_ERROR, "档案流程没有返回结果");
            }
            return parsed;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(BusinessExceptionCode.EXTERNAL_SERVICE_ERROR, "档案流程服务不可用");
        }
    }
}
