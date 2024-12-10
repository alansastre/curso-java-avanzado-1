package com.certidevs.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table("manufacturer")
public class Manufacturer {

    @Id
    private Long id;
    private String name;
    private String country;
    private Integer foundationYear;

    @Transient
    private List<Product> products = new ArrayList<>();
}
