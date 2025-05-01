package com.BookMyEvent.controller;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.OrderDetailsRepository;
import com.BookMyEvent.dao.PromoCodeRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Enums.OrderStatus;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.OrderDetails;
import com.BookMyEvent.entity.PaymentDetails;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.config.location=classpath:integrationtest.properties"
})
@AutoConfigureMockMvc
@Slf4j
class PayControllerTest {
//
//  @Autowired
//  private MockMvc mockMvc;
//  @Autowired
//  private OrderDetailsRepository orderDetailsRepository;
//  @Autowired
//  private EventRepository eventRepository;
//  @Autowired
//  private UserRepository userRepository;
//  @MockBean
//  private PromoCodeRepository promoCodeRepository;
//
//  private final ExecutorService executorService = Executors.newFixedThreadPool(3);
//  private final String merchantSecretKey = "07e12edf1d5f39eaf8b1b7fd029cd10f2b557c3e";
//  private final String merchantLogin = "evently_book_vercel_app";
//  private final String baseUrl = "https://secure.wayforpay.com/pay";
//  private String merchantAccount = merchantLogin;
//  private String merchantDomainName = "https://evently-book.vercel.app/";
//  private String currency = "UAH";
//  private String serviceUrl = "http://localhost:8080/api/v1/pay/status/verification";
//  private int percentage = 5;
//  private Event event;
//  private User userOne;
//
//  @BeforeEach
//  public void setIn() {
//    LocalTime localTime = LocalTime.now();
//    event = new Event();
//    event.setId(new ObjectId("67a7b34c48d0462fabc62d70"));
//    event.setTitle("Test Event");
//    event.setDescription("Test Description");
//    event.setEventType(EventType.SPORTS_EVENTS);
//    event.setEventCategory(EventCategory.TOP_EVENTS);
//    event.setEventStatus(EventStatus.PENDING);
//    event.setEventFormat(EventFormat.OFFLINE);
//    event.setAvailableTickets(100);
//    event.setNumberOfTickets(100);
//    event.setSoldTickets(0);
//    event.setProfit(BigDecimal.ZERO);
//    event.setUnlimitedTickets(false);
//    event.setPhoneNumber("+380961232456");
//    event.setTicketPrice(700L);
//    event.setLocation(new Location(
//        "Київ",
//        "вул. Успішна, 1",
//        "",
//        "50.426129",
//        "30.514067"));
//    event.setAboutOrganizer("Text About Organizer");
//    event.setRating(4.2D);
//    event.setImages(List.of());
//    event.setOrganizers(User.builder().id(new ObjectId("67a7b34c48d0462fabc62d22"))
//        .email("test@email.com")
//        .createdEvents(new ArrayList<>())
//        .build());
//    event.setDate(new DateDetails(
//        LocalDate.of(LocalDate.now().plusYears(1).getYear(),
//            10, 21
//        ).toString(),
//        localTime.toString(),
//        localTime.plusHours(2L).toString()));
//    userOne = User.builder()
//        .id(new ObjectId("67a7b34c48d0462fabc62d22"))
//        .name("Ronald")
//        .email("sewewt@code.com")
//        .password("As123ertyuer")
//        .location("Kyiv")
//        .mailConfirmation(true)
//        .status(Status.ACTIVE)
//        .role(Role.VISITOR)
//        .creationDate(LocalDateTime.now())
//        .build();
//    eventRepository.save(event);
//    userRepository.save(userOne);
//  }
//
//  @AfterEach
//  public void cleanDB() {
//    eventRepository.deleteAll();
//    userRepository.deleteAll();
//    orderDetailsRepository.deleteAll();
//    promoCodeRepository.deleteAll();
//  }
//
//  @Test
//  void prepareForPayment() throws Exception {
//    PaymentRequestDTO paymentRequest = new PaymentRequestDTO(
//        "67a7b34c48d0462fabc62d22",
//        new ProductDTO("", "700", "1", "700"),
//        "Joh",
//        "Feris",
//        "+380345728991",
//        "clientest@code.com"
//    );
//
//    MvcResult result = mockMvc.perform(post("/api/pay/{eventId}", event.getId())
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(new ObjectMapper().writeValueAsString(paymentRequest)))
//        .andExpect(status().isOk())
//        .andReturn();
//
//    String responseJson = result.getResponse().getContentAsString();
//    JsonNode jsonNode = new ObjectMapper().readTree(responseJson);
//    String responseMerchantDomainName = jsonNode.get("merchantDomainName").asText();
//    String clientEmail = jsonNode.get("clientEmail").asText();
//    String responseOrderReference = jsonNode.get("orderReference").asText();
//    assertEquals(paymentRequest.clientEmail(),clientEmail);
//    assertEquals(merchantDomainName, responseMerchantDomainName);
//
//    mockMvc.perform(get("/api/v1/pay/order/details/{orderReference}", responseOrderReference))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.orderReference").value(responseOrderReference))
//        .andExpect(jsonPath("$.status").value(OrderStatus.UNPAID));
//  }
//
//  @Test
//  void paymentVerification() throws Exception {
//    PaymentStatusResponseDTO statusResponse = new PaymentStatusResponseDTO(
//        "evently_book_vercel_app",
//        "ON1375089945192",
//        "9bc3d5875985abae06a9d8d20e95ef9b",
//        "700",
//        "UAH",
//        "324567",
//        "clientest@code.com",
//        "+380345728991",
//        "1745143687",
//        "1745143687",
//        "42****4242",
//        "Ukraine",
//        "PrivatBank",
//        "121213321-3213213-3213213-321-3",
//        "Approved",
//        "Ok",
//        "1100",
//        "0.00",
//        "card"
//    );
//    PaymentDetails paymentDetails = PaymentDetails.builder()
//        .merchantAccount("evently_book_vercel_app")
//        .merchantAuthType("SimpleSignature")
//        .merchantDomainName("https://evently-book.vercel.app/")
//        .currency("UAH")
//        .orderTimeout("600")
//        .holdTimeout("600")
//        .product(new ProductDTO("event-one", "700", "1", "700"))
//        .clientFirstName("Joh")
//        .clientLastName("Feris")
//        .clientEmail("clientest@code.com")
//        .clientPhone("+380345728991")
//        .defaultPaymentSystem("card")
//        .serviceUrl("http://localhost:8080/api/v1/pay/status/verification")
//        .merchantSignature("9bc3d5875985abae06a9d8d20e95ef9b")
//        .build();
//    OrderDetails orderDetails = OrderDetails.builder()
//        .id(new ObjectId("68050733834e4213c066b2a6"))
//        .orderReference(statusResponse.orderReference())
//        .orderDate(Instant.ofEpochSecond(Long.parseLong(statusResponse.createdDate())))
//        .paymentDetails(paymentDetails)
//        .user(userOne)
//        .event(event)
//        .status(OrderStatus.UNPAID)
//        .build();
//    orderDetailsRepository.save(orderDetails);
//
//    mockMvc.perform(post("/api/payments/status/verification")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(new ObjectMapper().writeValueAsString(statusResponse)))
//        .andExpect(status().isOk());
//
//    mockMvc.perform(get("/api/payments/order/details/{orderReference}", statusResponse.orderReference()))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.orderReference").value(statusResponse.orderReference()))
//        .andExpect(jsonPath("$.status").value("APPROVED"));
//  }

  @Test
  void getOrderDetails() {
  }

  @Test
  void getPromoCode() {
  }
}