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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void addProduct() {
        //Arrange
        CreateUpdateProduct createProduct = new CreateUpdateProduct("product", "description", 15.4F);

        //Act
        productService.addProduct(createProduct);

        //Assert
        verify(repository, times(1)).save(
                new Product(null, createProduct.name(), createProduct.price(), createProduct.description(),
                        new ArrayList<>()));
    }

    @Test
    void getAllProducts_repositoryReturnsListOfProducts_returnProductsDetails() {
        //Arrange
        Product product1 = new Product(1L, "product 1", 30.5F, "this is product1",
                List.of(new Review(1L, "user1", "is good", 5, null)));
        Product product2 = new Product(2L, "product2", 50F, "this is product2",
                List.of(new Review(2L, "user1", "is Ok", 3, null)));
        doReturn(List.of(product1, product2)).when(repository)
                                             .findAll();

        //Act
        List<GetProductDetails> actualList = productService.getAllProducts();

        //Assert
        Assertions.assertThat(actualList)
                  .hasSize(2)
                  .isNotNull();
        Assertions.assertThat(actualList.get(0))
                  .isEqualTo(mapProductToGetProductDetails(product1));
        Assertions.assertThat(actualList.get(1))
                  .isEqualTo(mapProductToGetProductDetails(product2));
    }

    @Test
    void getProduct_repositoryReturnsProduct_returnProductDetails() {
        //Arrange
        Long productId = 1L;
        Product product = new Product(1L, "product 1", 30.5F, "this is product1",
                List.of(new Review(1L, "user1", "is good", 5, null)));
        doReturn(Optional.of(product)).when(repository)
                                      .findById(productId);

        //Act
        GetProductDetails actualProduct = productService.getProduct(productId);

        //Assert
        Assertions.assertThat(actualProduct)
                  .isEqualTo(mapProductToGetProductDetails(product));
    }

    @Test
    void getProduct_repositoryReturnsEmptyOptional_throwsResourceDoesNotExistException() {
        //Arrange
        Long productId = 99L;
        doReturn(Optional.empty()).when(repository)
                                  .findById(productId);

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> {
                      //Act
                      productService.getProduct(productId);
                  })
                  .withMessage("Product with id " + productId + " not found!");
    }

    @Test
    void deleteProduct() {
        //Arrange
        long productId = 1L;

        //Act
        productService.deleteProduct(productId);

        //Assert
        verify(repository, times(1)).deleteById(productId);
    }

    @Test
    void updateProduct_repositoryReturnsProduct_callSaveMethod() {
        //Arrange
        CreateUpdateProduct updatedProduct = new CreateUpdateProduct("new prod", "new description", 30.0F);
        Long productId = 99L;
        Product product = new Product(productId, "product", 30.5F, "this is product",
                List.of(new Review(1L, "user1", "is good", 5, null)));

        doReturn(Optional.of(product)).when(repository)
                                      .findById(productId);

        //Act
        productService.updateProduct(productId, updatedProduct);

        //Assert
        verify(repository, times(1)).save(
                new Product(productId, updatedProduct.name(), updatedProduct.price(), updatedProduct.description(),
                        product.getReviews()));
    }

    @Test
    void updateProduct_repositoryReturnsNothing_throwsResourceDoesNotExistException() {
        //Arrange
        CreateUpdateProduct product = new CreateUpdateProduct("new prod", "new description", 30.0F);
        Long productId = 99L;
        doReturn(Optional.empty()).when(repository)
                                  .findById(productId);

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
