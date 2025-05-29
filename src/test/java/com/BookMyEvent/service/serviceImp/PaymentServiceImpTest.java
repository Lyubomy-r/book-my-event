package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
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
import com.BookMyEvent.entity.PromoCode;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.BookMyEvent.mapper.PaymentDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//@SpringBootTest(properties = {
//    "spring.config.location=classpath:integrationtest.properties"
//})
@SpringBootTest
@Import(TestConfig.class)
@Testcontainers
@Slf4j
class PaymentServiceImpTest {

  @Container
  private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

  @DynamicPropertySource
  static void mongoDbProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }
//  @Autowired
//  private PromoCodeRepository promoCodeRepository;
//  @Autowired
//  private OrderDetailsRepository orderDetailsRepository;
//  @Autowired
//  private PaymentDetailsMapper paymentDetailsMapper;
//  @Autowired
//  private EventRepository eventRepository;
//  @Autowired
//  private UserRepository userRepository;
//  @Autowired
//  private PaymentServiceImp paymentService;
//
//  private final ExecutorService executorService = Executors.newFixedThreadPool(3);
//
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
//  void prepareForPayment() {
//    LocalTime localTime = LocalTime.now();
//    PaymentRequestDTO paymentRequest = new PaymentRequestDTO(
//        "67a7b34c48d0462fabc62d22",
//        new ProductDTO("", "700", "1", "700"),
//        "Joh",
//        "Feris",
//        "+380345728991",
//        "clientest@code.com"
//    );
//    String eventId = "67a7b34c48d0462fabc62d70";
//
//    PaymentResponseDTO paymentResponse = paymentService.prepareForPayment(eventId, paymentRequest);
//    OrderDetails existOrder = orderDetailsRepository.findByOrderReference(paymentResponse.orderReference()).get();
//    log.info("PaymentServiceImpTest : paymentResponse  {}", paymentResponse);
//    log.info("PaymentServiceImpTest : existOrder  {}", existOrder);
//    assertAll(
//        () -> assertNotNull(paymentResponse),
//        () -> assertEquals(paymentRequest.product().productPrice(), paymentResponse.product().productPrice())
//    );
//  }
//
//  @Test
//  void paymentVerification() throws InterruptedException {
//
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
//    CountDownLatch latch = new CountDownLatch(3);
//
//    for (int i = 0; i < 3; i++) {
//      int numberTread = i;
//      executorService.submit(() -> {
//        try {
//          paymentService.paymentVerification(statusResponse);
//          log.info("PaymentServiceImpTest : paymentVerification Thread  {} done", numberTread);// викликаємо метод у кожному потоці
//        } finally {
//          latch.countDown();
//        }
//      });
//    }
//    latch.await();
//    executorService.shutdown();
//
//    OrderDetails existOrder = orderDetailsRepository.findByOrderReference(statusResponse.orderReference()).get();
//    log.info("PaymentServiceImpTest : paymentVerification existOrder  {}", existOrder);
//    assertAll(
//        () -> assertNotNull(existOrder),
//        () -> assertEquals(OrderStatus.PAID, existOrder.getStatus()),
//        () -> assertEquals(3, existOrder.getEvent().getSoldTickets()),
//        () -> assertEquals(97, existOrder.getEvent().getAvailableTickets()),
//        () -> assertEquals(BigDecimal.valueOf(2100).setScale(2, RoundingMode.HALF_UP), existOrder.getEvent().getProfit())
//    );
//
//  }
//
//  @Test
//  void generateSignature() {
//  }
//
//  @Test
//  void random() {
//  }
//
//  @Test
//  void calculateAmount() {
//  }
//
//  @Test
//  void testCalculateAmount() {
//  }
//
//  @Test
//  void getPromoCode() {
//    PromoCode promoCode = PromoCode.builder()
//        .name("WelcomeBME")
//        .percentage(5)
//        .build();
//    promoCodeRepository.save(promoCode);
//
//    PromoCode existPromoCode = paymentService.getPromoCode(promoCode.getName());
//
//    assertEquals(promoCode.getName(), existPromoCode.getName());
//    assertEquals(promoCode.getPercentage(), existPromoCode.getPercentage());
//  }
}