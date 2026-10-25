/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.service.rdms.rich.RichRenderModelService;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocGrid;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按 GB/T 9704—2012 党政机关公文格式生成 .docx（实现方案第 10.1 节）：
 * A4，天头 37mm、下白边 35mm、订口 28mm、翻口 26mm，版心每页 22 行、每行 28 字；
 * 标题 2 号小标宋，正文 3 号仿宋、首行缩进 2 字；层次序数“一、”黑体、“（一）”楷体、“1.”仿宋加粗、“（1）”仿宋；
 * 页码 4 号“— 1 —”；正文默认黑色，keepColor 时保留原文字颜色。字体名称由 Collabora 的 fontconfig 映射到开源字体。
 */
public class GbtDocxWriter {

    public static final String FONT_TITLE = "方正小标宋简体";
    public static final String FONT_BODY = "仿宋_GB2312";
    public static final String FONT_HEI = "黑体";
    public static final String FONT_KAI = "楷体_GB2312";
    public static final String FONT_SONG = "宋体";
    public static final String FONT_MONO = "Courier New";

    private static final int BODY_PT = 16;
    private static final int TITLE_PT = 22;
    private static final int TABLE_PT = 14;
    private static final int PAGE_NUM_PT = 14;
    /** 版心 225mm / 22 行，向下取整到 1/20 磅，四舍五入会使 22 行超出版心而只排 21 行。 */
    private static final double LINE_PT = Math.floor(225.0 / 22 / 25.4 * 72 * 20) / 20;
    /** 版心宽 156mm。 */
    private static final double TEXT_WIDTH_PT = 156.0 / 25.4 * 72;
    private static final int FIRST_LINE_TWIPS = BODY_PT * 20 * 2;
    private static final String[] CN_NUM = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
    private static final Pattern COLOR = Pattern.compile("(?i)(?:^|;)\\s*color\\s*:\\s*([^;]+)");
    private static final Pattern ALIGN = Pattern.compile("(?i)text-align\\s*:\\s*(left|center|right|justify)");
    private static final Pattern RGB = Pattern.compile("(?i)rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)");

    private final boolean keepColor;
    /** 按图片 ID 取字节，取不到时返回 null。 */
    private final Function<String, byte[]> imageLoader;
    private final String watermark;

    private XWPFDocument doc;
    private int h2;
    private int h3;
    private int h4;

    public GbtDocxWriter(boolean keepColor, Function<String, byte[]> imageLoader, String watermark) {
        this.keepColor = keepColor;
        this.imageLoader = imageLoader;
        this.watermark = watermark;
    }

    public byte[] write(RichRenderModelService.Model model) throws IOException {
        doc = new XWPFDocument();
        try {
            setupPage();
            writeHead(model);
            int index = 0;
            for (RichRenderModelService.Section s : model.getSections()) {
                index++;
                XWPFParagraph p = bodyParagraph(true);
                XWPFRun r = p.createRun();
                font(r, FONT_HEI, BODY_PT);
                r.setText(cnNumber(index) + "、" + s.getName());
                h2 = 0;
                h3 = 0;
                h4 = 0;
                String html = s.getHtml();
                boolean hasContent = StringUtils.hasText(html)
                        && (StringUtils.hasText(Jsoup.parse(html).text()) || html.contains("<img"));
                if (hasContent) {
                    blocks(Jsoup.parseBodyFragment(s.getHtml()).body(), 0);
                } else {
                    XWPFParagraph empty = bodyParagraph(true);
                    run(empty, "无。", new Style());
                }
            }
            writeSignature(model);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        } finally {
            doc.close();
        }
    }

    private void setupPage() {
        CTSectPr sect = doc.getDocument().getBody().isSetSectPr()
                ? doc.getDocument().getBody().getSectPr() : doc.getDocument().getBody().addNewSectPr();
        CTPageSz size = sect.isSetPgSz() ? sect.getPgSz() : sect.addNewPgSz();
        size.setW(BigInteger.valueOf(mm(210)));
        size.setH(BigInteger.valueOf(mm(297)));
        CTPageMar mar = sect.isSetPgMar() ? sect.getPgMar() : sect.addNewPgMar();
        mar.setTop(BigInteger.valueOf(mm(37)));
        mar.setBottom(BigInteger.valueOf(mm(35)));
        mar.setLeft(BigInteger.valueOf(mm(28)));
        mar.setRight(BigInteger.valueOf(mm(26)));
        mar.setHeader(BigInteger.valueOf(mm(15)));
        mar.setFooter(BigInteger.valueOf(mm(25)));
        // 三号字 28 字宽于版心 156mm；不用字符网格（LibreOffice 遇字宽大于格距时每字占两格），改为收紧字距后两端对齐
        CTStyles styles = CTStyles.Factory.newInstance();
        CTStyle normal = styles.addNewStyle();
        normal.setType(STStyleType.PARAGRAPH);
        normal.setStyleId("Normal");
        normal.setDefault(true);
        normal.addNewName().setVal("Normal");
        bodyDefaults(styles.addNewDocDefaults().addNewRPrDefault().addNewRPr());
        bodyDefaults(normal.addNewRPr());
        doc.createStyles().setStyles(styles);
        CTDocGrid grid = sect.isSetDocGrid() ? sect.getDocGrid() : sect.addNewDocGrid();
        grid.setType(STDocGrid.LINES);
        grid.setLinePitch(BigInteger.valueOf(Math.round(LINE_PT * 20)));

        XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph p = footer.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        offGrid(p);
        XWPFRun left = p.createRun();
        font(left, FONT_SONG, PAGE_NUM_PT);
        left.setText("— ");
        CTSimpleField field = p.getCTP().addNewFldSimple();
        field.setInstr("PAGE \\* MERGEFORMAT");
        XWPFRun num = new XWPFRun(field.addNewR(), p);
        font(num, FONT_SONG, PAGE_NUM_PT);
        num.setText("1");
        XWPFRun right = p.createRun();
        font(right, FONT_SONG, PAGE_NUM_PT);
        right.setText(" —");

        if (StringUtils.hasText(watermark)) {
            XWPFHeaderFooterPolicy policy = doc.getHeaderFooterPolicy() != null
                    ? doc.getHeaderFooterPolicy() : doc.createHeaderFooterPolicy();
            policy.createWatermark(watermark);
        }
    }

    private void writeHead(RichRenderModelService.Model m) {
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        title.setSpacingBetween(LINE_PT * 1.3, LineSpacingRule.EXACT);
        offGrid(title);
        XWPFRun tr = title.createRun();
        font(tr, FONT_TITLE, TITLE_PT);
        tr.setText(m.getTitle());

        XWPFParagraph sub = doc.createParagraph();
        sub.setAlignment(ParagraphAlignment.CENTER);
        sub.setSpacingBetween(LINE_PT, LineSpacingRule.EXACT);
        offGrid(sub);
        XWPFRun sr = sub.createRun();
        font(sr, FONT_KAI, BODY_PT);
        sr.setText(m.subtitle());

        if (!m.isLocked()) {
            XWPFParagraph warn = doc.createParagraph();
            warn.setAlignment(ParagraphAlignment.CENTER);
            warn.setSpacingBetween(LINE_PT, LineSpacingRule.EXACT);
            offGrid(warn);
            XWPFRun wr = warn.createRun();
            font(wr, FONT_HEI, BODY_PT);
            wr.setText("本版本未经锁定，不作为研发依据");
        }

        XWPFTable table = doc.createTable(m.getBasicInfo().size(), 2);
        table.setWidth("100%");
        for (int i = 0; i < m.getBasicInfo().size(); i++) {
            String[] row = m.getBasicInfo().get(i);
            cellText(table.getRow(i).getCell(0), row[0], true);
            cellText(table.getRow(i).getCell(1), row[1], false);
            table.getRow(i).getCell(0).setWidth("25%");
            table.getRow(i).getCell(1).setWidth("75%");
        }
        offGrid(table);
        doc.createParagraph().setSpacingBetween(LINE_PT, LineSpacingRule.EXACT);
    }

    private void cellText(XWPFTableCell cell, String text, boolean bold) {
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        XWPFRun r = p.createRun();
        font(r, FONT_BODY, TABLE_PT);
        r.setBold(bold);
        r.setText(text == null ? "" : text);
    }

    private void writeSignature(RichRenderModelService.Model m) {
        doc.createParagraph().setSpacingBetween(LINE_PT, LineSpacingRule.EXACT);
        Date date = m.getSaveTime() == null ? new Date() : m.getSaveTime();
        for (String text : new String[]{m.getOrgName(), new SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(date)}) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.RIGHT);
            p.setIndentationRight(BODY_PT * 20 * 4);
            p.setSpacingBetween(LINE_PT, LineSpacingRule.EXACT);
            XWPFRun r = p.createRun();
            font(r, FONT_BODY, BODY_PT);
            r.setText(text);
        }
    }

    // ---------------- 块级元素 ----------------

    private void blocks(Element parent, int listDepth) {
        XWPFParagraph inlineHost = null;
        for (Node node : parent.childNodes()) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text();
                if (!StringUtils.hasText(text)) {
                    continue;
                }
                if (inlineHost == null) {
                    inlineHost = bodyParagraph(true);
                }
                run(inlineHost, text, new Style());
                continue;
            }
            if (!(node instanceof Element)) {
                continue;
            }
            Element e = (Element) node;
            String tag = e.normalName();
            switch (tag) {
                case "p":
                case "div":
                    if (e.select("> p, > div, > ul, > ol, > table, > blockquote, > pre, > h1, > h2, > h3, > h4").isEmpty()) {
                        XWPFParagraph p = bodyParagraph(true);
                        applyAlign(p, e);
                        inlines(p, e, new Style());
                    } else {
                        blocks(e, listDepth);
                    }
                    inlineHost = null;
                    break;
                case "h1":
                case "h2":
                    heading(e, 2);
                    inlineHost = null;
                    break;
                case "h3":
                    heading(e, 3);
                    inlineHost = null;
                    break;
                case "h4":
                case "h5":
                case "h6":
                    heading(e, 4);
                    inlineHost = null;
                    break;
                case "ul":
                case "ol":
                    list(e, listDepth + 1, "ol".equals(tag));
                    inlineHost = null;
                    break;
                case "table":
                    table(e);
                    inlineHost = null;
                    break;
                case "blockquote": {
                    for (Element child : e.children()) {
                        XWPFParagraph p = bodyParagraph(false);
                        p.setIndentationLeft(FIRST_LINE_TWIPS);
                        inlines(p, child, new Style().italic());
                    }
                    if (e.children().isEmpty()) {
                        XWPFParagraph p = bodyParagraph(false);
                        p.setIndentationLeft(FIRST_LINE_TWIPS);
                        inlines(p, e, new Style().italic());
                    }
                    inlineHost = null;
                    break;
                }
                case "pre": {
                    String[] lines = e.wholeText().split("\\r?\\n", -1);
                    XWPFParagraph p = bodyParagraph(false);
                    offGrid(p);
                    for (int i = 0; i < lines.length; i++) {
                        XWPFRun r = p.createRun();
                        font(r, FONT_MONO, 12);
                        r.setText(lines[i]);
                        if (i < lines.length - 1) {
                            r.addBreak();
                        }
                    }
                    inlineHost = null;
                    break;
                }
                case "hr":
                    inlineHost = null;
                    break;
                case "img": {
                    XWPFParagraph p = doc.createParagraph();
                    p.setAlignment(ParagraphAlignment.CENTER);
                    offGrid(p);
                    image(p, e);
                    inlineHost = null;
                    break;
                }
                default:
                    if (inlineHost == null) {
                        inlineHost = bodyParagraph(true);
                    }
                    inline(inlineHost, e, new Style());
            }
        }
    }

    private void heading(Element e, int level) {
        String prefix;
        XWPFParagraph p = bodyParagraph(false);
        p.setIndentationFirstLine(FIRST_LINE_TWIPS);
        Style style = new Style();
        if (level == 2) {
            h2++;
            h3 = 0;
            h4 = 0;
            prefix = "（" + cnNumber(h2) + "）";
            style.font = FONT_KAI;
        } else if (level == 3) {
            h3++;
            h4 = 0;
            prefix = h3 + ".";
            style.bold = true;
        } else {
            h4++;
            prefix = "（" + h4 + "）";
        }
        run(p, prefix, style);
        inlines(p, e, style);
    }

    private void list(Element list, int depth, boolean ordered) {
        int n = 0;
        for (Element li : list.children()) {
            if (!"li".equals(li.normalName())) {
                continue;
            }
            n++;
            XWPFParagraph p = bodyParagraph(false);
            p.setIndentationLeft(FIRST_LINE_TWIPS * depth);
            run(p, ordered ? n + ". " : "• ", new Style());
            for (Node child : li.childNodes()) {
                if (child instanceof Element && ("ul".equals(((Element) child).normalName())
                        || "ol".equals(((Element) child).normalName()))) {
                    list((Element) child, depth + 1, "ol".equals(((Element) child).normalName()));
                } else if (child instanceof Element && "p".equals(((Element) child).normalName())) {
                    inlines(p, (Element) child, new Style());
                } else if (child instanceof Element) {
                    inline(p, (Element) child, new Style());
                } else if (child instanceof TextNode) {
                    run(p, ((TextNode) child).text(), new Style());
                }
            }
        }
    }

    private void table(Element tableEl) {
        java.util.List<Element> rows = tableEl.select("> tr, > thead > tr, > tbody > tr, > tfoot > tr");
        if (rows.isEmpty()) {
            return;
        }
        int cols = 0;
        for (Element tr : rows) {
            int c = 0;
            for (Element cell : tr.children()) {
                c += Math.max(1, intAttr(cell, "colspan", 1));
            }
            cols = Math.max(cols, c);
        }
        if (cols == 0) {
            return;
        }
        XWPFTable table = doc.createTable(rows.size(), 1);
        table.setWidth("100%");
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = table.getRow(i);
            int cellIndex = 0;
            for (Element cellEl : rows.get(i).children()) {
                XWPFTableCell cell = cellIndex == 0 ? row.getCell(0) : row.addNewTableCell();
                int span = Math.max(1, intAttr(cellEl, "colspan", 1));
                if (span > 1) {
                    cell.getCTTc().addNewTcPr().addNewGridSpan().setVal(BigInteger.valueOf(span));
                }
                XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
                Style style = new Style();
                style.size = TABLE_PT;
                style.bold = "th".equals(cellEl.normalName());
                for (Node child : cellEl.childNodes()) {
                    if (child instanceof Element && "p".equals(((Element) child).normalName())) {
                        if (!p.getRuns().isEmpty()) {
                            p = cell.addParagraph();
                        }
                        inlines(p, (Element) child, style);
                    } else if (child instanceof Element) {
                        inline(p, (Element) child, style);
                    } else if (child instanceof TextNode) {
                        run(p, ((TextNode) child).text(), style);
                    }
                }
                cellIndex++;
            }
        }
        offGrid(table);
        doc.createParagraph();
    }

    // ---------------- 行内元素 ----------------

    private void inlines(XWPFParagraph p, Element parent, Style style) {
        for (Node node : parent.childNodes()) {
            if (node instanceof TextNode) {
                run(p, ((TextNode) node).text(), style);
            } else if (node instanceof Element) {
                inline(p, (Element) node, style);
            }
        }
    }

    private void inline(XWPFParagraph p, Element e, Style parent) {
        Style s = parent.copy();
        switch (e.normalName()) {
            case "strong":
            case "b":
                s.bold = true;
                break;
            case "em":
            case "i":
                s.italic = true;
                break;
            case "u":
                s.underline = true;
                break;
            case "s":
            case "del":
            case "strike":
                s.strike = true;
                break;
            case "sub":
                s.vertical = VerticalAlign.SUBSCRIPT;
                break;
            case "sup":
                s.vertical = VerticalAlign.SUPERSCRIPT;
                break;
            case "code":
                s.font = FONT_MONO;
                break;
            case "a":
                s.underline = true;
                break;
            case "br":
                p.createRun().addBreak();
                return;
            case "img":
                image(p, e);
                return;
            default:
                break;
        }
        if (keepColor) {
            String color = color(e.attr("style"));
            if (color != null) {
                s.color = color;
            }
        }
        inlines(p, e, s);
    }

    private void run(XWPFParagraph p, String text, Style s) {
        if (text == null || text.isEmpty()) {
            return;
        }
        XWPFRun r = p.createRun();
        font(r, s.font == null ? FONT_BODY : s.font, s.size);
        r.setBold(s.bold);
        r.setItalic(s.italic);
        if (s.underline) {
            r.setUnderline(UnderlinePatterns.SINGLE);
        }
        r.setStrikeThrough(s.strike);
        if (s.vertical != null) {
            r.setSubscript(s.vertical);
        }
        r.setColor(keepColor && s.color != null ? s.color : "000000");
        r.setText(text.replace('\u00a0', ' '));
    }

    private void image(XWPFParagraph p, Element img) {
        String assetId = img.attr("data-asset-id");
        byte[] data = StringUtils.hasText(assetId) && imageLoader != null ? imageLoader.apply(assetId) : null;
        if (data == null) {
            run(p, "［图片缺失］", new Style());
            return;
        }
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(data));
            if (bi == null) {
                run(p, "［图片格式不支持打印］", new Style());
                return;
            }
            int type = pictureType(data);
            if (type == 0) {
                ByteArrayOutputStream png = new ByteArrayOutputStream();
                ImageIO.write(bi, "png", png);
                data = png.toByteArray();
                type = Document.PICTURE_TYPE_PNG;
            }
            double widthPt = bi.getWidth() * 0.75;
            double heightPt = bi.getHeight() * 0.75;
            String w = img.attr("width");
            if (w.endsWith("%")) {
                try {
                    widthPt = TEXT_WIDTH_PT * Double.parseDouble(w.substring(0, w.length() - 1)) / 100;
                    heightPt = widthPt * bi.getHeight() / bi.getWidth();
                } catch (NumberFormatException ignore) {
                    // 宽度属性无效时按原始尺寸
                }
            } else if (StringUtils.hasText(w) && w.matches("\\d+(px)?")) {
                widthPt = Integer.parseInt(w.replace("px", "")) * 0.75;
                heightPt = widthPt * bi.getHeight() / bi.getWidth();
            }
            if (widthPt > TEXT_WIDTH_PT) {
                heightPt = heightPt * TEXT_WIDTH_PT / widthPt;
                widthPt = TEXT_WIDTH_PT;
            }
            p.createRun().addPicture(new ByteArrayInputStream(data), type, assetId,
                    Units.toEMU(widthPt), Units.toEMU(heightPt));
        } catch (Exception e) {
            run(p, "［图片缺失］", new Style());
        }
    }

    // ---------------- 工具 ----------------

    private XWPFParagraph bodyParagraph(boolean indent) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.BOTH);
        p.setSpacingBetween(LINE_PT, LineSpacingRule.EXACT);
        if (indent) {
            p.setIndentationFirstLine(FIRST_LINE_TWIPS);
        }
        return p;
    }

    private static void bodyDefaults(CTRPr rpr) {
        rpr.addNewSz().setVal(BigInteger.valueOf(BODY_PT * 2L));
        rpr.addNewSzCs().setVal(BigInteger.valueOf(BODY_PT * 2L));
        rpr.addNewSpacing().setVal(BigInteger.valueOf((long) Math.floor((TEXT_WIDTH_PT / 28 - BODY_PT) * 20)));
    }

    /** 标题、表格、图片等不对齐行网格，按各自行距排列。 */
    private static void offGrid(XWPFParagraph p) {
        CTPPr ppr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
        (ppr.isSetSnapToGrid() ? ppr.getSnapToGrid() : ppr.addNewSnapToGrid()).setVal(STOnOff1.OFF);
    }

    private static void offGrid(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph p : cell.getParagraphs()) {
                    offGrid(p);
                }
            }
        }
    }

    private static void applyAlign(XWPFParagraph p, Element e) {
        Matcher m = ALIGN.matcher(e.attr("style"));
        if (!m.find()) {
            return;
        }
        switch (m.group(1).toLowerCase(Locale.ROOT)) {
            case "center":
                p.setAlignment(ParagraphAlignment.CENTER);
                p.setIndentationFirstLine(0);
                break;
            case "right":
                p.setAlignment(ParagraphAlignment.RIGHT);
                p.setIndentationFirstLine(0);
                break;
            case "left":
                p.setAlignment(ParagraphAlignment.LEFT);
                break;
            default:
                break;
        }
    }

    private static void font(XWPFRun r, String name, int size) {
        r.setFontFamily(name, XWPFRun.FontCharRange.ascii);
        r.setFontFamily(name, XWPFRun.FontCharRange.hAnsi);
        r.setFontFamily(name, XWPFRun.FontCharRange.eastAsia);
        r.setFontFamily(name, XWPFRun.FontCharRange.cs);
        r.setFontSize(size);
        r.setColor("000000");
    }

    static String cnNumber(int n) {
        if (n <= 10) {
            return CN_NUM[n];
        }
        if (n < 20) {
            return "十" + CN_NUM[n - 10];
        }
        if (n < 100) {
            return CN_NUM[n / 10] + "十" + (n % 10 == 0 ? "" : CN_NUM[n % 10]);
        }
        return String.valueOf(n);
    }

    static String color(String style) {
        if (!StringUtils.hasText(style)) {
            return null;
        }
        Matcher m = COLOR.matcher(style);
        if (!m.find()) {
            return null;
        }
        String v = m.group(1).trim();
        if (v.matches("#[0-9a-fA-F]{6}")) {
            return v.substring(1).toUpperCase(Locale.ROOT);
        }
        if (v.matches("#[0-9a-fA-F]{3}")) {
            char[] c = v.substring(1).toUpperCase(Locale.ROOT).toCharArray();
            return "" + c[0] + c[0] + c[1] + c[1] + c[2] + c[2];
        }
        Matcher rgb = RGB.matcher(v);
        if (rgb.find()) {
            return String.format("%02X%02X%02X", Math.min(255, Integer.parseInt(rgb.group(1))),
                    Math.min(255, Integer.parseInt(rgb.group(2))), Math.min(255, Integer.parseInt(rgb.group(3))));
        }
        return null;
    }

    private static int pictureType(byte[] d) {
        if (d.length > 8 && (d[0] & 0xff) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return Document.PICTURE_TYPE_PNG;
        }
        if (d.length > 3 && (d[0] & 0xff) == 0xFF && (d[1] & 0xff) == 0xD8) {
            return Document.PICTURE_TYPE_JPEG;
        }
        if (d.length > 6 && d[0] == 'G' && d[1] == 'I' && d[2] == 'F') {
            return Document.PICTURE_TYPE_GIF;
        }
        return 0;
    }

    private static int intAttr(Element e, String name, int def) {
        try {
            return StringUtils.hasText(e.attr(name)) ? Integer.parseInt(e.attr(name).trim()) : def;
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    private static int mm(double v) {
        return (int) Math.round(v / 25.4 * 1440);
    }

    private static final class Style {
        String font;
        int size = BODY_PT;
        boolean bold;
        boolean italic;
        boolean underline;
        boolean strike;
        VerticalAlign vertical;
        String color;

        Style copy() {
            Style s = new Style();
            s.font = font;
            s.size = size;
            s.bold = bold;
            s.italic = italic;
            s.underline = underline;
            s.strike = strike;
            s.vertical = vertical;
            s.color = color;
            return s;
        }

        Style italic() {
            italic = true;
            return this;
        }
    }
}
