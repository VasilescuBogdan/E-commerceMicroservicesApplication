package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateUpdateProductDto;
import com.bogdan.shop.controllers.models.GetProductDetailsDto;

import java.util.List;

public interface ProductService {

    void addProduct(CreateUpdateProductDto product);

    List<GetProductDetailsDto> getAllProducts();

    GetProductDetailsDto getProduct(Long id);

    void deleteProduct(Long id);

    void updateProduct(Long id, CreateUpdateProductDto product);
}
