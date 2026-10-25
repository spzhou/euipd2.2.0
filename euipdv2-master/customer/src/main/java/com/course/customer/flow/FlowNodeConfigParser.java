/**
 * Author：周朔鹏
 * 编制时间：2026-10-06

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.service.rdms.flow.FlowNodeConfig;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把 BPMN 节点 {@code extensionElements} 里的 {@code euipd:node} 读成 {@link FlowNodeConfig}。
 * 节点参数以 BPMN 为唯一来源，不另建节点参数表（实施方案第 4.3 节）。
 */
@Component
public class FlowNodeConfigParser {

    private static final String NODE = "node";
    private static final String ASSIGNEE = "assignee";
    private static final String FORM = "form";
    private static final String GUARD = "guard";
    private static final String ON_ENTER = "onEnter";
    private static final String ON_COMPLETE = "onComplete";
    private static final String TIMER = "timer";

    /** assignee 元素上由解析器自身消费的属性，其余属性作为解析器参数传入。 */
    private static final List<String> ASSIGNEE_RESERVED = Arrays.asList("resolver", "fallback", "denyCode");
    /** 动作元素上由框架消费的属性，其余属性作为动作参数传入。 */
    private static final List<String> ACTION_RESERVED = Arrays.asList("action", "when");

    /**
     * 解析一个节点的配置。节点没有 {@code euipd:node} 时返回只含标识与名称的空配置，
     * 由发布校验判定该节点是否必须配置。
     */
    public FlowNodeConfig parse(FlowElement element) {
        FlowNodeConfig config = new FlowNodeConfig();
        config.setNodeId(element.getId());
        config.setNodeName(element.getName());

        ExtensionElement node = firstChild(element.getExtensionElements(), NODE);
        if (node == null) {
            return config;
        }

        ExtensionElement assignee = firstChild(node.getChildElements(), ASSIGNEE);
        if (assignee != null) {
            config.setAssigneeResolver(attr(assignee, "resolver"));
            config.setAssigneeFallback(attr(assignee, "fallback"));
            config.setAssigneeDenyCode(attr(assignee, "denyCode"));
            config.setAssigneeParams(otherAttributes(assignee, ASSIGNEE_RESERVED));
        }

        ExtensionElement form = firstChild(node.getChildElements(), FORM);
        if (form != null) {
            config.setFormCode(attr(form, "code"));
        }

        ExtensionElement guard = firstChild(node.getChildElements(), GUARD);
        if (guard != null) {
            String sourceStates = attr(guard, "sourceStates");
            if (sourceStates != null && !sourceStates.isEmpty()) {
                for (String state : sourceStates.split(",")) {
                    String trimmed = state.trim();
                    if (!trimmed.isEmpty()) {
                        config.getGuardSourceStates().add(trimmed);
                    }
                }
            }
            config.setGuardDenyCode(attr(guard, "denyCode"));
        }

        config.setOnEnter(parseActions(node, ON_ENTER));
        config.setOnComplete(parseActions(node, ON_COMPLETE));

        ExtensionElement timer = firstChild(node.getChildElements(), TIMER);
        if (timer != null) {
            FlowNodeConfig.FlowTimerConfig timerConfig = new FlowNodeConfig.FlowTimerConfig();
            timerConfig.setDuration(attr(timer, "duration"));
            timerConfig.setAction(attr(timer, "action"));
            timerConfig.setRepeat(Boolean.parseBoolean(attr(timer, "repeat")));
            config.setTimer(timerConfig);
        }
        return config;
    }

    private List<FlowNodeConfig.FlowActionConfig> parseActions(ExtensionElement node, String name) {
        List<FlowNodeConfig.FlowActionConfig> configs = new ArrayList<>();
        List<ExtensionElement> elements = node.getChildElements() == null ? null : node.getChildElements().get(name);
        if (elements == null) {
            return configs;
        }
        for (ExtensionElement element : elements) {
            FlowNodeConfig.FlowActionConfig config = new FlowNodeConfig.FlowActionConfig();
            config.setAction(attr(element, "action"));
            config.setWhen(attr(element, "when"));
            config.setParams(otherAttributes(element, ACTION_RESERVED));
            configs.add(config);
        }
        return configs;
    }

    private ExtensionElement firstChild(Map<String, List<ExtensionElement>> children, String name) {
        if (children == null) {
            return null;
        }
        List<ExtensionElement> elements = children.get(name);
        return (elements == null || elements.isEmpty()) ? null : elements.get(0);
    }

    private String attr(ExtensionElement element, String name) {
        Map<String, List<ExtensionAttribute>> attributes = element.getAttributes();
        if (attributes == null) {
            return null;
        }
        List<ExtensionAttribute> values = attributes.get(name);
        return (values == null || values.isEmpty()) ? null : values.get(0).getValue();
    }

    /** 取除保留属性外的全部属性，作为解析器或动作的参数。 */
    private Map<String, String> otherAttributes(ExtensionElement element, List<String> reserved) {
        Map<String, String> params = new LinkedHashMap<>();
        Map<String, List<ExtensionAttribute>> attributes = element.getAttributes();
        if (attributes == null) {
            return params;
        }
        attributes.forEach((name, values) -> {
            if (reserved.contains(name) || values == null || values.isEmpty()) {
                return;
            }
            params.put(name, values.get(0).getValue());
        });
        return params;
    }
}
