/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 流程双轨开关。属性 {@code rdms.flow.enabled-biz-codes} 为逗号分隔列表。
 * 单独的业务码表示全部机构；{@code 机构标识:业务码} 只放开该机构。
 * 属性缺省或为空时，新单据一律走原代码。
 */
@Component
public class FlowSwitch {

    @Value("${rdms.flow.enabled-biz-codes:}")
    private String enabledBizCodes;

    public boolean enabled(String customerId, String bizCode) {
        return matches(enabledBizCodes, customerId, bizCode);
    }

    static boolean matches(String raw, String customerId, String bizCode) {
        if (raw == null || bizCode == null || bizCode.isEmpty()) {
            return false;
        }
        for (String token : raw.split(",")) {
            String item = token.trim();
            if (item.isEmpty()) {
                continue;
            }
            int colon = item.indexOf(':');
            if (colon < 0) {
                if (item.equals(bizCode)) {
                    return true;
                }
                continue;
            }
            String org = item.substring(0, colon).trim();
            String code = item.substring(colon + 1).trim();
            if (code.equals(bizCode) && org.equals(customerId)) {
                return true;
            }
        }
        return false;
    }
}
