package com.neusis.backapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDto {
    private String date;      // yyyy-MM-dd
    private String name;      // 공휴일 이름
}