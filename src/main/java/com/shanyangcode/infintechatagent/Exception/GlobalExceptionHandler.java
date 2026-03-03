package com.shanyangcode.infintechatagent.Exception;


import java.util.HashMap;
import java.util.Map;


import com.shanyangcode.infintechatagent.common.BaseResponse;
import com.shanyangcode.infintechatagent.common.ErrorCode;
import com.shanyangcode.infintechatagent.common.ResultUtils;
import dev.langchain4j.guardrail.InputGuardrailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        String message = errors.toString();
        return ResultUtils.error(ErrorCode.INVALID_PARAMETER_ERROR, message);
    }


    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public BaseResponse<?> handlerMissingServletRequestParameterException(Exception e) {
        log.error("缺少必填参数:{}", e.toString());
        return ResultUtils.error(ErrorCode.INVALID_PARAMETER_ERROR, "缺少必填参数");
    }

    @ExceptionHandler(InputGuardrailException.class)
    public BaseResponse<?> inputGuardrailExceptionHandler(InputGuardrailException e) {
        log.error("敏感词拦截: {}", e.getMessage());
        // 直接从异常信息里获取提示内容返回给前端
        // 或者统一返回 SENSITIVE_WORD_ERROR
        return ResultUtils.error(ErrorCode.SENSITIVE_WORD_ERROR);
    }
}