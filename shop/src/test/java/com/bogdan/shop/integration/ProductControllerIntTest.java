package com.bogdan.shop.integration;

import com.bogdan.shop.controllers.models.CreateUpdateProduct;
import com.bogdan.shop.controllers.models.GetProductDetails;
import com.bogdan.shop.controllers.models.GetReview;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerIntTest extends TestSetup {

    @Autowired
    private ProductRepository productRepository;

    private final String baseUrl = "http://localhost:" + port + "/api/products";

    private final List<Product> products = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Product product1 = new Product(null, "product1", 10F, "this is product1", new ArrayList<>(), new ArrayList<>());
        Product product2 = new Product(null, "product2", 15F, "this is product2", new ArrayList<>(), new ArrayList<>());
        Product product3 = new Product(null, "product3", 18F, "this is product3", new ArrayList<>(), new ArrayList<>());
        Product product4 = new Product(null, "product4", 50F, "this is product4", new ArrayList<>(), new ArrayList<>());
        products.addAll(List.of(product1, product2, product3, product4));
        productRepository.saveAll(products);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE product AUTO_INCREMENT = 1")
                     .executeUpdate();
    }

    @Test
    void createProduct_responseStatusCreatedAndNewProductIsCreated() throws Exception {
        //Arrange
        CreateUpdateProduct product = new CreateUpdateProduct("product5", "this is product5", 50.15F);

        //Act
        ResultActions response = mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON)
                                                          .header(HttpHeaders.AUTHORIZATION,
                                                                  generateTokenAdmin("admin"))
                                                          .content(mapper.writeValueAsString(product)));

        //Assert
        response.andExpect(status().isCreated());
        List<Product> updatedList = productRepository.findAll();
        assertThat(updatedList).hasSize(products.size() + 1);
        Product savedProduct = updatedList.get(updatedList.size() - 1);
        assertThat(savedProduct.getName()).isEqualTo(product.name());
        assertThat(savedProduct.getPrice()).isEqualTo(product.price());
        assertThat(savedProduct.getDescription()).isEqualTo(product.description());
    }

    @Test
    void getAllProducts_responseStatusOkAndReturnProductList() throws Exception {
        //Arrange
        List<GetProductDetails> productDetailsList = products.stream()
                                                             .map(product -> GetProductDetails.builder()
                                                                                              .name(product.getName())
                                                                                              .price(product.getPrice())
                                                                                              .description(
                                                                                                      product.getDescription())
                                                                                              .reviews(
                                                                                                      product.getReviews()
                                                                                                             .stream()
                                                                                                             .map(review -> GetReview.builder()
                                                                                                                                     .sender(review.getSender())
                                                                                                                                     .message(
                                                                                                                                             review.getMessage())
                                                                                                                                     .numberOfStars(
                                                                                                                                             review.getNumberOfStars())
                                                                                                                                     .build())
                                                                                                             .toList())
                                                                                              .build())
                                                             .toList();

        //Act
        ResultActions response = mvc.perform(get(baseUrl).header(HttpHeaders.AUTHORIZATION, generateTokenUser("user")));


        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(productDetailsList)));

    }

    @Test
    void getProduct_productExists_responseStatusOkAndReturnProduct() throws Exception {
        //Arrange
        long productId = 1L;
        Product expectedProduct = products.get(0);
        GetProductDetails productDetails = GetProductDetails.builder()
                                                            .name(expectedProduct.getName())
                                                            .description(expectedProduct.getDescription())
                                                            .price(expectedProduct.getPrice())
                                                            .reviews(expectedProduct.getReviews()
                                                                                    .stream()
                                                                                    .map(review -> GetReview.builder()
                                                                                                            .sender(review.getSender())
                                                                                                            .message(
                                                                                                                    review.getMessage())
                                                                                                            .numberOfStars(
                                                                                                                    review.getNumberOfStars())
                                                                                                            .build())
                                                                                    .toList())
                                                            .build();

        //Act
        ResultActions response = mvc.perform(
                get(baseUrl + "/{id}", productId).header(HttpHeaders.AUTHORIZATION, generateTokenAdmin("admin")));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(productDetails)));
    }

    @Test
    void getProduct_productDoesNotExist_responseStatusNotFound() throws Exception {
        //Arrange
        long productId = 99L;

        //Act
        ResultActions response = mvc.perform(
                get(baseUrl + "/{id}", productId).header(HttpHeaders.AUTHORIZATION, generateTokenAdmin("admin")));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_productExists_responseStatusNoContentAndProductDoesNoLongerExists() throws Exception {
        //Arrange
        long productId = 1L;

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", productId).header(HttpHeaders.AUTHORIZATION, generateTokenAdmin("admin")));

        //Assert
        response.andExpect(status().isNoContent());
        Optional<Product> isProduct = productRepository.findById(productId);
        assertThat(isProduct).isEmpty();
    }

    @Test
    void updateProduct_productExists_responseStatusNoContentAndProductIsModified() throws Exception {
        //Arrange
        long productId = 1L;
        CreateUpdateProduct updateProduct = CreateUpdateProduct.builder()
                                                               .name("new name")
                                                               .description("new description")
                                                               .price(30F)
                                                               .build();

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", productId).header(HttpHeaders.AUTHORIZATION,
                                                                                      generateTokenAdmin("admin"))
                                                                              .contentType(MediaType.APPLICATION_JSON)
                                                                              .content(mapper.writeValueAsString(
                                                                                      updateProduct)));

        //Assert
        response.andExpect(status().isNoContent());
        Product updatedProduct = productRepository.getReferenceById(productId);
        assertThat(updatedProduct.getName()).isEqualTo(updateProduct.name());
        assertThat(updatedProduct.getDescription()).isEqualTo(updateProduct.description());
        assertThat(updatedProduct.getPrice()).isEqualTo(updateProduct.price());
    }

    @Test
    void updateProduct_productDoesNotExist_responseStatusNotFound() throws Exception {
        //Arrange
        long productId = 99L;
        CreateUpdateProduct updateProduct = CreateUpdateProduct.builder()
                                                               .name("new name")
                                                               .description("new description")
                                                               .price(30F)
                                                               .build();

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", productId).header(HttpHeaders.AUTHORIZATION,
                                                                                      generateTokenAdmin("admin"))
                                                                              .contentType(MediaType.APPLICATION_JSON)
                                                                              .content(mapper.writeValueAsString(
                                                                                      updateProduct)));

        //Assert
        response.andExpect(status().isNotFound());
    }
}
