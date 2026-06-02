package com.company.ai.common.core.exception;

import com.company.ai.common.core.result.R;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.common.core.util.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBizException(BizException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return R.<Void>fail(e.getCode(), e.getMessage())
                .traceId(TraceIdUtil.getTraceId());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return R.<Void>fail(ResultCode.INTERNAL_ERROR)
                .traceId(TraceIdUtil.getTraceId());
    }
}
