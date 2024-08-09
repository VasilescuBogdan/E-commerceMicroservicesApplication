package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateProductDto;
import com.bogdan.shop.controllers.models.GetProductDto;
import com.bogdan.shop.controllers.models.GetReviewProductDto;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Override
    public void addProduct(CreateProductDto product) {
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
    public List<GetProductDto> getAllProducts() {
        return repository.findAll()
                         .stream()
                         .map(this::mapProductToGetProductDto)
                         .toList();
    }

    @Override
    public GetProductDto getProduct(Long id) {
        return repository.findById(id)
                         .map(this::mapProductToGetProductDto)
                         .orElseThrow(() -> new RuntimeException("Product with id " + id + " not found!"));
    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void updateProduct(Long id, CreateProductDto product) {
        Product updatedProduct = repository.findById(id)
                                           .orElseThrow(
                                                   () -> new RuntimeException("Product with id " + id + " not found!"));
        updatedProduct.setName(product.name());
        updatedProduct.setPrice(product.price());
        updatedProduct.setDescription(product.description());
        repository.save(updatedProduct);
    }

    private GetProductDto mapProductToGetProductDto(Product product) {
        return GetProductDto.builder()
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .reviews(product.getReviews()
                                            .stream()
                                            .map(review -> GetReviewProductDto.builder()
                                                                              .sender(review.getSender())
                                                                              .message(review.getMessage())
                                                                              .numberOfStars(review.getNumberOfStars())
                                                                              .build())
                                            .toList())
                            .build();
    }
}
