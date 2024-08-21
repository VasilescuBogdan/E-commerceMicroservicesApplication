package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateUpdateProduct;
import com.bogdan.shop.controllers.models.GetProductDetails;
import com.bogdan.shop.controllers.models.GetReview;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.ProductService;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public void addProduct(CreateUpdateProduct product) {
        Product newProduct = Product.builder()
                                    .name(product.name())
                                    .description(product.description())
                                    .price(product.price())
                                    .orders(new ArrayList<>())
                                    .reviews(new ArrayList<>())
                                    .build();
        repository.save(newProduct);
    }

    @Override
    public List<GetProductDetails> getAllProducts() {
        return repository.findAll()
                         .stream()
                         .map(this::mapProductToGetProductDetailsDto)
                         .toList();
    }

    @Override
    public GetProductDetails getProduct(Long id) {
        return repository.findById(id)
                         .map(this::mapProductToGetProductDetailsDto)
                         .orElseThrow(() -> new ResourceDoesNotExistException("Product with id " + id + " not found!"));
    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void updateProduct(Long id, CreateUpdateProduct product) {
        Product updatedProduct = repository.findById(id)
                                           .orElseThrow(() -> new ResourceDoesNotExistException(
                                                   "Product with id " + id + " not found!"));
        updatedProduct.setName(product.name());
        updatedProduct.setPrice(product.price());
        updatedProduct.setDescription(product.description());
        repository.save(updatedProduct);
    }

    private GetProductDetails mapProductToGetProductDetailsDto(Product product) {
        return GetProductDetails.builder()
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .reviews(product.getReviews()
                                                   .stream()
                                                   .map(review -> GetReview.builder()
                                                                           .sender(review.getSender())
                                                                           .message(review.getMessage())
                                                                           .numberOfStars(review.getNumberOfStars())
                                                                           .build())
                                                   .toList())
                                .build();
    }
}
