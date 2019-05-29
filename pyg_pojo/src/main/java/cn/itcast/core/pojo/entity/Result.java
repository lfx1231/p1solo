package cn.itcast.core.pojo.entity;

import java.io.Serializable;

public class Result implements Serializable {

    //如果为true表示操作成功, false操作失败
    private boolean success;
    //放成功或者失败信息
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
