/**
 * Author：周朔鹏
 * 编制时间：2025-08-05
 * 修订：2026-08-02 阿里云 OSS → MinIO（S3 兼容）
 *
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.controller;

import com.course.server.storage.OssTargetResolver;
import com.course.server.domain.RdmsFile;
import com.course.server.dto.FileDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsFileApplyDto;
import com.course.server.dto.rdms.RdmsFileDto;
import com.course.server.enums.FileGroupingEnum;
import com.course.server.enums.rdms.FileApplicationStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.*;
import com.course.server.security.tenant.TenantContext;
import com.course.server.storage.StorageTarget;
import com.course.server.service.util.IpUtils;
import com.course.server.storage.ObjectStorageService;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/rdms")
public class RdmsOssController {

    private static final Logger LOG = LoggerFactory.getLogger(RdmsOssController.class);

    public static final String BUSINESS_NAME = "文件上传下载";

    @Resource
    private OssTargetResolver ossTargetResolver;
    @Resource
    private RdmsFileDownloadService rdmsFileDownloadService;
    @Resource
    private RdmsFileService rdmsFileService;
    @Resource
    private RdmsFileApplyService rdmsFileApplyService;
    @Resource
    private RdmsFileAuthService rdmsFileAuthService;
    @Resource
    private IpUtils ipUtils;
    @Resource
    private ObjectStorageService objectStorageService;

    @PostMapping("/oss-simple-rdms")
    public ResponseDto<FileDto> fileUploadV2(@RequestParam String folder, MultipartFile file, Integer size,
                                             String use, String customer) throws Exception {
        LOG.info("上传文件开始(MinIO/租户)");
        customer = OssTargetResolver.currentTenant(customer);
        StorageTarget target = ossTargetResolver.docTarget(customer);
        FileDto fileDto = buildRdmsFileDto(folder, file, size, use, customer, target.getDomain());
        objectStorageService.putObject(target, fileDto.getPath(), file.getBytes(), file.getContentType());
        ResponseDto<FileDto> responseDto = new ResponseDto<>();
        responseDto.setContent(fileDto);
        return responseDto;
    }

    @PostMapping("/oss-file-upload-web")
    public ResponseDto<FileDto> ossFileUploadWeb(@RequestParam String folder, MultipartFile file, Integer size,
                                                 String use, String customer) throws Exception {
        LOG.info("上传公共文件开始(MinIO)");
        customer = OssTargetResolver.currentTenant(customer);
        StorageTarget target = ossTargetResolver.webTarget(customer);
        FileDto fileDto = buildRdmsFileDto(folder, file, size, use, customer, target.getDomain());
        objectStorageService.putObject(target, fileDto.getPath(), file.getBytes(), file.getContentType());
        ResponseDto<FileDto> responseDto = new ResponseDto<>();
        responseDto.setContent(fileDto);
        return responseDto;
    }

    @PostMapping("/oss-get-signed-url")
    public ResponseDto<String> getSignedUrl(@RequestParam @NotEmpty String fileFullName,
                                            @RequestParam @NotEmpty String customer,
                                            @RequestParam @NotEmpty String user,
                                            HttpServletRequest servletRequest) {
        LOG.info("获取签名Url(MinIO) file={}", fileFullName);
        RdmsFileDto fileByFullName = rdmsFileService.getFileByFullName(fileFullName);
        if (ObjectUtils.isEmpty(fileByFullName) || !rdmsFileAuthService.mayDownload(fileByFullName)) {
            throw new BusinessException(BusinessExceptionCode.FILE_DOWNLOAD_DENIED);
        }
        String memberId = TenantContext.getMembershipId();
        if (StringUtils.hasText(memberId)) {
            user = memberId;
        }

        StorageTarget target = ossTargetResolver.docTarget(OssTargetResolver.currentTenant(customer));
        String url = objectStorageService.presignGetUrl(target, fileFullName);

        rdmsFileDownloadService.record(CopyUtil.copy(fileByFullName, RdmsFile.class), user,
                ipUtils.getIpAddress(servletRequest));
        List<RdmsFileApplyDto> applyFileList =
                rdmsFileApplyService.getApplyFileListByFileIdAndCustomerUserId(fileByFullName.getId(), user);
        if (!CollectionUtils.isEmpty(applyFileList)) {
            for (RdmsFileApplyDto applyDto : applyFileList) {
                boolean active = applyDto.getDeleted() == null || applyDto.getDeleted() == 0;
                if (active && FileApplicationStatusEnum.APPROVED.getStatus().equals(applyDto.getApplicationStatus())) {
                    applyDto.setApplicationStatus(FileApplicationStatusEnum.COMPLETE.getStatus());
                    rdmsFileApplyService.update(applyDto);
                }
            }
        }

        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(url);
        return responseDto;
    }

    private FileDto buildRdmsFileDto(String folder, MultipartFile file, Integer size, String use,
                                     String customer, String domain) {
        FileDto fileDto = new FileDto();
        FileGroupingEnum useEnum = FileGroupingEnum.getByCode(use);
        assert useEnum != null;
        String key = UuidUtil.getShortUuid();
        fileDto.setKey(key);
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        fileDto.setName(fileName);
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        fileDto.setSuffix(suffix);
        fileDto.setUse(useEnum.getCode().toLowerCase());
        fileDto.setCustomer(customer);
        String dir;
        if (customer != null && !customer.isEmpty()) {
            dir = customer + "/" + fileDto.getUse();
        } else {
            dir = fileDto.getUse();
        }
        String path;
        if (folder == null || folder.isEmpty() || "null".equals(folder)) {
            path = dir + "/" + key + "-" + fileName;
        } else {
            path = dir + "/" + folder + "/" + key + "-" + fileName;
        }
        fileDto.setPath(path);
        fileDto.setAbsPath(objectStorageService.joinPublicUrl(domain, path));
        fileDto.setSize(size);
        fileDto.setCreatedAt(new Date());
        return fileDto;
    }
}
