package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateUpdateProductDto;
import com.bogdan.shop.controllers.models.GetProductDetailsDto;
import com.bogdan.shop.controllers.models.GetReviewDto;
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
    public void addProduct(CreateUpdateProductDto product) {
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
    public List<GetProductDetailsDto> getAllProducts() {
        return repository.findAll()
                         .stream()
                         .map(this::mapProductToGetProductDetailsDto)
                         .toList();
    }

    @Override
    public GetProductDetailsDto getProduct(Long id) {
        return repository.findById(id)
                         .map(this::mapProductToGetProductDetailsDto)
                         .orElseThrow(() -> new ResourceDoesNotExistException("Product with id " + id + " not found!"));
    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void updateProduct(Long id, CreateUpdateProductDto product) {
        Product updatedProduct = repository.findById(id)
                                           .orElseThrow(() -> new ResourceDoesNotExistException(
                                                   "Product with id " + id + " not found!"));
        updatedProduct.setName(product.name());
        updatedProduct.setPrice(product.price());
        updatedProduct.setDescription(product.description());
        repository.save(updatedProduct);
    }

    private GetProductDetailsDto mapProductToGetProductDetailsDto(Product product) {
        return GetProductDetailsDto.builder()
                                   .name(product.getName())
                                   .description(product.getDescription())
                                   .price(product.getPrice())
                                   .reviews(product.getReviews()
                                                   .stream()
                                                   .map(review -> GetReviewDto.builder()
                                                                              .sender(review.getSender())
                                                                              .message(review.getMessage())
                                                                              .numberOfStars(review.getNumberOfStars())
                                                                              .build())
                                                   .toList())
                                   .build();
    }
}
