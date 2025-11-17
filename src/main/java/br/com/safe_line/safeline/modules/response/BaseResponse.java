package br.com.safe_line.safeline.modules.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class BaseResponse<T> {

    private String message;
    private T data;
    private boolean success;
    private int statusCode;

    public static <T> BaseResponse<T> success(String message, T data, int statusCode) {
        return BaseResponse.<T>builder()
                .message(message)
                .data(data)
                .success(true)
                .statusCode(statusCode)
                .build();
    }

    public static <T> BaseResponse<T> error (String message) {
        return BaseResponse.<T>builder()
                .message(message)
                .data(null)
                .success(false)
                .build();
    }

}
