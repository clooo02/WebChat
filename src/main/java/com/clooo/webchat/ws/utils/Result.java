package com.clooo.webchat.ws.utils;

public class Result {

    private Object data;

    private Boolean success;

    private String error;

    private Result(Object data, Boolean success, String error) {
        this.data = data;
        this.success = success;
        this.error = error;
    }

    public static Result ok(Object data) {
        return new Result(data, true, null);
    }

    public static Result ok() {
        return new Result(null, true, null);
    }

    public static Result error(String error) {
        return new Result(null, false, error);
    }

    public static Result error() {
        return new Result(null, false, null);
    }

    @Override
    public String toString() {
        return "Result{" +
                "data=" + data +
                ", success=" + success +
                ", error='" + error + '\'' +
                '}';
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
