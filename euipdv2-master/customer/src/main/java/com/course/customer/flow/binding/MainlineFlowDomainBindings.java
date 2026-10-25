/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.server.domain.RdmsCharacter;
import com.course.server.domain.RdmsCharacterProcess;
import com.course.server.domain.RdmsDemand;
import com.course.server.domain.RdmsDemandProcess;
import com.course.server.domain.RdmsProduct;
import com.course.server.domain.RdmsProductRelease;
import com.course.server.domain.RdmsProject;
import com.course.server.domain.RdmsProjectSubproject;
import com.course.server.enums.rdms.CharacterProcessStatusEnum;
import com.course.server.enums.rdms.CharacterStatusEnum;
import com.course.server.enums.rdms.DemandStatusEnum;
import com.course.server.enums.rdms.PreProjectLifecycleStatusEnum;
import com.course.server.enums.rdms.ProjectStatusEnum;
import com.course.server.enums.rdms.RdmsProductStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsDemandProcessMapper;
import com.course.server.service.rdms.RdmsCharacterProcessService;
import com.course.server.service.rdms.RdmsCharacterService;
import com.course.server.service.rdms.RdmsDemandService;
import com.course.server.service.rdms.RdmsProductReleaseService;
import com.course.server.service.rdms.RdmsProductService;
import com.course.server.service.rdms.RdmsProjectService;
import com.course.server.service.rdms.RdmsSubprojectService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import com.course.server.service.rdms.flow.MainlineTransitions;
import com.course.server.util.UuidUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 九个主线业务码各一个绑定。特性族与项目族共用写入规则，业务码仍然分开登记。
 * 产品台账不在需求、特性、预立项、项目四张表上，因此单独绑定。
 * 预立项生命周期由预立项服务按立项与关闭标记计算；CLOSED 的关闭标记与级联由关闭服务写入，绑定只校验取值。
 */
public final class MainlineFlowDomainBindings {

    private static final Set<String> DEMAND_STATUSES = statuses(DemandStatusEnum.values(), DemandStatusEnum::getStatus);
    private static final Set<String> CHARACTER_STATUSES = statuses(CharacterStatusEnum.values(), CharacterStatusEnum::getStatus);
    private static final Set<String> PROJECT_STATUSES = statuses(ProjectStatusEnum.values(), ProjectStatusEnum::getStatus);
    private static final Set<String> PRODUCT_STATUSES = statuses(RdmsProductStatusEnum.values(), RdmsProductStatusEnum::getStatus);
    private static final Set<String> PRE_PROJECT_STATUSES;

    static {
        Set<String> statuses = new HashSet<>();
        for (PreProjectLifecycleStatusEnum item : PreProjectLifecycleStatusEnum.values()) {
            statuses.add(item.getStatus());
        }
        PRE_PROJECT_STATUSES = Collections.unmodifiableSet(statuses);
    }

    private MainlineFlowDomainBindings() {
    }

    @Component
    public static class DemandBinding implements FlowDomainBinding {

        @Resource
        private RdmsDemandService rdmsDemandService;
        @Resource
        private RdmsDemandProcessMapper rdmsDemandProcessMapper;

        @Override
        public String bizCode() {
            return FlowBizCode.DEMAND.getCode();
        }

        @Override
        public Set<String> validStatuses() {
            return DEMAND_STATUSES;
        }

        @Override
        public String currentStatus(FlowContext context) {
            return require(context).getStatus();
        }

        @Override
        public void writeStatus(FlowContext context, String targetStatus) {
            RdmsDemand demand = require(context);
            MainlineTransitions.assertMove(bizCode(), demand.getStatus(), targetStatus, context);
            if (targetStatus.equals(demand.getStatus())) {
                return;
            }
            demand.setStatus(targetStatus);
            RdmsDemand patch = new RdmsDemand();
            patch.setId(demand.getId());
            patch.setStatus(targetStatus);
            rdmsDemandService.updateByPrimaryKeySelective(patch);
            context.setBizEntity(demand);
        }

        @Override
        public void writeProcess(FlowContext context, String text) {
            if (!StringUtils.hasText(text)) {
                return;
            }
            RdmsDemand demand = require(context);
            RdmsDemandProcess process = new RdmsDemandProcess();
            process.setId(UuidUtil.getShortUuid());
            process.setCustomerId(demand.getCustomerId());
            process.setDemandId(demand.getId());
            process.setAction(demand.getStatus());
            process.setReason(text.length() > 500 ? text.substring(0, 500) : text);
            process.setExecutorId(context.getOperatorId());
            process.setCreateTime(new Date());
            rdmsDemandProcessMapper.insert(process);
        }

        @Override
        public void writeNextNode(FlowContext context, String assigneeId) {
        }

        private RdmsDemand require(FlowContext context) {
            if (context.getBizEntity() instanceof RdmsDemand
                    && context.getBizId().equals(((RdmsDemand) context.getBizEntity()).getId())) {
                return (RdmsDemand) context.getBizEntity();
            }
            RdmsDemand demand = rdmsDemandService.selectByPrimaryKey(context.getBizId());
            if (demand == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            context.setBizEntity(demand);
            return demand;
        }
    }

    public abstract static class CharacterFamily implements FlowDomainBinding {

        private final String code;

        @Resource
        private RdmsCharacterService rdmsCharacterService;
        @Resource
        private RdmsCharacterProcessService rdmsCharacterProcessService;

        protected CharacterFamily(String code) {
            this.code = code;
        }

        @Override
        public String bizCode() {
            return code;
        }

        @Override
        public Set<String> validStatuses() {
            return CHARACTER_STATUSES;
        }

        @Override
        public String currentStatus(FlowContext context) {
            return require(context).getStatus();
        }

        @Override
        public void writeStatus(FlowContext context, String targetStatus) {
            RdmsCharacter character = require(context);
            MainlineTransitions.assertMove(bizCode(), character.getStatus(), targetStatus, context);
            if (targetStatus.equals(character.getStatus())) {
                return;
            }
            character.setStatus(targetStatus);
            RdmsCharacter patch = new RdmsCharacter();
            patch.setId(character.getId());
            patch.setStatus(targetStatus);
            rdmsCharacterService.updateByPrimaryKeySelective(patch);
            context.setBizEntity(character);
        }

        @Override
        public void writeProcess(FlowContext context, String text) {
            if (!StringUtils.hasText(text)) {
                return;
            }
            RdmsCharacterProcess process = new RdmsCharacterProcess();
            process.setCharacterId(context.getBizId());
            process.setExecutorId(context.getOperatorId());
            process.setJobDescription(text);
            process.setProcessStatus(CharacterProcessStatusEnum.ONGOING.getStatus());
            rdmsCharacterProcessService.save(process);
        }

        @Override
        public void writeNextNode(FlowContext context, String assigneeId) {
        }

        private RdmsCharacter require(FlowContext context) {
            if (context.getBizEntity() instanceof RdmsCharacter
                    && context.getBizId().equals(((RdmsCharacter) context.getBizEntity()).getId())) {
                return (RdmsCharacter) context.getBizEntity();
            }
            RdmsCharacter character = rdmsCharacterService.selectByPrimaryKey(context.getBizId());
            if (character == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            context.setBizEntity(character);
            return character;
        }
    }

    @Component
    public static class CharacterBinding extends CharacterFamily {
        public CharacterBinding() {
            super(FlowBizCode.CHARACTER.getCode());
        }
    }

    @Component
    public static class DecomposeBinding extends CharacterFamily {
        public DecomposeBinding() {
            super(FlowBizCode.DECOMPOSE.getCode());
        }
    }

    @Component
    public static class RevisionBinding extends CharacterFamily {
        public RevisionBinding() {
            super(FlowBizCode.REVISION.getCode());
        }
    }

    @Component
    public static class PreProjectBinding implements FlowDomainBinding {

        @Override
        public String bizCode() {
            return FlowBizCode.PRE_PROJECT.getCode();
        }

        @Override
        public Set<String> validStatuses() {
            return PRE_PROJECT_STATUSES;
        }

        @Override
        public String currentStatus(FlowContext context) {
            String current = context.variableAsString("currentStatus");
            return StringUtils.hasText(current) ? current : PreProjectLifecycleStatusEnum.DRAFT.getStatus();
        }

        @Override
        public void writeStatus(FlowContext context, String targetStatus) {
            MainlineTransitions.assertMove(bizCode(), currentStatus(context), targetStatus, context);
        }

        @Override
        public void writeProcess(FlowContext context, String text) {
        }

        @Override
        public void writeNextNode(FlowContext context, String assigneeId) {
        }
    }

    public abstract static class ProjectFamily implements FlowDomainBinding {

        private final String code;

        @Resource
        private RdmsProjectService rdmsProjectService;
        @Resource
        private RdmsSubprojectService rdmsSubprojectService;

        protected ProjectFamily(String code) {
            this.code = code;
        }

        @Override
        public String bizCode() {
            return code;
        }

        @Override
        public Set<String> validStatuses() {
            return PROJECT_STATUSES;
        }

        @Override
        public String currentStatus(FlowContext context) {
            if (subproject(context)) {
                return requireSubproject(context).getStatus();
            }
            return requireProject(context).getStatus();
        }

        @Override
        public void writeStatus(FlowContext context, String targetStatus) {
            if (subproject(context)) {
                RdmsProjectSubproject subproject = requireSubproject(context);
                MainlineTransitions.assertMove(bizCode(), subproject.getStatus(), targetStatus, context);
                if (targetStatus.equals(subproject.getStatus())) {
                    return;
                }
                subproject.setStatus(targetStatus);
                RdmsProjectSubproject patch = new RdmsProjectSubproject();
                patch.setId(subproject.getId());
                patch.setStatus(targetStatus);
                rdmsSubprojectService.updateByPrimaryKeySelective(patch);
                context.setBizEntity(subproject);
                return;
            }
            RdmsProject project = requireProject(context);
            MainlineTransitions.assertMove(bizCode(), project.getStatus(), targetStatus, context);
            if (targetStatus.equals(project.getStatus())) {
                return;
            }
            project.setStatus(targetStatus);
            RdmsProject patch = new RdmsProject();
            patch.setId(project.getId());
            patch.setStatus(targetStatus);
            rdmsProjectService.updateByPrimaryKeySelective(patch);
            context.setBizEntity(project);
        }

        @Override
        public void writeProcess(FlowContext context, String text) {
        }

        @Override
        public void writeNextNode(FlowContext context, String assigneeId) {
        }

        private boolean subproject(FlowContext context) {
            return MainlineTransitions.SUBPROJECT.equals(context.variableAsString(MainlineTransitions.ENTITY_KIND));
        }

        private RdmsProject requireProject(FlowContext context) {
            if (context.getBizEntity() instanceof RdmsProject
                    && context.getBizId().equals(((RdmsProject) context.getBizEntity()).getId())) {
                return (RdmsProject) context.getBizEntity();
            }
            RdmsProject project = rdmsProjectService.selectByPrimaryKey(context.getBizId());
            if (project == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            context.setBizEntity(project);
            return project;
        }

        private RdmsProjectSubproject requireSubproject(FlowContext context) {
            if (context.getBizEntity() instanceof RdmsProjectSubproject
                    && context.getBizId().equals(((RdmsProjectSubproject) context.getBizEntity()).getId())) {
                return (RdmsProjectSubproject) context.getBizEntity();
            }
            RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(context.getBizId());
            if (subproject == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            context.setBizEntity(subproject);
            return subproject;
        }
    }

    @Component
    public static class ProjectSetupBinding extends ProjectFamily {
        public ProjectSetupBinding() {
            super(FlowBizCode.PROJECT_SETUP.getCode());
        }
    }

    @Component
    public static class DevelopBinding extends ProjectFamily {
        public DevelopBinding() {
            super(FlowBizCode.DEVELOP.getCode());
        }
    }

    @Component
    public static class CloseoutBinding extends ProjectFamily {
        public CloseoutBinding() {
            super(FlowBizCode.CLOSEOUT.getCode());
        }
    }

    /**
     * 业务键为发布登记标识；发布登记启用前建立的实例以产品标识为键，查不到发布登记时按产品处理。
     */
    @Component
    public static class ProductBinding implements FlowDomainBinding {

        @Resource
        private RdmsProductService rdmsProductService;
        @Resource
        private RdmsProductReleaseService rdmsProductReleaseService;

        @Override
        public String bizCode() {
            return FlowBizCode.PRODUCT.getCode();
        }

        @Override
        public Set<String> validStatuses() {
            return PRODUCT_STATUSES;
        }

        @Override
        public String currentStatus(FlowContext context) {
            RdmsProductRelease release = release(context);
            return release != null ? release.getStatus() : require(context).getStatus();
        }

        @Override
        public void writeStatus(FlowContext context, String targetStatus) {
            RdmsProductRelease release = release(context);
            if (release != null) {
                MainlineTransitions.assertMove(bizCode(), release.getStatus(), targetStatus, context);
                if (RdmsProductStatusEnum.EOL.getStatus().equals(targetStatus)) {
                    rdmsProductReleaseService.markEol(release);
                }
                return;
            }
            RdmsProduct product = require(context);
            MainlineTransitions.assertMove(bizCode(), product.getStatus(), targetStatus, context);
            if (targetStatus.equals(product.getStatus())) {
                return;
            }
            product.setStatus(targetStatus);
            RdmsProduct patch = new RdmsProduct();
            patch.setId(product.getId());
            patch.setStatus(targetStatus);
            rdmsProductService.updateByPrimaryKeySelective(patch);
            context.setBizEntity(product);
        }

        private RdmsProductRelease release(FlowContext context) {
            if (context.getBizEntity() instanceof RdmsProductRelease
                    && context.getBizId().equals(((RdmsProductRelease) context.getBizEntity()).getId())) {
                return (RdmsProductRelease) context.getBizEntity();
            }
            if (context.getBizEntity() instanceof RdmsProduct) {
                return null;
            }
            RdmsProductRelease release = rdmsProductReleaseService.selectById(context.getCustomerId(), context.getBizId());
            if (release != null) {
                context.setBizEntity(release);
            }
            return release;
        }

        @Override
        public void writeProcess(FlowContext context, String text) {
        }

        @Override
        public void writeNextNode(FlowContext context, String assigneeId) {
        }

        private RdmsProduct require(FlowContext context) {
            if (context.getBizEntity() instanceof RdmsProduct
                    && context.getBizId().equals(((RdmsProduct) context.getBizEntity()).getId())) {
                return (RdmsProduct) context.getBizEntity();
            }
            RdmsProduct product = rdmsProductService.selectByPrimaryKey(context.getBizId());
            if (product == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            context.setBizEntity(product);
            return product;
        }
    }

    private static <T> Set<String> statuses(T[] values, java.util.function.Function<T, String> reader) {
        Set<String> statuses = new HashSet<>();
        for (T item : values) {
            statuses.add(reader.apply(item));
        }
        return Collections.unmodifiableSet(statuses);
    }
}
