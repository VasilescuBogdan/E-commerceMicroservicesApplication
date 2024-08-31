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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private MockMvc mvc;

    @MockBean
    private AuthenticationGateway authenticationGateway;

    @MockBean
    private ProductService service;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "http://localhost:8082/api/products";

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
        doReturn(List.of(product1, product2)).when(service)
                                             .getAllProducts();

        //Act
        ResultActions response = mvc.perform(get(BASE_URL));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value(product1.name()))
                .andExpect(jsonPath("$[0].description").value(product1.description()))
                .andExpect(jsonPath("$[0].price").value(product1.price()))
                .andExpect(jsonPath("$[0].reviews[0].message").value(product1.reviews()
                                                                             .get(0)
                                                                             .message()))
                .andExpect(jsonPath("$[0].reviews[0].numberOfStars").value(product1.reviews()
                                                                                   .get(0)
                                                                                   .numberOfStars()))
                .andExpect(jsonPath("$[0].reviews[0].sender").value(product1.reviews()
                                                                            .get(0)
                                                                            .sender()))
                .andExpect(jsonPath("$[1].name").value(product2.name()))
                .andExpect(jsonPath("$[1].description").value(product2.description()))
                .andExpect(jsonPath("$[1].price").value(product2.price()));
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
                .andExpect(jsonPath("$.name").value(product.name()))
                .andExpect(jsonPath("$.description").value(product.description()))
                .andExpect(jsonPath("$.price").value(product.price()))
                .andExpect(jsonPath("$.reviews[0].message").value(product.reviews()
                                                                         .get(0)
                                                                         .message()))
                .andExpect(jsonPath("$.reviews[0].numberOfStars").value(product.reviews()
                                                                               .get(0)
                                                                               .numberOfStars()))
                .andExpect(jsonPath("$.reviews[0].sender").value(product.reviews()
                                                                        .get(0)
                                                                        .sender()));
    }

    @Test
    void getProduct_serviceThrowsRecourseDoesNotExistException_returnStatusBadRequest() throws Exception {
        //Arrange
        long productId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .getProduct(productId);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL + "/{id}", productId));

        //Assert
        response.andExpect(status().isBadRequest());
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
    void updateProduct_serviceThrowsResourceDoesNotExistException_returnStatusBadRequest() throws Exception {
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
        response.andExpect(status().isBadRequest());
    }
}
