/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsCustomer;
import com.course.server.domain.RdmsUser;
import com.course.server.dto.*;
import com.course.server.dto.rdms.RdmsCourseContentDto;
import com.course.server.dto.rdms.RdmsCourseDto;
import com.course.server.dto.rdms.RdmsSectionDto;
import com.course.server.enums.CourseChargeEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.RdmsCourseOwnCategoryService;
import com.course.server.service.RdmsCourseService;
import com.course.server.service.RdmsSectionService;
import com.course.server.service.rdms.RdmsCustomerService;
import com.course.server.service.rdms.RdmsUserService;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    private static final Logger LOG = LoggerFactory.getLogger(CourseController.class);
    public static final String BUSINESS_NAME = "课程";

    @Resource
    private RdmsCourseService rdmsCourseService;
    @Resource
    private RdmsSectionService rdmsSectionService;
    @Resource
    private RdmsCourseOwnCategoryService rdmsCourseOwnCategoryService;
    @Resource
    private RdmsUserService rdmsUserService;
    @Resource
    private RdmsCustomerService rdmsCustomerService;

    /**
     * 列表查询
     */
    @PostMapping("/list")
    public ResponseDto list(@RequestBody RdmsCoursePageDto pageDto) {
        ValidatorUtil.require(pageDto.getCustomerId(), "机构ID");
        ResponseDto responseDto = new ResponseDto();

        rdmsCourseService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 保存，id有值时更新，无值时新增
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto save(@RequestBody RdmsCourseDto courseDto) {
        // 保存校验
        ValidatorUtil.require(courseDto.getName(), "名称");
        ValidatorUtil.length(courseDto.getName(), "名称", 1, 50);
        ValidatorUtil.length(courseDto.getSummary(), "概述", 1, 200);
        ValidatorUtil.length(courseDto.getImage(), "封面", 1, 200);

        ResponseDto responseDto = new ResponseDto();
        //新建课程的时候,创建者就是课程的拥有者
        //更新课程信息的时候,不能更新课程的创建者的loginName
        if(courseDto.getId()==null || courseDto.getId().isEmpty()){
            courseDto.setCreatorLoginname(currentLoginName());
        }else{
            RdmsCourseDto course = rdmsCourseService.getCourse(courseDto.getId());
            if (course == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            courseDto.setCreatorLoginname(course.getCreatorLoginname());
        }

        if(courseDto.getId()!=null){
            List<RdmsSectionDto> sectionDtoList = rdmsSectionService.listByCourse(courseDto.getId());
            if(sectionDtoList.isEmpty()){
                courseDto.setCharge(CourseChargeEnum.FREE.getCode());
            }else{
                courseDto.setCharge(CourseChargeEnum.FREE.getCode());
                for(RdmsSectionDto sectionDto : sectionDtoList){
                    if(sectionDto.getCharge().equals(CourseChargeEnum.CHARGE.getCode())){  //只要有一个小节是收费的, 课程就是收费的
                        courseDto.setCharge(CourseChargeEnum.CHARGE.getCode());
                        break;
                    }
                }
            }
        }else{
            if(!ObjectUtils.isEmpty(courseDto.getCharge())){
                courseDto.setCharge(CourseChargeEnum.FREE.getCode());
            }
        }

        if(courseDto.getPrice()==null){
            courseDto.setPrice(BigDecimal.valueOf(0.00));
        }
        rdmsCourseService.save(courseDto);

        responseDto.setContent(courseDto);
        return responseDto;
    }

    /**
     * 机构身份登录时 JWT 主体为机构标识，登录名取机构联系电话
     */
    private String currentLoginName() {
        String userId = TenantContext.getUserId();
        if (StringUtils.hasText(userId)) {
            RdmsUser user = rdmsUserService.selectByPrimaryKey(userId);
            if (user != null) {
                return user.getLoginName();
            }
            RdmsCustomer customer = rdmsCustomerService.selectByPrimaryKey(userId);
            if (customer != null) {
                return customer.getContactPhone();
            }
        }
        throw new BusinessException(BusinessExceptionCode.USER_NOT_EXIST_ERROR);
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsCourseService.delete(id);
        return responseDto;
    }

    /**
     * 查找课程下所有分类
     * @param courseId
     */
    @PostMapping("/list-category/{courseId}")
    public ResponseDto listCategory(@PathVariable(value = "courseId") String courseId) {
        ResponseDto responseDto = new ResponseDto();
        List<String> cidList = rdmsCourseOwnCategoryService.listByCourse(courseId);
        responseDto.setContent(cidList);
        return responseDto;
    }

    @GetMapping("/find-content/{id}")
    public ResponseDto findContent(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        RdmsCourseContentDto contentDto = rdmsCourseService.findContent(id);
        responseDto.setContent(contentDto);
        return responseDto;
    }

    @PostMapping("/save-content")
    public ResponseDto saveContent(@RequestBody RdmsCourseContentDto contentDto) {
        ResponseDto responseDto = new ResponseDto();
        rdmsCourseService.saveContent(contentDto);
        return responseDto;
    }

    @RequestMapping(value = "/sort")
    public ResponseDto sort(@RequestBody RdmsSortDto sortDto) {
        LOG.info("更新排序");
        ResponseDto responseDto = new ResponseDto();
        rdmsCourseService.sort(sortDto);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/user-course/{loginName}")
    public ResponseDto userProduct(@PathVariable(value = "loginName") String loginName) {
        ResponseDto responseDto = new ResponseDto();
        List<RdmsCourseDto> userProduct = rdmsCourseService.getUserProduct(loginName);
        responseDto.setContent(userProduct);
        return responseDto;
    }

}
