package com.task05;

import lombok.*;

import java.util.Map;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestData {
    private int principalId;
    private Map<String, String> content;
}
