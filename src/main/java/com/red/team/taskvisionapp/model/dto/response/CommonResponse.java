package com.red.team.taskvisionapp.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonResponse<T> {
    private Integer statusCode;
    private String message;
    private T data;
    private String error;

    public CommonResponse(String message, T data, int statusCode) {
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }
}
