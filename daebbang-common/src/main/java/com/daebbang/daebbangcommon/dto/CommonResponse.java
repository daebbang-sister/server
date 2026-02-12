package com.daebbang.daebbangcommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonResponse<T> {

    private boolean success;
    private Integer status;
    private String message;
    private T data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;

    private CommonResponse(boolean success, Integer status, String message, T data, List<ValidationError> errors) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> CommonResponse<T> success(Integer status, String message) {
        return new CommonResponse<>(true, status, message, null, null);
    }

    public static <T> CommonResponse<T> success(Integer status, String message, T data) {
        return new CommonResponse<>(true, status, message, data, null);
    }

    public static <T> CommonResponse<T> error(Integer status, String message) {
        return new CommonResponse<>(false, status, message, null, null);
    }

    public static <T> CommonResponse<T> error(Integer status, String message, List<ValidationError> errors) {
        return new CommonResponse<>(false, status, message, null, errors);
    }

    @Builder
    public record ValidationError(String field, String message) {
        public static ValidationError of(final FieldError fieldError) {
                return ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
            }
        }
}
