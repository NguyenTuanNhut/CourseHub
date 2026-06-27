package com.k24.coursegradingmanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CourseFullException extends RuntimeException {
    public CourseFullException(String message) {
        super(message);
    }
}
