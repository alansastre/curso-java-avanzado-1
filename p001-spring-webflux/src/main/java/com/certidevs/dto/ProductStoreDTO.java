package com.certidevs.dto;

public record ProductStoreDTO(
Long id,
String title,
Double price,
String description,
String category,
String image,
RatingDTO rating
) {
}
