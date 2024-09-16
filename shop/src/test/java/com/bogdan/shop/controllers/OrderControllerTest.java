package com.bogdan.shop.controllers;

import com.bogdan.shop.controllers.api.OrderController;
import com.bogdan.shop.controllers.models.CreateOrder;
import com.bogdan.shop.controllers.models.GetOrder;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.UpdateOrder;
import com.bogdan.shop.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.shop.persistence.entities.OrderStatus;
import com.bogdan.shop.services.OrderService;
import com.bogdan.shop.util.exceptions.OperationNotSupportedException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockBean
    private OrderService service;

    @MockBean
    private Principal principal;

    @MockBean
    private AuthenticationGateway authenticationGateway;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String BASE_URL = "http://localhost:8082/api/orders";

    private final String user = "user";

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                             .build();
        doReturn(user).when(principal)
                      .getName();
    }

    @Test
    void createOrder_serviceIsCalledSuccessfully_responseStatusCreated() throws Exception {
        //Arrange
        CreateOrder order = new CreateOrder("address", List.of(1L));

        //Act
        ResultActions response = mvc.perform(post(BASE_URL).principal(principal)
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(mapper.writeValueAsString(order)));

        //Assert
        verify(service, times(1)).createOrder(user, order);
        response.andExpect(status().isCreated());
    }

    @Test
    void createOrder_serviceThrowsResourceDoesNotExistException_responseStatusNotFound() throws Exception {
        //Arrange
        CreateOrder order = new CreateOrder("address", List.of(1L));
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .createOrder(user, order);

        //Act
        ResultActions response = mvc.perform(post(BASE_URL).principal(principal)
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(mapper.writeValueAsString(order)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void getOrdersUser_serviceReturnsOrders_responseStatusOkAndReturnOrders() throws Exception {
        //Arrange
        GetProduct product = new GetProduct("product", "this is product", 10F);
        GetOrder getOrder1 = new GetOrder(1L, user, "address1", OrderStatus.CREATED, List.of(product));
        GetOrder getOrder2 = new GetOrder(2L, user, "address2", OrderStatus.FINISHED, List.of(product));
        List<GetOrder> orderList = List.of(getOrder1, getOrder2);
        doReturn(orderList).when(service)
                           .getOrdersUser(user);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL + "/user").principal(principal));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(orderList)));
    }

    @Test
    void getOrdersAdmin_serviceReturnsOrders_responseStatusOkAndReturnOrders() throws Exception {
        //Arrange
        GetProduct product = new GetProduct("product", "this is product", 10F);
        GetOrder getOrder = new GetOrder(1L, "user1", "address", OrderStatus.CREATED, List.of(product));
        doReturn(List.of(getOrder)).when(service)
                          .getOrders();

        //Act
        ResultActions response = mvc.perform(get(BASE_URL));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(getOrder))));
    }

    @Test
    void deleteOrder_serviceCallsSuccessfully_responseNoContent() throws Exception {
        //Arrange
        long orderId = 1L;

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isNoContent());
        verify(service, times(1)).deleteOrder(orderId, user);
    }

    @Test
    void deleteOrder_serviceThrowsResourceDoesNotExistException_responseNotFound() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .deleteOrder(orderId, user);

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_serviceThrowsResourceNotOwnedException_responseBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(ResourceNotOwnedException.class).when(service)
                                                .deleteOrder(orderId, user);

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void deleteOrder_serviceThrowsOperationNotSuppoertedException_responseBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(OperationNotSupportedException.class).when(service)
                                                     .deleteOrder(orderId, user);

        //Act
        ResultActions response = mvc.perform(delete(BASE_URL + "/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void updateOrder_serviceCallsSuccessfully_responseNoContent() throws Exception {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(1L, 2L));

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", orderId).contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateOrder))
                                                                             .principal(principal));

        //Assert
        response.andExpect(status().isNoContent());
        verify(service, times(1)).updateOrder(orderId, updateOrder, user);
    }

    @Test
    void updateOrder_serviceThrowsResourceDoesNotExistException_responseNotFound() throws Exception {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(1L, 2L));
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .updateOrder(orderId, updateOrder, user);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", orderId).contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateOrder))
                                                                             .principal(principal));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void updateOrder_serviceThrowsResourceNotOwnedException_responseBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(1L, 2L));
        doThrow(ResourceNotOwnedException.class).when(service)
                                                .updateOrder(orderId, updateOrder, user);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", orderId).contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateOrder))
                                                                             .principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void updateOrder_serviceThrowsOperationNotSupportedException_responseBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        UpdateOrder updateOrder = new UpdateOrder("new address", List.of(1L, 2L));
        doThrow(OperationNotSupportedException.class).when(service)
                                                     .updateOrder(orderId, updateOrder, user);

        //Act
        ResultActions response = mvc.perform(put(BASE_URL + "/{id}", orderId).contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateOrder))
                                                                             .principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_serviceCallsSuccessfully_responseOk() throws Exception {
        //Arrange
        long orderId = 1L;

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/place/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isOk());
        verify(service, times(1)).placeOrder(orderId, user);
    }

    @Test
    void placeOrder_serviceThrowsResourceDoesNotExistException_responseNotFound() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .placeOrder(orderId, user);

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/place/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_servResourceNotOwnedExceptionException_responseBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(ResourceNotOwnedException.class).when(service)
                                                .placeOrder(orderId, user);

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/place/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_serviceThrowsOperationNotSupportedException_responseBadRequest() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(OperationNotSupportedException.class).when(service)
                                                     .placeOrder(orderId, user);

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/place/{id}", orderId).principal(principal));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void finishOrder_serviceCallsSuccessfully_responseOk() throws Exception {
        //Arrange
        long orderId = 1L;

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/finish/{id}", orderId));

        //Assert
        response.andExpect(status().isOk());
        verify(service, times(1)).finishOrder(orderId);
    }

    @Test
    void finishOrder_serviceThrowsResourceDoesNotExistException_responseNotFound() throws Exception {
        //Arrange
        long orderId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .finishOrder(orderId);

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/finish/{id}", orderId));

        //Assert
        response.andExpect(status().isNotFound());
    }
}
