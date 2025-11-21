package com.neusis.backapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusis.backapi.dto.HolidayDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {

    @Value("${openapi.holiday.service-key}")
    private String serviceKey;

    private static final String BASE_URL =
            "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo";

    public List<HolidayDto> getHolidays(int year, int month) {
        try {
            String solMonth = String.format("%02d", month);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("_type", "json")
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 100)
                    .queryParam("solYear", year)
                    .queryParam("solMonth", solMonth);

            String url = builder.toUriString();
            log.info("Holiday API Request: {}", url);

            RestTemplate restTemplate = new RestTemplate();
            String body = restTemplate.getForObject(url, String.class);
            log.info("Holiday API Response raw: {}", body);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> root = mapper.readValue(body, new TypeReference<>() {});

            Map<String, Object> response = (Map<String, Object>) root.get("response");
            Map<String, Object> resBody  = (Map<String, Object>) response.get("body");

            // totalCount 0일 때 대비
            Integer totalCount = (Integer) resBody.get("totalCount");
            if (totalCount == null || totalCount == 0) {
                return List.of();
            }

            Object itemsObj = resBody.get("items");
            if (itemsObj == null || itemsObj instanceof String) {
                // items="" 인 케이스
                return List.of();
            }

            Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
            Object itemObj = itemsMap.get("item");

            List<Map<String, Object>> itemList;

            if (itemObj instanceof List) {
                // 여러 건
                itemList = (List<Map<String, Object>>) itemObj;
            } else if (itemObj instanceof Map) {
                // 한 건만 있을 때
                itemList = List.of((Map<String, Object>) itemObj);
            } else {
                return List.of();
            }

            return itemList.stream()
                    // 공휴일만 쓰고 싶으면 isHoliday == "Y" 필터
                    .filter(m -> "Y".equals(String.valueOf(m.get("isHoliday"))))
                    .map(m -> {
                        String locdate = String.valueOf(m.get("locdate")); // 20250228 이런 형식
                        String dateName = String.valueOf(m.get("dateName"));

                        // "YYYYMMDD" → "YYYY-MM-DD" 로 변환
                        String isoDate = locdate.length() == 8
                                ? locdate.substring(0, 4) + "-" +
                                locdate.substring(4, 6) + "-" +
                                locdate.substring(6, 8)
                                : locdate;

                        return new HolidayDto(isoDate, dateName);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("Holiday parsing error", e);
            return List.of();
        }
    }
}