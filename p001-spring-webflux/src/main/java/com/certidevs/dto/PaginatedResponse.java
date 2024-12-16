package com.certidevs.dto;

import lombok.*;
import org.springframework.web.bind.annotation.BindParam;

import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaginatedResponse<T> {
    private List<T> items;
    private Integer page;
    private Integer size;
    private Long total;
}
