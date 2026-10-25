/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * BPMN 全文与引擎模型之间的转换。
 */
@Component
public class FlowBpmnParser {

    /**
     * 把定义全文解析成引擎模型。
     * 关闭外部实体与 DTD，流程图由机构上传，不得据此读取服务器文件。
     */
    public BpmnModel parse(String bpmnXml) {
        if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_INVALID, "流程图内容为空");
        }
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            XMLStreamReader reader = factory.createXMLStreamReader(new InputStreamReader(
                    new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            return new BpmnXMLConverter().convertToBpmnModel(reader);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_INVALID,
                    "流程图不是合法的 BPMN 文件：" + e.getMessage());
        }
    }
}
