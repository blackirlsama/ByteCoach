package com.shanyangcode.infintechatagent.common;

/**
 * 自定义错误码
 *
 * 注意：91xxx/92xxx 系列错误消息引用自 ValidationError 枚举，遵循单一数据源原则
 */
public enum ErrorCode {
    // ============ 通用错误码（40xxx）============
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    TOKEN_MISSING(40102, "令牌缺失"),
    TOKEN_EXPIRED(40103, "令牌已过期"),
    TOKEN_INVALID(40104, "令牌无效"),
    TOKEN_MISMATCH(40105, "令牌与用户不匹配"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    SENSITIVE_WORD_ERROR(40003, "包含敏感词，请求被拒绝"),

    // ============ 系统错误码（50xxx）============
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    INVALID_PARAMETER_ERROR(50003, "参数校验失败"),
    PLEASE_LOGIN(50004, "请先登录"),
    SAME_LOGIN_CONFLICT(50005, "账号已在其他地方登录"),
    SYSTEM_BUSY(50008, "系统繁忙，请稍后重试"),

    // ============ 用户相关错误码（70xxx）============
    PHONE_EMAIL_ERROR(70000, "手机号/邮箱格式错误"),
    USER_ALREADY_EXISTS(70001, "用户已存在"),
    USER_NOT_EXISTS(70002, "用户不存在"),
    REGISTER_ERROR(70003, "注册失败"),
    LOGIN_ERROR_CODE(70004, "验证码错误"),
    LOGIN_ERROR(70005, "登录失败, 用户名或密码错误"),
    LoginPasswordError(70006, "两次密码不一致"),

    // ============ WebSocket 参数错误码（90xxx）============
    SIGNAL_TYPE_ERROR(90000, "单聊消息必须指定接收者"),
    GROUP_TYPE_ERROR(90001, "群聊消息不需要指定接收者"),
    INVALID_TOKEN(90003, "无效token，请重新登录"),
    USER_EMAIL_LIST_EMPTY(90004, "用户邮箱列表为空，请检查用户服务是否正常或没有用户注册");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}