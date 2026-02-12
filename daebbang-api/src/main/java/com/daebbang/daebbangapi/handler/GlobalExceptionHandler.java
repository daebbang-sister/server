package com.daebbang.daebbangapi.handler;

import com.daebbang.daebbangcommon.dto.CommonResponse;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.ErrorCode;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull CommonResponse<Object>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_DATA;
        CommonResponse<Object> response = makeErrorResponse(errorCode, e);
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
