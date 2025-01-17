package com.certidevs.entity;

import com.certidevs.dto.RatingDTO;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table("product")
// @Accessors(fluent = true)
public class Product {

    @Id
    private Long id;
    private String title;
    private Double price;
    private Integer quantity;
    private Boolean active;
    private LocalDateTime creationDate;
    private Long manufacturerId;

    @Transient
    private Manufacturer manufacturer;
    @Transient
    private List<RatingDTO> ratings = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
