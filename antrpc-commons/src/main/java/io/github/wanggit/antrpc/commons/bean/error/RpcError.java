package io.github.wanggit.antrpc.commons.bean.error;

public enum RpcError {
    CLASS_NOT_FOUND("class_not_found", "class not found."),
    ARGUMENT_CLASS_NOT_FOUND("argument_class_not_found", "argument class not found."),
    METHOD_NOT_FOUND("method_not_found", "method not found."),
    MANY_BEANS("many_beans", "find multiple implementation classes for an interface."),
    NO_BEANS("no_beans", "cannot found implementation classes for an interface."),
    ;

    private String code;
    private String message;

    RpcError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
