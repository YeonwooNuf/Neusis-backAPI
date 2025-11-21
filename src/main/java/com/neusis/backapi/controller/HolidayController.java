package com.neusis.backapi.controller;

import com.neusis.backapi.dto.HolidayDto;
import com.neusis.backapi.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class HolidayController {

    private final HolidayService holidayService;

    // 특정 연도 - 월의 공휴일 목록 조회
    @GetMapping("/{year}/{month}/holidays")
    public List<HolidayDto> getHolidays(
            @PathVariable int year,
            @PathVariable int month
    ) {
        return holidayService.getHolidays(year, month);
    }
}