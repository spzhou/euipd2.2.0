/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.service.rdms.rich.RichDocSchemaRegistry;
import com.course.server.util.RichHtmlSanitizer;
import lombok.Data;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlink;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jsoup.nodes.Entities;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STVerticalAlignRun;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 把 .docx 按一级标题拆分到内容文档的章节（实现方案第 10.4 节）：
 * 一级标题（或独立成段、与章节名称或别名相同的短段落）去掉序号后与章节名称、别名比对；
 * 二至四级标题转为 h2–h4，图片交给 imageUploader 上传为 TEMP 图片。结果只用于填充编辑器，不直接保存。
 */
public class RichWordParser {

    private static final Pattern HEADING_STYLE = Pattern.compile("(?i)^(?:heading|标题)\\s*(\\d)$");
    private static final Pattern NUMBER_PREFIX = Pattern.compile(
            "^(?:第[一二三四五六七八九十百零〇\\d]+[章节部分篇]|[一二三四五六七八九十]+[、.．]|[（(][一二三四五六七八九十\\d]+[）)]"
                    + "|\\d+(?:\\.\\d+)*[、.．]?)\\s*");
    private static final int ALIAS_MAX_LEN = 30;

    @Data
    public static class Result {
        private Map<String, String> sections = new LinkedHashMap<>();
        /** 未能归入任何章节的内容（一级标题与章节不匹配时）。 */
        private String unassigned = "";
        private List<String> matchedHeadings = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private int images;
    }

    private final String bizType;
    private final Function<byte[], String> imageUploader;
    private final Map<String, String> aliasToCode = new LinkedHashMap<>();
    private final Result result = new Result();
    private final Map<String, StringBuilder> buffers = new LinkedHashMap<>();
    private StringBuilder current;
    private XWPFDocument doc;
    private String openList;

    public RichWordParser(String bizType, Function<byte[], String> imageUploader) {
        this.bizType = bizType;
        this.imageUploader = imageUploader;
        for (RichDocSchemaRegistry.SectionDef def : RichDocSchemaRegistry.sections(bizType)) {
            aliasToCode.putIfAbsent(normalize(def.getName()), def.getCode());
            for (String alias : def.getAliases()) {
                aliasToCode.putIfAbsent(normalize(alias), def.getCode());
            }
        }
    }

    public Result parse(byte[] docx) throws IOException {
        StringBuilder unassigned = new StringBuilder();
        current = unassigned;
        boolean matchedAny = false;
        try (XWPFDocument d = new XWPFDocument(new ByteArrayInputStream(docx))) {
            doc = d;
            for (IBodyElement element : d.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph p = (XWPFParagraph) element;
                    String text = p.getText() == null ? "" : p.getText().trim();
                    int level = headingLevel(p);
                    String code = level <= 1 && !text.isEmpty() && text.length() <= ALIAS_MAX_LEN
                            ? sectionOf(text) : null;
                    if (code != null) {
                        closeList();
                        current = buffers.computeIfAbsent(code, k -> new StringBuilder());
                        result.getMatchedHeadings().add(text);
                        matchedAny = true;
                        continue;
                    }
                    if (level == 1) {
                        closeList();
                        current = unassigned;
                        current.append("<p><strong>").append(esc(text)).append("</strong></p>");
                        continue;
                    }
                    paragraph(p, level);
                } else if (element instanceof XWPFTable) {
                    closeList();
                    table((XWPFTable) element);
                } else if (element instanceof XWPFSDT) {
                    closeList();
                    String text = ((XWPFSDT) element).getContent().getText();
                    if (StringUtils.hasText(text)) {
                        current.append("<p>").append(esc(text.trim())).append("</p>");
                    }
                }
            }
            closeList();
        }
        List<RichDocSchemaRegistry.SectionDef> defs = RichDocSchemaRegistry.sections(bizType);
        if (!matchedAny && !defs.isEmpty() && unassigned.length() > 0) {
            buffers.put(defs.get(0).getCode(), unassigned);
            unassigned = new StringBuilder();
            result.getWarnings().add("未识别到与章节对应的一级标题，全部内容放入“" + defs.get(0).getName() + "”");
        }
        for (RichDocSchemaRegistry.SectionDef def : defs) {
            StringBuilder sb = buffers.get(def.getCode());
            if (sb == null) {
                continue;
            }
            String html = RichHtmlSanitizer.sanitize(sb.toString());
            result.getSections().put(def.getCode(), html);
            if (RichHtmlSanitizer.textLength(html) > def.getMax()) {
                result.getWarnings().add("“" + def.getName() + "”超过 " + def.getMax() + " 字，保存前请删减");
            }
        }
        result.setUnassigned(RichHtmlSanitizer.sanitize(unassigned.toString()));
        return result;
    }

    /** 整段与章节名称或别名相同；带“/”的标题任一部分相同亦可。 */
    private String sectionOf(String text) {
        String key = normalize(text);
        String code = aliasToCode.get(key);
        if (code == null && key.contains("/")) {
            for (String part : key.split("/")) {
                code = aliasToCode.get(part);
                if (code != null) {
                    break;
                }
            }
        }
        return code;
    }

    private void paragraph(XWPFParagraph p, int level) {
        String inner = runs(p);
        if (level >= 2) {
            closeList();
            int h = Math.min(level, 4);
            current.append("<h").append(h).append('>').append(inner).append("</h").append(h).append('>');
            return;
        }
        if (p.getNumID() != null) {
            String fmt = p.getNumFmt();
            String tag = fmt == null || "bullet".equalsIgnoreCase(fmt) ? "ul" : "ol";
            if (!tag.equals(openList)) {
                closeList();
                current.append('<').append(tag).append('>');
                openList = tag;
            }
            current.append("<li><p>").append(inner).append("</p></li>");
            return;
        }
        closeList();
        if (inner.isEmpty()) {
            return;
        }
        String align = alignStyle(p);
        current.append(align == null ? "<p>" : "<p style=\"text-align: " + align + "\">").append(inner).append("</p>");
    }

    private void closeList() {
        if (openList != null) {
            current.append("</").append(openList).append('>');
            openList = null;
        }
    }

    private String runs(XWPFParagraph p) {
        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : p.getRuns()) {
            for (XWPFPicture pic : r.getEmbeddedPictures()) {
                image(sb, pic);
            }
            String text = r.text();
            if (text == null || text.isEmpty()) {
                continue;
            }
            String html = esc(text).replace("\n", "<br>").replace("\t", "    ");
            STVerticalAlignRun.Enum vertical = r.getVerticalAlignment();
            if (vertical == STVerticalAlignRun.SUPERSCRIPT) {
                html = "<sup>" + html + "</sup>";
            } else if (vertical == STVerticalAlignRun.SUBSCRIPT) {
                html = "<sub>" + html + "</sub>";
            }
            if (r.isStrikeThrough()) {
                html = "<s>" + html + "</s>";
            }
            if (r.getUnderline() != null && r.getUnderline() != UnderlinePatterns.NONE && !(r instanceof XWPFHyperlinkRun)) {
                html = "<u>" + html + "</u>";
            }
            if (r.isItalic()) {
                html = "<em>" + html + "</em>";
            }
            if (r.isBold()) {
                html = "<strong>" + html + "</strong>";
            }
            String color = r.getColor();
            if (color != null && color.matches("[0-9A-Fa-f]{6}") && !"000000".equals(color)) {
                html = "<span style=\"color: #" + color.toLowerCase(Locale.ROOT) + "\">" + html + "</span>";
            }
            if (r instanceof XWPFHyperlinkRun) {
                XWPFHyperlink link = ((XWPFHyperlinkRun) r).getHyperlink(doc);
                String url = link == null ? null : link.getURL();
                if (url != null && url.matches("(?i)^(https?|mailto):.*")) {
                    html = "<a href=\"" + esc(url) + "\">" + html + "</a>";
                }
            }
            sb.append(html);
        }
        return sb.toString();
    }

    private void image(StringBuilder sb, XWPFPicture pic) {
        if (pic.getPictureData() == null) {
            return;
        }
        String ext = pic.getPictureData().suggestFileExtension();
        if (ext == null || !ext.toLowerCase(Locale.ROOT).matches("png|jpe?g|gif|webp")) {
            result.getWarnings().add("跳过不支持的图片格式：" + (ext == null ? "未知" : ext));
            return;
        }
        String assetId;
        try {
            assetId = imageUploader.apply(pic.getPictureData().getData());
        } catch (RuntimeException e) {
            assetId = null;
        }
        if (assetId == null) {
            result.getWarnings().add("有图片上传失败（格式不支持或超过 10MB），已跳过");
            return;
        }
        result.setImages(result.getImages() + 1);
        sb.append("<img ").append(RichHtmlSanitizer.ASSET_ID_ATTR).append("=\"").append(esc(assetId)).append("\">");
    }

    private void table(XWPFTable table) {
        current.append("<table><tbody>");
        for (XWPFTableRow row : table.getRows()) {
            current.append("<tr>");
            for (XWPFTableCell cell : row.getTableCells()) {
                int span = 1;
                CTTcPr pr = cell.getCTTc().getTcPr();
                if (pr != null && pr.getGridSpan() != null && pr.getGridSpan().getVal() != null) {
                    span = pr.getGridSpan().getVal().intValue();
                }
                current.append(span > 1 ? "<td colspan=\"" + span + "\">" : "<td>");
                boolean any = false;
                for (XWPFParagraph p : cell.getParagraphs()) {
                    String inner = runs(p);
                    if (!inner.isEmpty()) {
                        current.append("<p>").append(inner).append("</p>");
                        any = true;
                    }
                }
                if (!any) {
                    current.append("<p></p>");
                }
                current.append("</td>");
            }
            current.append("</tr>");
        }
        current.append("</tbody></table>");
    }

    /** 0 表示正文；1–9 为标题级别。取段落样式名称或大纲级别。 */
    private int headingLevel(XWPFParagraph p) {
        CTPPr ppr = p.getCTP().getPPr();
        if (ppr != null && ppr.getOutlineLvl() != null && ppr.getOutlineLvl().getVal() != null) {
            int lvl = ppr.getOutlineLvl().getVal().intValue();
            if (lvl < 9) {
                return lvl + 1;
            }
        }
        String styleId = p.getStyleID();
        if (styleId == null || doc.getStyles() == null) {
            return 0;
        }
        XWPFStyle style = doc.getStyles().getStyle(styleId);
        for (int i = 0; style != null && i < 5; i++) {
            Matcher m = HEADING_STYLE.matcher(style.getName() == null ? "" : style.getName().trim());
            if (m.matches()) {
                return Integer.parseInt(m.group(1));
            }
            if (style.getCTStyle().getPPr() != null && style.getCTStyle().getPPr().getOutlineLvl() != null) {
                int lvl = style.getCTStyle().getPPr().getOutlineLvl().getVal().intValue();
                if (lvl < 9) {
                    return lvl + 1;
                }
            }
            style = style.getBasisStyleID() == null ? null : doc.getStyles().getStyle(style.getBasisStyleID());
        }
        Matcher m = HEADING_STYLE.matcher(styleId);
        return m.matches() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static String alignStyle(XWPFParagraph p) {
        if (p.getAlignment() == null) {
            return null;
        }
        switch (p.getAlignment()) {
            case CENTER:
                return "center";
            case RIGHT:
                return "right";
            default:
                return null;
        }
    }

    static String normalize(String text) {
        String t = text == null ? "" : text.trim();
        t = NUMBER_PREFIX.matcher(t).replaceFirst("");
        t = t.replaceAll("[\\s　]+", "").replaceAll("[：:。.]+$", "");
        return t.toLowerCase(Locale.ROOT);
    }

    private static String esc(String s) {
        return Entities.escape(s == null ? "" : s);
    }
}
