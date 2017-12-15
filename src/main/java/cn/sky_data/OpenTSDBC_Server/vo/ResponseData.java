package cn.sky_data.OpenTSDBC_Server.vo;

/**
 * Created by chenhaonee on 2017/5/12.
 */
public class ResponseData<T> {

    public static final int RESULT_OK = 200;
    public static final int RESULT_NG = 500;
    public static final int RESULT_PARAM_ERROR = 400;


    /**
     * 数据
     */
    private T data;
    /**
     * 错误描述
     */
    private String description;
    /**
     * 错误码
     */
    private int resultCode;

    public ResponseData(T data) {
        this.data = data;
        this.description = null;
        this.resultCode = 200;
    }

    public ResponseData(T data, String description, int resultCode) {
        this.data = data;
        this.description = description;
        this.resultCode = resultCode;
    }

    public ResponseData(String description, int resultCode) {
        this.data = null;
        this.description = description;
        this.resultCode = resultCode;
    }

    public ResponseData() {
        this.description = null;
        this.resultCode = 200;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
}
