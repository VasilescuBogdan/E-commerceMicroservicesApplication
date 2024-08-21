package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateUpdateProduct;
import com.bogdan.shop.controllers.models.GetProductDetails;

import java.util.List;

public interface ProductService {

    void addProduct(CreateUpdateProduct product);

    List<GetProductDetails> getAllProducts();

    GetProductDetails getProduct(Long id);

    void deleteProduct(Long id);

    void updateProduct(Long id, CreateUpdateProduct product);
}
