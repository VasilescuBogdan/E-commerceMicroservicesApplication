package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateUpdateProduct;
import com.bogdan.shop.controllers.models.GetProductDetails;
import com.bogdan.shop.controllers.models.GetReview;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.entities.Review;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.services.impl.ProductServiceImpl;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl productService;

    private List<Product> productList;

    @BeforeEach
    void setUp() {
        Product product1 = Product.builder()
                                  .id(1L)
                                  .name("product1")
                                  .price(30.50F)
                                  .description("this is product 1")
                                  .reviews(List.of(Review.builder()
                                                         .id(1L)
                                                         .sender("user1")
                                                         .message("is good")
                                                         .numberOfStars(5)
                                                         .build()))
                                  .build();
        Product product2 = Product.builder()
                                  .id(2L)
                                  .name("product2")
                                  .price(50F)
                                  .description("this is product2")
                                  .reviews(List.of(Review.builder()
                                                         .sender("user1")
                                                         .message("is Ok")
                                                         .numberOfStars(3)
                                                         .build()))
                                  .build();
        productList = List.of(product1, product2);
    }

    @Test
    void getAllProducts_repositoryReturnsListOfProducts_returnProductsDetails() {
        //Arrange
        Mockito.when(repository.findAll())
               .thenReturn(productList);

        //Act
        List<GetProductDetails> actualList = productService.getAllProducts();

        //Assert
        Assertions.assertThat(actualList)
                  .hasSize(2)
                  .isNotNull();
        Assertions.assertThat(actualList.get(0))
                  .isEqualTo(mapProductToGetProductDetails(productList.get(0)));
        Assertions.assertThat(actualList.get(1))
                  .isEqualTo(mapProductToGetProductDetails(productList.get(1)));
    }

    @Test
    void getProduct_repositoryReturnsProduct_returnProductDetails() {
        //Arrange
        Long productId = 1L;
        Mockito.when(repository.findById(productId))
               .thenReturn(Optional.of(productList.get(0)));

        //Act
        GetProductDetails actualProduct = productService.getProduct(productId);

        //Assert
        Assertions.assertThat(actualProduct)
                  .isEqualTo(mapProductToGetProductDetails(productList.get(0)));
    }

    @Test
    void getProduct_repositoryReturnsEmptyOptional_throwsResourceDoesNotExistException() {
        //Arrange
        Long productId = 99L;
        Mockito.when(repository.findById(productId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> {
                      //Act
                      productService.getProduct(productId);
                  })
                  .withMessage("Product with id " + productId + " not found!");
    }

    @Test
    void updateProduct_repositoryReturnsEmptyOptional_throwsResourceDoesNotExistException() {
        //Arrange
        CreateUpdateProduct product = new CreateUpdateProduct("new prod", "new description", 30.0F);
        Long productId = 99L;
        Mockito.when(repository.findById(productId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> {
                      //Act
                      productService.updateProduct(productId, product);
                  })
                  .withMessage("Product with id " + productId + " not found!");
    }

    private GetProductDetails mapProductToGetProductDetails(Product product) {
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
