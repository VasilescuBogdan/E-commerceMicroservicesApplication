package com.bogdan.shop.controllers;

import com.bogdan.shop.controllers.api.ProductController;
import com.bogdan.shop.controllers.models.CreateUpdateProduct;
import com.bogdan.shop.controllers.models.GetProductDetails;
import com.bogdan.shop.controllers.models.GetReview;
import com.bogdan.shop.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.shop.services.ProductService;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private AuthenticationGateway authenticationGateway;

    @MockBean
    private ProductService service;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "http://localhost:8082/api/products";

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build();
    }

    @Test
    void createProduct_productIsAddedToDatabase_responseStatusCreated() throws Exception {
        //Arrange
        CreateUpdateProduct product = new CreateUpdateProduct("product1", "this is product 1", 10.5F);

        //Act
        ResultActions response = mvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                                                           .content(objectMapper.writeValueAsString(product)));

        //Assert
        response.andExpect(status().isCreated());
        verify(service, times(1)).addProduct(product);
    }

    @Test
    void getAllProducts_serviceReturnsProductDetails_returnProductDetailsAndStatusOk() throws Exception {
        //Arrange
        GetProductDetails product1 = new GetProductDetails("product1", "this is product1", 13F,
                List.of(new GetReview("is bad", 1, "user1")));
        GetProductDetails product2 = new GetProductDetails("product2", "this is product2", 15.5F, null);
        List<GetProductDetails> productList = List.of(product1, product2);
        doReturn(productList).when(service)
                          .getAllProducts();

        //Act
        ResultActions response = mvc.perform(get(BASE_URL));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(productList)));
    }

    @Test
    void getProduct_serviceReturnsProductDetails_returnProductDetailsAndStatusOk() throws Exception {
        //Arrange
        long productId = 1L;
        GetProductDetails product = new GetProductDetails("product1", "this is product1", 13F,
                List.of(new GetReview("is bad", 1, "user1")));
        doReturn(product).when(service)
                         .getProduct(productId);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL + "/{id}", productId));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(product)));
    }

    @Test
    void getProduct_serviceThrowsRecourseDoesNotExistException_returnStatusNotFound() throws Exception {
        //Arrange
        long productId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .getProduct(productId);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL + "/{id}", productId));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returnStatusNoContent() throws Exception {
        //Arrange
        long productId = 1L;

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", productId));

        //Assert
        verify(service, times(1)).deleteProduct(productId);
        response.andExpect(status().isNoContent());
    }

    @Test
    void updateProduct_serviceReturnsActualProduct_returnStatusNoContent() throws Exception {
        //Arrange
        long productId = 1L;
        CreateUpdateProduct product = new CreateUpdateProduct("updated product", "updated description", 10F);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", productId).contentType(MediaType.APPLICATION_JSON)
                                                                               .content(objectMapper.writeValueAsString(
                                                                                       product)));

        //Assert
        verify(service, times(1)).updateProduct(productId, product);
        response.andExpect(status().isNoContent());
    }


    @Test
    void updateProduct_serviceThrowsResourceDoesNotExistException_returnStatusNotFound() throws Exception {
        //Arrange
        long productId = 1L;
        CreateUpdateProduct product = new CreateUpdateProduct("updated product", "updated description", 10F);
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .updateProduct(productId, product);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", productId).contentType(MediaType.APPLICATION_JSON)
                                                                               .content(objectMapper.writeValueAsString(
                                                                                       product)));

        //Assert
        response.andExpect(status().isNotFound());
    }
}
