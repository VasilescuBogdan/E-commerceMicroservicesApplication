package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateProductDto;
import com.bogdan.shop.controllers.models.GetProductDto;

import java.util.List;

public interface ProductService {

    void addProduct(CreateProductDto product);

    List<GetProductDto> getAllProducts();

    GetProductDto getProduct(Long id);

    void deleteProduct(Long id);

    void updateProduct(Long id, CreateProductDto product);
}
