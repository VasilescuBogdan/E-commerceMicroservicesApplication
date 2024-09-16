package com.bogdan.shop.controllers;

import com.bogdan.shop.controllers.api.ReviewController;
import com.bogdan.shop.controllers.models.CreateReview;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.GetReviewDetails;
import com.bogdan.shop.controllers.models.UpdateReview;
import com.bogdan.shop.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.shop.services.ReviewService;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
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

import java.security.Principal;
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

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @MockBean
    private ReviewService service;

    @MockBean
    private AuthenticationGateway authenticationGateway;

    @MockBean
    private Principal principal;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mvc;

    private final String sender = "user";

    private static final String BASE_URL = "http://localhost:8082/api/reviews";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                             .build();
        doReturn(sender).when(principal)
                        .getName();
    }

    @Test
    void addReview_responseStatusCreated() throws Exception {
        //Arrange
        CreateReview review = new CreateReview(1L, 2, "is ok");

        //Act
        ResultActions response = mvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                                                           .principal(principal)
                                                           .content(objectMapper.writeValueAsString(review)));

        //Assert
        response.andExpect(status().isCreated());
        verify(service, times(1)).createReview(principal.getName(), review);
    }

    @Test
    void getReviewsSender_serviceReturnsReviewList_returnReviewListAndStatusOk() throws Exception {
        //Arrange
        GetProduct getProduct = new GetProduct("product", "this is product", 10F);
        GetReviewDetails reviewDetails1 = new GetReviewDetails("is ok", 3, sender, getProduct);
        GetReviewDetails reviewDetails2 = new GetReviewDetails("is bad", 1, sender, getProduct);
        List<GetReviewDetails> reviewList = List.of(reviewDetails1, reviewDetails2);
        doReturn(reviewList).when(service)
                                .getReviewsSender(sender);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL).principal(principal));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(reviewList)));
    }

    @Test
    void getReviewsSender_serviceThrowsResourceDoesNotExistException_responseStatusNotFound() throws Exception {
        //Arrange
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .getReviewsSender(sender);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL).principal(principal));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_serviceCallsDelete_responseStatusNoContent() throws Exception {
        //Arrange
        Long reviewId = 1L;

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", reviewId).principal(principal));

        //Assert
        verify(service, times(1)).deleteReview(reviewId, sender);
        response.andExpect(status().isNoContent());
    }

    @Test
    void deleteReview_serviceThrowsResourceDoesNotExistException_responseStatusNotFound() throws Exception {
        //Arrange
        Long reviewId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .deleteReview(reviewId, sender);

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", reviewId).principal(principal));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_serviceThrowsResourceNotOwnedException_responseStatusBadRequest() throws Exception {
        //Arrange
        Long reviewId = 1L;
        doThrow(ResourceNotOwnedException.class).when(service)
                                                .deleteReview(reviewId, sender);

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", reviewId).principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void updateReview_serviceCallsUpdate_responseStatusNoContent() throws Exception {
        //Arrange
        Long reviewId = 1L;
        UpdateReview updateReview = new UpdateReview("is good", 3);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", reviewId).principal(principal)
                                                                              .contentType(MediaType.APPLICATION_JSON)
                                                                              .content(objectMapper.writeValueAsString(
                                                                                      updateReview)));

        //Assert
        verify(service, times(1)).updateReview(updateReview, reviewId, sender);
        response.andExpect(status().isNoContent());
    }

    @Test
    void updateReview_serviceThrowsResourceDoesNotExistException_responseStatusNotFound() throws Exception {
        //Arrange
        Long reviewId = 1L;
        UpdateReview updateReview = new UpdateReview("is good", 3);
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .updateReview(updateReview, reviewId, sender);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", reviewId).principal(principal)
                                                                              .contentType(MediaType.APPLICATION_JSON)
                                                                              .content(objectMapper.writeValueAsString(
                                                                                      updateReview)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void updateReview_serviceThrowsResourceNotOwnedException_responseStatusBadRequest() throws Exception {
        //Arrange
        Long reviewId = 1L;
        UpdateReview updateReview = new UpdateReview("is good", 3);
        doThrow(ResourceNotOwnedException.class).when(service)
                                                .updateReview(updateReview, reviewId, sender);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", reviewId).principal(principal)
                                                                              .contentType(MediaType.APPLICATION_JSON)
                                                                              .content(objectMapper.writeValueAsString(
                                                                                      updateReview)));

        //Assert
        response.andExpect(status().isBadRequest());
    }
}
