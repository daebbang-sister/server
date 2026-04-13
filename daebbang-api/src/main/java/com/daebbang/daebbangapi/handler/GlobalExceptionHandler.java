package com.daebbang.daebbangapi.handler;

import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        CommonResponse<Object> response = makeErrorResponse(errorCode);
        return handleExceptionInternal(response);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerConstraintViolationException(ConstraintViolationException e) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_DATA;
        List<CommonResponse.ValidationError> errors = e.getConstraintViolations()
            .stream()
            .map(violation -> {
                String path = violation.getPropertyPath().toString();
                String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                return CommonResponse.ValidationError.builder()
                    .field(field)
                    .message(violation.getMessage())
                    .build();
            })
            .toList();
        CommonResponse<Object> response = CommonResponse.error(errorCode.getStatus(), errorCode.getMessage(), errors);
        return handleExceptionInternal(response);
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerBindException(BindException e) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_DATA;
        CommonResponse<Object> response = makeErrorResponse(errorCode, e);
        return handleExceptionInternal(response);
    }

    @ExceptionHandler(value = MissingRequestCookieException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerMissingRequestCookieException(MissingRequestCookieException e) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_DATA;
        CommonResponse<Object> response = makeErrorResponse(errorCode);
        return handleExceptionInternal(response);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_DATA;
        List<CommonResponse.ValidationError> errors = List.of(
            CommonResponse.ValidationError.builder()
                .field(e.getParameterName())
                .message(e.getParameterName() + " 입력값은 필수입니다.")
                .build()
        );
        CommonResponse<Object> response = CommonResponse.error(errorCode.getStatus(), errorCode.getMessage(), errors);
        return handleExceptionInternal(response);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorCode errorCode = CommonErrorCode.DUPLICATE_RESOURCE;
        CommonResponse<Object> response = makeErrorResponse(errorCode);
        return handleExceptionInternal(response);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerException(Exception e) {
        log.error("예상치 못한 서버 에러 발생 (Unhandled Exception) : ", e);
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        CommonResponse<Object> response = makeErrorResponse(errorCode);
        return handleExceptionInternal(response);
    }

    private ResponseEntity<@NonNull CommonResponse<Object>> handleExceptionInternal(CommonResponse<Object> response) {
        return ResponseEntity
                            .status(response.getStatus())
                            .body(response);
    }

    private CommonResponse<Object> makeErrorResponse(ErrorCode errorCode) {
        return CommonResponse.error(
                                errorCode.getStatus(),
                                errorCode.getMessage()
                            );
    }

    private CommonResponse<Object> makeErrorResponse(ErrorCode errorCode, BindException e) {
        List<CommonResponse.ValidationError> errors = e.getBindingResult()
                                                        .getFieldErrors()
                                                        .stream()
                                                        .map(CommonResponse.ValidationError::of)
                                                        .toList();

        return CommonResponse.error(
                                errorCode.getStatus(),
                                errorCode.getMessage(),
                                errors
                            );
    }
}
