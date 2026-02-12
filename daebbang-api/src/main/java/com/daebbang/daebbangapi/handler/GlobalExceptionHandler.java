package com.daebbang.daebbangapi.handler;

import com.daebbang.daebbangcommon.dto.CommonResponse;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ErrorCode;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<@NonNull CommonResponse<Void>> handlerBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        CommonResponse<Void> response = CommonResponse.error(
                                                        errorCode.getStatus(),
                                                        errorCode.getMessage()
                                                    );

        return ResponseEntity
                            .status(errorCode.getStatus())
                            .body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull CommonResponse<Void>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        CommonResponse<Void> response = CommonResponse.error(
                                                        e.getStatusCode().value(),
                                                        e.getMessage()
                                                    );

        return ResponseEntity
                            .status(response.getStatus())
                            .body(response);
    }
}
