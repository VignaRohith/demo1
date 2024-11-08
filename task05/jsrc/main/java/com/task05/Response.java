package com.task05;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private int statusCode;
    private Event event;
}
