package io.github.raghultech.markdown.swing.integration;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class SwingMarkdownRenderer {

    private static SwingMarkdownRenderer instance;

    public static synchronized SwingMarkdownRenderer getInstance() {
        if (instance == null) {
            instance = new SwingMarkdownRenderer();
        }
        return instance;
    }

    private final Pattern emojiPattern;

    private SwingMarkdownRenderer() {
        // Same pattern as before but precompiled once
        emojiPattern = Pattern.compile(
            "[\u00A9\u00AE\u203C\u2049\u2122\u2139\u2194-\u2199\u21A9\u21AA\u231A\u231B\u2328\u23CF" +
            "\u23E9-\u23F3\u23F8-\u23FA\u24C2\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE\u2600-\u2604\u260E" +
            "\u2611\u2614\u2615\u2618\u261D\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638-\u263A\u2640" +
            "\u2642\u2648-\u2653\u265F\u2660\u2663\u2665\u2666\u2668\u267B\u267E\u267F\u2692-\u2697\u2699" +
            "\u269B\u269C\u26A0\u26A1\u26AA\u26AB\u26B0\u26B1\u26BD\u26BE\u26C4\u26C5\u26C8\u26CE\u26CF" +
            "\u26D1\u26D3\u26D4\u26E9\u26EA\u26F0-\u26F5\u26F7-\u26FA\u26FD\u2702\u2705\u2708-\u270D" +
            "\u270F\u2712\u2714\u2716\u271D\u2721\u2728\u2733\u2734\u2744\u2747\u274C\u274E\u2753-\u2755" +
            "\u2757\u2763\u2764\u2795-\u2797\u27A1\u27B0\u27BF\u2934\u2935\u2B05-\u2B07\u2B1B\u2B1C\u2B50" +
            "\u2B55\u3030\u303D\u3297\u3299" +
            "\uD83C\uDC04\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD92-\uD83C\uDD9A" +
            "\uD83C\uDDE6-\uD83C\uDDFF\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A" +
            "\uD83C\uDE50\uD83C\uDE51" +
            "\uD83C\uDF00-\uD83C\uDFFF" +
            "\uD83D\uDC00-\uD83D\uDDFF" +
            "\uD83D\uDE00-\uD83D\uDE4F\uD83D\uDE80-\uD83D\uDEFF" +
            "\uD83E\uDD00-\uD83E\uDDFF" +
            "\uD83E\uDE00-\uD83E\uDEFF]",
            Pattern.UNICODE_CHARACTER_CLASS
        );
    }

    public synchronized String renderMarkdown(String markdown) {
        if (markdown == null) {
            throw new IllegalArgumentException("Markdown cannot be null.");
        }

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            TaskListExtension.create(),
            StrikethroughExtension.create(),
            GfmUsersExtension.create()
        ));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String html = renderer.render(parser.parse(markdown));
        return processEmojis(html);
     //   return html;
    }

    private String processEmojis(String html) {
        Matcher matcher = emojiPattern.matcher(html);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String emoji = matcher.group();
            String code = toCodePoint(emoji);
            String replacement = String.format(
                "<span style=\"display:inline-block;height:16px;width:16px;\">" +
                "<img alt=\"%s\" src=\"https://github.githubassets.com/images/icons/emoji/unicode/%s.png\" " +
                "width=\"16\" height=\"16\" style=\"vertical-align:middle;\" " +
                "onerror=\"this.style.display='none';this.parentNode.textContent='%s';\">" +
                "</span>",
                emoji, code, emoji
            );
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }




    private String toCodePoint(String emoji) {
        if (emoji.codePointCount(0, emoji.length()) == 1) {
            return Integer.toHexString(emoji.codePointAt(0));
        }
        StringBuilder codePoints = new StringBuilder();
        int offset = 0;
        while (offset < emoji.length()) {
            int codePoint = emoji.codePointAt(offset);
            codePoints.append(Integer.toHexString(codePoint));
            offset += Character.charCount(codePoint);
            if (offset < emoji.length()) {
                codePoints.append("-");
            }
        }
        return codePoints.toString();
    }

    
    public synchronized String wrapInHtml(String bodyHtml, File baseFile, boolean darkMode) {
        String basePath = "";
        if (baseFile != null && baseFile.getParentFile() != null) {
            basePath = baseFile.getParentFile().toURI().toString();
        }

        String styles = darkMode ? DARK_CSS : LIGHT_CSS;

        return String.format("""
            <!DOCTYPE html>
           <html>
            <head>
                <meta charset="UTF-8">
                <base href="%s">
                <style>%s</style>
            </head>
            <body>%s</body>
            </html>
            """, basePath, styles, bodyHtml);
    }

    private  final String LIGHT_CSS = """
    	    body {
    	        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    	        font-size: 13px;
    	        line-height: 1.6;
    	        padding: 20px;
    	        max-width: 800px;
    	        margin: auto;
    	        color: #333;
    	        background-color: #ffffff;
    	    }
    	    h1, h2, h3, h4, h5, h6 {
    	        font-weight: bold;
    	        margin: 1em 0 0.5em;
    	    }
    	    pre {
    	        background-color: #f6f8fa;
    	        border: 1px solid #ddd;
    	        padding: 8px;
    	        overflow: auto;
    	    }
    	    code {
    	        font-family: monospace;
    	        background-color: #eee;
    	        padding: 2px 4px;
    	    }
    	    table {
    	        border-collapse: collapse;
    	        width: 100%;
    	    }
    	    th, td {
    	        border: 1px solid #ccc;
    	        padding: 6px 10px;
    	    }
    	    blockquote {
    	        margin: 1em 0;
    	        padding: 0.5em 1em;
    	        border-left: 4px solid #ccc;
    	        background-color: #f9f9f9;
    	    }
    	    a {
    	        color: #0366d6;
    	        text-decoration: none;
    	    }
    	    a:hover {
    	        text-decoration: underline;
    	    }
    	""";

    	private final String DARK_CSS = """
    	    body {
    	        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    	        font-size: 13px;
    	        line-height: 1.6;
    	        padding: 20px;
    	        max-width: 800px;
    	        margin: auto;
    	        color: #ddd;
    	        background-color: #1e1e1e;
    	    }
    	    h1, h2, h3, h4, h5, h6 {
    	        color: #ffffff;
    	        font-weight: bold;
    	        margin: 1em 0 0.5em;
    	    }
    	    pre {
    	        background-color: #2d2d2d;
    	        border: 1px solid #555;
    	        padding: 8px;
    	        overflow: auto;
    	    }
    	    code {
    	        font-family: monospace;
    	        background-color: #444;
    	        padding: 2px 4px;
    	    }
    	    table {
    	        border-collapse: collapse;
    	        width: 100%;
    	    }
    	    th, td {
    	        border: 1px solid #555;
    	        padding: 6px 10px;
    	    }
    	    blockquote {
    	        margin: 1em 0;
    	        padding: 0.5em 1em;
    	        border-left: 4px solid #555;
    	        background-color: #2a2a2a;
    	    }
    	    a {
    	        color: #58a6ff;
    	        text-decoration: none;
    	    }
    	    a:hover {
    	        text-decoration: underline;
    	    }
    	""";


}
