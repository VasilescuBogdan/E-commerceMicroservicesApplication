package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.ProductDto;

import java.util.List;

public interface ProductService {

    void addProduct(ProductDto product);

    List<ProductDto> getAllProducts();

    ProductDto getProduct(Long id);

    void deleteProduct(Long id);

    void updateProduct(Long id, ProductDto product);
}
