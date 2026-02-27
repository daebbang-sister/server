package com.daebbang.daebbangapi.filter;

import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@NullMarked
@RequiredArgsConstructor
public class CustomExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            log.error("[Filter BusinessException] URI: {}, Error: {}", request.getRequestURI(), e.getErrorCode().getMessage());
            writeErrorResponse(response, e.getErrorCode(), mapper);
        } catch (Exception e) {
            log.error("[Filter Unknown Exception] URI: {}, Message: {}", request.getRequestURI(), e.getMessage(), e);
            writeErrorResponse(response, CommonErrorCode.INTERNAL_SERVER_ERROR, mapper);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, ObjectMapper mapper) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus());

        String body = mapper.writeValueAsString(CommonResponse.error(errorCode));
        response.getWriter().write(body);
    }
}
