package com.phraiz.back.member.service;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.output.Citation;

import net.minidev.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;

@Service
public class CiteService {
    public String generateCite(String style, JSONObject cslJson){
        try {
            // jsonObject 를 cslItemData 로 변환
            // JSONObject를 CSLItemData로 변환
            CSLItemData itemData = jsonToCSLItemData(cslJson);

            // ListItemDataProvider 사용 (이미 구현된 클래스)
            ListItemDataProvider provider = new ListItemDataProvider(itemData);

            String styleContent = loadStyleContent(style);
            CSL csl = new CSL(provider, styleContent);

            List<Citation> citations = csl.makeCitation(cslJson.getAsString("id"));
            Citation citation = citations.get(0);

            return citation.getText();
        } catch (IOException e) {
            throw new RuntimeException("인용문 생성 실패",e);
        }

    }

    private String loadStyleContent(String style) {
        try {
            // 리소스 폴더에서 로드
            ClassPathResource resource = new ClassPathResource("csl-styles/" + style + ".csl");
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private CSLItemData jsonToCSLItemData(JSONObject cslJson) {
        try {
            // jsonObject->String
            String jsonString = cslJson.toString();
            // String->Reader
            StringReader sr = new StringReader(jsonString);
            JsonLexer lexer = new JsonLexer(sr);
            JsonParser parser = new JsonParser(lexer);

            return CSLItemData.fromJson(parser.parseObject());

        } catch (Exception e) {
            throw new RuntimeException("CSLItemData 변환 실패", e);
        }
        }
}
