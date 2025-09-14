package com.phraiz.back.cite.parser;

import com.phraiz.back.cite.dto.response.Creator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KyoboAuthorParser {
    public static List<Creator> getAuthors(String url) {
        List<Creator> creators = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();

            // meta 태그에서 citation_author 속성 가져오기
            Elements authorMetaElements = doc.select("meta[name=citation_author]");

            for (Element meta : authorMetaElements) {
                String authorName = meta.attr("content").trim();
                if (!authorName.isEmpty()) {
                    // Creator DTO로 변환
                    Creator creator = new Creator();
                    creator.setCreatorType("author");
                    creator.setFirstName(null);
                    creator.setLastName(authorName);  // 한글 이름 전체 저장
                    creators.add(creator);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return creators.isEmpty() ? null : creators;
    }

}
