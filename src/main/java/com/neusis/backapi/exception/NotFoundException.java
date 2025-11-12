package com.neusis.backapi.exception;

// 전역 예외 처리 구조를 만들기 위한 기반
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
