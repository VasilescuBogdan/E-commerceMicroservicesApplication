package com.bogdan.order.integration;

import com.bogdan.order.controller.model.GetBill;
import com.bogdan.order.controller.model.GetItem;
import com.bogdan.order.integration.gateways.gatewaysshop.OrderGateway;
import com.bogdan.order.integration.gateways.gatewaysuser.AuthenticationGateway;
import com.bogdan.order.integration.gateways.model.ValidationResponse;
import com.bogdan.order.persistence.entities.Bill;
import com.bogdan.order.persistence.entities.Item;
import com.bogdan.order.persistence.repositories.BillRepository;
import com.bogdan.order.persistence.repositories.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Testcontainers
class BillControllerIntTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.0.1");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockBean
    private AuthenticationGateway authenticationGateway;

    @MockBean
    private OrderGateway orderGateway;

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:" + port + "/api/bills";

    private final List<Bill> bills = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Item item1 = new Item(null, "product 1", 15F);
        Item item2 = new Item(null, "product 2", 17.9F);
        Item item3 = new Item(null, "product 1", 15F);
        itemRepository.saveAll(List.of(item1, item2, item3));
        Bill bill1 = new Bill(null, "user1", LocalDateTime.of(2000, 11, 3, 12, 8, 7), 1L,
                new ArrayList<>(List.of(item1, item2)));
        Bill bill2 = new Bill(null, "user2", LocalDateTime.of(2000, 11, 3, 12, 8, 7), 2L,
                new ArrayList<>(List.of(item3)));
        bills.addAll(List.of(bill1, bill2));
        billRepository.saveAll(bills);
    }

    @AfterEach
    void tearDown() {
        billRepository.deleteAll();
        itemRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE bill AUTO_INCREMENT = 1")
                     .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE item AUTO_INCREMENT = 1")
                     .executeUpdate();
    }

    @Test
    void getBillsUser_billListIsReturnedByRepository_responseStatusOkAndListIsReturned() throws Exception {
        //Arrange
        String user = "user1";
        List<GetBill> billList = Stream.of(bills.get(0))
                                       .map(this::mapBillToGetBill)
                                       .toList();

        //Act
        ResultActions response = mockMvc.perform(
                get(baseUrl + "/user").header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(billList)));
    }

    @Test
    void getBillsAdmin_billListIsReturnedByRepository_responseStatusOkAndListIsReturned() throws Exception {
        //Arrange
        List<GetBill> billList = bills.stream()
                                      .map(this::mapBillToGetBill)
                                      .toList();

        //Act
        ResultActions response = mockMvc.perform(get(baseUrl).header(HttpHeaders.AUTHORIZATION, generateTokenAdmin()));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(billList)));
    }

    @Test
    void payBill_repositoryReturnsBill_responseStatusOk() throws Exception {
        //Arrange
        long billId = 1L;
        String user = "user1";

        //Act
        ResultActions response = mockMvc.perform(
                patch(baseUrl + "/{id}", billId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isOk());
    }

    @Test
    void payBill_repositoryReturnsNothing_responseStatusNotFound() throws Exception {
        //Arrange
        long billId = 99L;
        String user = "user1";

        //Act
        ResultActions response = mockMvc.perform(
                patch(baseUrl + "/{id}", billId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    private String generateTokenUser(String username) {
        return generateTestToken(username, "USER");
    }

    private String generateTokenAdmin() {
        return generateTestToken("admin", "ADMIN");
    }

    private String generateTestToken(String username, String role) {
        ValidationResponse validationResponse = new ValidationResponse(role, username);
        String testToken = "token";
        doReturn(Optional.of(validationResponse)).when(authenticationGateway)
                                                 .validateToken(testToken);
        return "Bearer " + testToken;
    }

    private GetBill mapBillToGetBill(Bill bill) {
        return GetBill.builder()
                      .user(bill.getUser())
                      .dateTime(bill.getDateTime())
                      .orderNumber(bill.getOrderNumber())
                      .items(bill.getItems()
                                 .stream()
                                 .map(item -> GetItem.builder()
                                                     .name(item.getName())
                                                     .price(item.getPrice())
                                                     .build())
                                 .toList())
                      .total((float) bill.getItems()
                                         .stream()
                                         .mapToDouble(Item::getPrice)
                                         .sum())
                      .build();
    }
}
