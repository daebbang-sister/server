package com.daebbang.daebbangcommon.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonResponse<T> {

    private boolean success;
    private Integer status;
    private String message;
    private T data;

    private CommonResponse(boolean success, Integer status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success(Integer status, String message) {
        return new CommonResponse<>(true, status, message, null);
    }

    public static <T> CommonResponse<T> success(Integer status, String message, T data) {
        return new CommonResponse<>(true, status, message, data);
    }

    public static <T> CommonResponse<T> error(Integer status, String message) {
        return new CommonResponse<>(false, status, message, null);
    }
}
