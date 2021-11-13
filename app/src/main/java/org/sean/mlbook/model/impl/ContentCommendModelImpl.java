//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.model.impl;

import org.sean.mlbook.ErrorAnalyContentManager;
import org.sean.mlbook.bean.BookContentBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

import java.util.List;

public class ContentCommendModelImpl {

    private ContentCommendModelImpl() {

    }

    public static ContentCommendModelImpl getInstance() {
        return new ContentCommendModelImpl();
    }

    public BookContentBean analyBookcontent(BookContentBean bookContentBean, String s, String realUrl) throws Exception {
        ErrorAnalyContentManager.getInstance().writeNewErrorUrl(realUrl);
        try {
            Document doc = Jsoup.parse(s);
            List<TextNode> contentEs = doc.getElementById("content").textNodes();
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < contentEs.size(); i++) {
                String temp = contentEs.get(i).text().trim();
                temp = temp.replaceAll(" ", "").replaceAll(" ", "");
                if (temp.length() > 0) {
                    content.append("\u3000\u3000" + temp);
                    if (i < contentEs.size() - 1) {
                        content.append("\r\n");
                    }
                }
            }
            bookContentBean.setDurCapterContent(content.toString());
        } catch (Exception e) {
            e.printStackTrace();
            bookContentBean.setDurCapterContent(realUrl.substring(0, realUrl.indexOf('/', 8)) + "站点暂时不支持解析");
            bookContentBean.setRight(false);
        }
        return bookContentBean;
    }
}
