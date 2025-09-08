package com.phraiz.back.cite.parser;


import com.phraiz.back.cite.dto.response.Creator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// dbpia용 author 이름 추출
public class DbpiaAuthorParser {
    public static List<Creator> getAuthor(String url) {
        List<Creator> creators = new ArrayList<>();
        try {
            // 1. 해당 URL에 접속하여 HTML 문서 전체를 가져오기
            Document doc = Jsoup.connect(url).get();

            // 2. 저자 이름이 담긴 <a class="authorName"> 태그를 모두 찾기
            Elements authorElements = doc.select("a.authorName");

            // 3. 찾은 요소들을 순회하며 각 저자 이름을 추출
            for (Element element : authorElements) {
                String authorName = element.text().trim();
                if (!authorName.isEmpty()) {
                    Creator creator = new Creator();
                    creator.setCreatorType("author");
                    creator.setFirstName(null);
                    creator.setLastName(authorName);
                    creators.add(creator);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 저자를 찾지 못하면 null, 찾았으면 creators 리스트 반환
        return creators.isEmpty() ? null : creators;
    }
}
