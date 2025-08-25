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

public class KissAuthorParser {
    public static List<Creator> getAuthor(String url) {
        List<Creator> creators = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();

            // 저자 이름이 담긴 div.author 태그를 찾음
            Element authorElement = doc.select("div.author.mb-1").first();

            if (authorElement != null) {
                String fullText = authorElement.text();

                // 정규 표현식을 사용해 한국 이름을 추출
                // 예: "백승익 ( Baek Seung-ik )"에서 "백승익"만 추출
                Pattern pattern = Pattern.compile("([가-힣]+)\\s*\\(");
                Matcher matcher = pattern.matcher(fullText);

                while (matcher.find()) {
                    String authorName = matcher.group(1).trim();
                    creators.add(createCreator(authorName));
                }

                // 만약 정규 표현식이 실패할 경우 쉼표로 분리
                if (creators.isEmpty()) {
                    String[] names = fullText.split("[,()]");
                    for (String name : names) {
                        String cleanName = name.replaceAll("\\(.*?\\)", "").trim();
                        if (!cleanName.isEmpty()) {
                            creators.add(createCreator(cleanName));
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return creators.isEmpty() ? null : creators;
    }

    private static Creator createCreator(String name) {
        Creator creator = new Creator();
        creator.setCreatorType("author");
        creator.setFirstName(null);
        creator.setLastName(name);
        return creator;
    }
}
