package com.bogdan.order.controller;

import com.bogdan.order.controller.api.BillController;
import com.bogdan.order.controller.model.GetBill;
import com.bogdan.order.controller.model.GetItem;
import com.bogdan.order.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.order.service.BillService;
import com.bogdan.order.utils.exception.ResourceDoesNotExistException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillController.class)
class BillControllerTest {

    @MockBean
    private BillService service;

    @MockBean
    private AuthenticationGateway authenticationGateway;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    private static final String BASE_URL = "http://localhost:8083/api/bills";

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                             .build();
    }

    @Test
    void getBillsUser_serviceReturnsBillList_returnsBillsAndStatusOk() throws Exception {
        //Arrange
        String user = "user";
        Principal principal = mock(Principal.class);
        doReturn(user).when(principal)
                      .getName();
        GetBill bill = new GetBill(user, LocalDateTime.of(2000, 10, 11, 12, 55, 30), 2L,
                List.of(new GetItem("product", 10F)), 10F);
        List<GetBill> billList = List.of(bill);
        doReturn(billList).when(service)
                       .getBillsUser(user);

        //Act
        ResultActions response = mvc.perform(get(BASE_URL + "/user").principal(principal));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(billList)));
    }

    @Test
    void getBillsAdmin_serviceReturnsBillList_returnsBillsAndStatusOk() throws Exception {
        //Arrange
        GetBill bill = new GetBill("user", LocalDateTime.of(2000, 10, 11, 12, 55, 30), 2L,
                List.of(new GetItem("product", 10F)), 10F);
        List<GetBill> billList = List.of(bill);
        doReturn(billList).when(service)
                          .getBills();

        //Act
        ResultActions response = mvc.perform(get(BASE_URL));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(billList)));
    }

    @Test
    void payBill_serviceCallsGateway_responseStatusOk() throws Exception {
        //Arrange
        long billId = 1L;

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/{id}", billId));

        //Assert
        response.andExpect(status().isOk());
        verify(service, times(1)).payBill(billId);
    }

    @Test
    void payBill_serviceThrowsResourceDoesNotExistException_responseStatusNotFound() throws Exception {
        //Arrange
        long billId = 1L;
        doThrow(ResourceDoesNotExistException.class).when(service)
                                                    .payBill(billId);

        //Act
        ResultActions response = mvc.perform(patch(BASE_URL + "/{id}", billId));

        //Assert
        response.andExpect(status().isNotFound());
    }
}
