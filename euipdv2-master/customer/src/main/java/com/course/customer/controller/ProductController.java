/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.RdmsProduct;
import com.course.server.domain.RdmsProductRelease;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsProductDto;
import com.course.server.dto.rdms.RdmsProductReleaseDto;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.RdmsProductService;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    public static final String BUSINESS_NAME = "产品管理";

    @Resource
    private RdmsProductService rdmsProductService;


    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsProductDto>> list(@RequestBody PageDto<RdmsProductDto> pageDto) {
        ResponseDto<PageDto<RdmsProductDto>> responseDto = new ResponseDto<>();
        rdmsProductService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsProductDto> save(@RequestBody RdmsProductDto productDto) {
        ResponseDto<RdmsProductDto> responseDto = new ResponseDto<>();

        productDto.setCustomerId(TenantAccess.resolveCustomerId(productDto.getCustomerId()));
        ValidatorUtil.require(productDto.getCustomerId(), "客户ID");

        ValidatorUtil.length(productDto.getCustomerId(), "需求名称", 8, 8);

        RdmsProduct rdmsProduct = CopyUtil.copy(productDto, RdmsProduct.class);
        String id = rdmsProductService.saveBasic(rdmsProduct);

        responseDto.setContent(CopyUtil.copy(rdmsProductService.selectByPrimaryKey(id), RdmsProductDto.class));
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsProductService.delete(id);
        return responseDto;
    }

    /**
     * 从已完成项目发布产品（RDMS 台账 ACTIVE），同时生成发布基线。
     * body: projectId 必填；productName/productCode/releaseNote 可选；bomConfigId 产品配置，缺省为现行配置；
     * deliveryFileIdListStr 交付文件标识 JSON 数组，可选。
     */
    @PostMapping("/publish-from-project")
    @Transactional
    public ResponseDto<RdmsProductDto> publishFromProject(@RequestBody Map<String, String> body) {
        ResponseDto<RdmsProductDto> responseDto = new ResponseDto<>();
        ValidatorUtil.require(body == null ? null : body.get("projectId"), "项目ID");
        String fileIds = body.get("deliveryFileIdListStr");
        RdmsProduct product = rdmsProductService.publishFromProject(
                body.get("projectId"),
                body.get("productName"),
                body.get("productCode"),
                body.get("releaseNote"),
                body.get("bomConfigId"),
                StringUtils.hasText(fileIds) ? JSON.parseArray(fileIds, String.class) : null);
        responseDto.setContent(CopyUtil.copy(product, RdmsProductDto.class));
        responseDto.setSuccess(true);
        responseDto.setMessage("发布成功");
        return responseDto;
    }

    /**
     * 产品的发布版本，按发布时间倒序。body: productId 必填。
     */
    @PostMapping("/release/list")
    public ResponseDto<List<RdmsProductReleaseDto>> releaseList(@RequestBody Map<String, String> body) {
        ResponseDto<List<RdmsProductReleaseDto>> responseDto = new ResponseDto<>();
        ValidatorUtil.require(body == null ? null : body.get("productId"), "产品ID");
        responseDto.setContent(rdmsProductService.listReleases(body.get("productId")));
        return responseDto;
    }

    /**
     * 发布版本停产 ACTIVE→EOL；产品下全部停产后产品写 EOL。body: id 必填。
     */
    @PostMapping("/release/eol")
    @Transactional
    public ResponseDto<RdmsProductRelease> releaseEol(@RequestBody Map<String, String> body) {
        ResponseDto<RdmsProductRelease> responseDto = new ResponseDto<>();
        ValidatorUtil.require(body == null ? null : body.get("id"), "发布版本ID");
        responseDto.setContent(rdmsProductService.markReleaseEol(body.get("id")));
        responseDto.setSuccess(true);
        responseDto.setMessage("已停产");
        return responseDto;
    }

    /**
     * 产品停产：停产其全部发布版本，产品写 EOL。body: id 必填。
     */
    @PostMapping("/eol")
    @Transactional
    public ResponseDto<RdmsProductDto> eol(@RequestBody Map<String, String> body) {
        ResponseDto<RdmsProductDto> responseDto = new ResponseDto<>();
        ValidatorUtil.require(body == null ? null : body.get("id"), "产品ID");
        RdmsProduct product = rdmsProductService.markEol(body.get("id"));
        responseDto.setContent(CopyUtil.copy(product, RdmsProductDto.class));
        responseDto.setSuccess(true);
        responseDto.setMessage("已标记停产");
        return responseDto;
    }

}
