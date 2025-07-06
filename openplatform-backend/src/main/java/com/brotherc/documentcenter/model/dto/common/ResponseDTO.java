package com.brotherc.documentcenter.model.dto.common;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    private Boolean success;

    private Integer code;

    private String message;

    private T data;

    public static <T> ResponseDTO<T> success(String message, T data) {
        ResponseDTO<T> response = success();
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ResponseDTO<T> success(T data) {
        ResponseDTO<T> response = success();
        response.setData(data);
        return response;
    }

    public static <T> ResponseDTO<T> fail(Integer code, String message, T data) {
        ResponseDTO<T> response = fail();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ResponseDTO<T> success() {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setCode(0);
        return response;
    }

    private static <T> ResponseDTO<T> fail() {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setSuccess(false);
        return response;
    }

}
