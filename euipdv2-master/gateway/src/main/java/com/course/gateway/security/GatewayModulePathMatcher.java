/**
 * Author：周朔鹏
 * 编制时间：2026-08-01
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

/**
 * 规则与 server 的 ModulePathMatcher 保持一致。
 */
@Component
public class GatewayModulePathMatcher {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayModulePathMatcher.class);

    private final List<Mapping> mappings = new ArrayList<>();
    private final Set<String> whitelist = new HashSet<>();
    private final List<String> whitelistPrefixes = new ArrayList<>();

    @PostConstruct
    public void init() {
        try (InputStream in = new ClassPathResource("security/module-path.yml").getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(in);
            Object mappingNode = root.get("mappings");
            if (mappingNode instanceof List) {
                for (Object item : (List<?>) mappingNode) {
                    if (item instanceof Map) {
                        Map<?, ?> m = (Map<?, ?>) item;
                        String prefix = Objects.toString(m.get("prefix"), null);
                        String module = Objects.toString(m.get("module"), null);
                        if (StringUtils.hasText(prefix) && StringUtils.hasText(module)) {
                            mappings.add(new Mapping(prefix, module));
                        }
                    }
                }
            }
            mappings.sort((a, b) -> Integer.compare(b.prefix.length(), a.prefix.length()));
            readList(root.get("whitelist"), whitelist);
            readList(root.get("whitelist-prefixes"), whitelistPrefixes);
            LOG.info("Gateway module-path loaded mappings={}, whitelist={}, whitelistPrefixes={}",
                    mappings.size(), whitelist.size(), whitelistPrefixes.size());
        } catch (Exception e) {
            LOG.error("Failed to load module-path.yml", e);
        }
    }

    private static void readList(Object node, Collection<String> target) {
        if (node instanceof List) {
            for (Object w : (List<?>) node) {
                if (w != null && StringUtils.hasText(String.valueOf(w))) {
                    target.add(String.valueOf(w).trim());
                }
            }
        }
    }

    public boolean needIntercept(String path) {
        return path != null && (
                path.contains("/admin/")
                        || path.contains("/rdms/")
                        || path.contains("/customer/")
                        || path.contains("/user/")
                        || path.contains("/system/")
                        || path.contains("/auth/")
                        || path.contains("/tenant-ui-policy")
                        || path.contains("/file/")
                        || path.contains("/ftp/")
        );
    }

    /**
     * 精确条目须与路径相同；前缀条目须为路径开头。含 ..、;、// 或编码分隔符的路径一律不算白名单，
     * 避免借白名单前缀拼接出其他接口。
     */
    public boolean isWhitelisted(String path) {
        if (!StringUtils.hasText(path) || isAmbiguousPath(path)) {
            return false;
        }
        if (whitelist.contains(path)) {
            return true;
        }
        for (String prefix : whitelistPrefixes) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    static boolean isAmbiguousPath(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.contains("..") || lower.contains(";") || lower.contains("//")
                || lower.contains("%2e") || lower.contains("%2f") || lower.contains("%5c") || lower.contains("\\");
    }

    public String matchModule(String path) {
        for (Mapping m : mappings) {
            if (path.contains(m.prefix)) {
                return m.module;
            }
        }
        return null;
    }

    public String inferOperation(String method, String path) {
        String lowerPath = path == null ? "" : path.toLowerCase(Locale.ROOT);
        String m = method == null ? "GET" : method.toUpperCase(Locale.ROOT);
        switch (m) {
            case "GET":
                if (lowerPath.contains("/export")) return "EXPORT";
                if (isDownload(lowerPath)) return "DOWNLOAD";
                return "VIEW";
            case "POST":
                // 签名下载只要求 FILE:VIEW，是否可下载由服务侧 RdmsFileAuthService.mayDownload 判定
                if (lowerPath.contains("signed-url")) return "VIEW";
                if (isDownload(lowerPath)) return "DOWNLOAD";
                if (containsAny(lowerPath, "/save", "/create", "/add",
                        "/setup-application", "/direct-setup", "/module-setup", "/withdraw")) return "CREATE";
                if (containsAny(lowerPath, "/approve", "/review", "/audit",
                        "/setup-module-project", "/reject-application", "/define/reject", "/terminate/reject")
                        || lowerPath.endsWith("/setup") || lowerPath.contains("/setup/")) return "APPROVE";
                if (lowerPath.contains("/export")) return "EXPORT";
                if (lowerPath.contains("/import")) return "IMPORT";
                if (containsAny(lowerPath, "/update", "/edit", "/modify")) return "UPDATE";
                if (containsAny(lowerPath, "/delete", "/del", "/remove")) return "DELETE";
                if (containsAny(lowerPath, "/list", "/search", "/query", "/get", "/count",
                        "/validate", "/diagram", "/history", "/capability", "/where-used")) return "VIEW";
                return "UPDATE";
            case "PUT":
            case "PATCH":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return "VIEW";
        }
    }

    private static boolean isDownload(String lowerPath) {
        return lowerPath.contains("/download");
    }

    public boolean hasScope(Collection<String> scopes, String module, String operation) {
        if (scopes == null || scopes.isEmpty() || !StringUtils.hasText(module)) {
            return false;
        }
        if (scopes.contains("PLATFORM:*")) {
            return true;
        }
        if (scopes.contains(module + ":*")) {
            return true;
        }
        return StringUtils.hasText(operation) && scopes.contains(module + ":" + operation);
    }

    private static boolean containsAny(String path, String... parts) {
        for (String p : parts) {
            if (path.contains(p)) {
                return true;
            }
        }
        return false;
    }

    private static final class Mapping {
        private final String prefix;
        private final String module;

        private Mapping(String prefix, String module) {
            this.prefix = prefix;
            this.module = module;
        }
    }
}
