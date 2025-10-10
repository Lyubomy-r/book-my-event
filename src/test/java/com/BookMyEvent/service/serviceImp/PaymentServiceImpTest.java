package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.*;
import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Enums.OrderStatus;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.dto.*;
import com.BookMyEvent.mapper.PaymentDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static com.BookMyEvent.service.serviceImp.PaymentServiceImp.generateSignature;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Autowired private PromoCodeRepository promoCodeRepository;
  @Autowired private OrderDetailsRepository orderDetailsRepository;
  @Autowired private PaymentDetailsMapper paymentDetailsMapper;
  @Autowired private EventRepository eventRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PaymentServiceImp paymentService;
  @Autowired private FundsRequestRepository fundsRequestRepository;

  private final ExecutorService executorService = Executors.newFixedThreadPool(3);

  @Value("${merchant.secret.key}")
  private String merchantSecretKey;

  @Value("${merchant.login}")
  String merchantAccount;

  //  String merchantDomainName = "https://evently-book.vercel.app/";
  @Value("${front.url}")
  String merchantDomainName;

  String currency = "UAH";

  @Value("${service.url}")
  String serviceUrl;

  @Value("${percentage}")
  private int percentage;

  @Value("${web.user.cabinet.url}")
  private String userCabinetUrl;

  private Event event;
  private User userOne;

  @BeforeEach
  public void setIn() {
    LocalTime localTime = LocalTime.now();
    ObjectId userId = new ObjectId("67a7b34c48d0462fabc62d22");
    event = new Event();
    event.setId(new ObjectId("67a7b34c48d0462fabc62d70"));
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setEventStatus(EventStatus.PENDING);
    event.setEventFormat(EventFormat.OFFLINE);
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);
    event.setSoldTickets(0);
    event.setProfit(BigDecimal.ZERO);
    event.setUnlimitedTickets(false);
    event.setPhoneNumber("+380961232456");
    event.setTicketPrice(700L);
    event.setLocation(new Location("Київ", "вул. Успішна, 1", "", "50.426129", "30.514067"));
    event.setAboutOrganizer("Text About Organizer");
    event.setRating(4.2D);
    event.setImages(List.of());
    event.setDate(
        new DateDetails(
            LocalDate.of(LocalDate.now().plusYears(1).getYear(), 10, 21).toString(),
            localTime.toString(),
            localTime.plusHours(2L).toString()));
    event.setCompleted(false);
    userOne =
        User.builder()
            .id(userId)
            .name("Ronald")
            .email("sewewt@code.com")
            .password("As123ertyuer")
            .location("Kyiv")
            .mailConfirmation(true)
            .status(Status.ACTIVE)
            .role(Role.VISITOR)
            .creationDate(LocalDateTime.now())
            .build();
    event.setOrganizers(
            userOne);
    userOne.setCreatedEvents(List.of(event));
    eventRepository.save(event);
    userRepository.save(userOne);
  }

  @AfterEach
  public void cleanDB() {
    eventRepository.deleteAll();
    userRepository.deleteAll();
    orderDetailsRepository.deleteAll();
    promoCodeRepository.deleteAll();
    fundsRequestRepository.deleteAll();
  }

  @Test
  void prepareForPayment() {
    LocalTime localTime = LocalTime.now();
    PaymentRequestDTO paymentRequest =
        new PaymentRequestDTO(
            "67a7b34c48d0462fabc62d22",
            new ProductDTO("", "700", "1", "735"),
            "Joh",
            "Feris",
            "+380345728991",
            "clientest@code.com");
    String eventId = "67a7b34c48d0462fabc62d70";

    PaymentResponseDTO paymentResponse = paymentService.prepareForPayment(eventId, paymentRequest);
    OrderDetails existOrder =
        orderDetailsRepository.findByOrderReference(paymentResponse.orderReference()).get();
    log.info("PaymentServiceImpTest : paymentResponse  {}", paymentResponse);
    log.info("PaymentServiceImpTest : existOrder  {}", existOrder);
    assertAll(
        () -> assertNotNull(paymentResponse),
        () ->
            assertEquals(
                paymentRequest.product().productPrice(), paymentResponse.product().productPrice()));
  }

  @Test
  void paymentVerification() throws InterruptedException {

    PaymentStatusResponseDTO statusResponse =
        new PaymentStatusResponseDTO(
            "evently_book_vercel_app",
            "ON1375089945192",
            "",
            "700",
            "UAH",
            "324567",
            "sergiomail568@gmail.com",
            "+380345728991",
            "1745143687",
            "1745143687",
            "42****4242",
            "visa",
            "Ukraine",
            "PrivatBank",
            "121213321-3213213-3213213-321-3",
            "Approved",
            "Ok",
            "1100",
            "0.00",
            "card");
    String str =
        statusResponse.getMerchantAccount()
            + ";"
            + statusResponse.getOrderReference()
            + ";"
            + statusResponse.getAmount()
            + ";"
            + statusResponse.getCurrency()
            + ";"
            + statusResponse.getAuthCode()
            + ";"
            + statusResponse.getCardPan()
            + ";"
            + statusResponse.getTransactionStatus()
            + ";"
            + statusResponse.getReasonCode();
    String getMerchantSignature = generateSignature(merchantSecretKey, str);
    statusResponse.setMerchantSignature(getMerchantSignature);
    PaymentDetails paymentDetails =
        PaymentDetails.builder()
            .merchantAccount("evently_book_vercel_app")
            .merchantAuthType("SimpleSignature")
            .merchantDomainName("https://evently-book.vercel.app/")
            .currency("UAH")
            .orderTimeout("600")
            .holdTimeout("600")
            .product(new ProductDTO("event-one", "700", "1", "700"))
            .clientFirstName("Joh")
            .clientLastName("Feris")
            .clientEmail(statusResponse.getEmail())
            .clientPhone("+380345728991")
            .defaultPaymentSystem("card")
            .serviceUrl("http://localhost:8080/api/v1/pay/status/verification")
            .merchantSignature("9bc3d5875985abae06a9d8d20e95ef9b")
            .build();
    OrderDetails orderDetails =
        OrderDetails.builder()
            .id(new ObjectId("68050733834e4213c066b2a6"))
            .orderReference(statusResponse.getOrderReference())
            .orderDate(Instant.ofEpochSecond(Long.parseLong(statusResponse.getCreatedDate())))
            .paymentDetails(paymentDetails)
            .user(userOne)
            .event(event)
            .status(OrderStatus.UNPAID)
            .build();
    orderDetailsRepository.save(orderDetails);

    CountDownLatch latch = new CountDownLatch(3);

    for (int i = 0; i < 3; i++) {
      int numberTread = i;
      executorService.submit(
          () -> {
            try {
              paymentService.paymentVerification(statusResponse);
              log.info("PaymentServiceImpTest : paymentVerification Thread  {} done", numberTread);
            } finally {
              latch.countDown();
            }
          });
    }
    latch.await();
    executorService.shutdown();

    OrderDetails existOrder =
        orderDetailsRepository.findByOrderReference(statusResponse.getOrderReference()).get();
    log.info("PaymentServiceImpTest : paymentVerification existOrder  {}", existOrder);
    assertAll(
        () -> assertNotNull(existOrder),
        () -> assertEquals(OrderStatus.PAID, existOrder.getStatus()),
        () -> assertEquals(1, existOrder.getEvent().getSoldTickets()),
        () -> assertEquals(99, existOrder.getEvent().getAvailableTickets()),
        () ->
            assertEquals(
                BigDecimal.valueOf(700).setScale(2, RoundingMode.HALF_UP),
                existOrder.getEvent().getProfit()));
  }


    @Test
    void testGenerateSignature() {
    }

    @Test
    void random() {
    }

    @Test
    void calculateAmount() {
    }

    @Test
    void testCalculateAmount() {
    }

    @Test
    @DisplayName(
            "Test PaymentServiceImp method getPromoCode. Positive Scenario return promo code.")
    void testGetPromoCodePositiveScenarioReturnPromoCode() {
      PromoCode promoCode = PromoCode.builder()
          .name("WelcomeBME")
          .percentage(3)
          .build();
      promoCodeRepository.save(promoCode);

      PromoCode existPromoCode = paymentService.getPromoCode(promoCode.getName());

      assertEquals(promoCode.getName(), existPromoCode.getName());
      assertEquals(promoCode.getPercentage(), existPromoCode.getPercentage());
    }

  @Test
  @DisplayName(
      "Test PaymentServiceImp method saveFundsRequest. Positive Scenario funds request saved.")
  public void testSaveFundsRequestPositiveScenarioFundsSaved() {
    CreateFundsRequestDTO request =
            CreateFundsRequestDTO.builder()
            .cartNumber("4563772990209409839902")
            .amount(BigDecimal.valueOf(700))
            .build();
    event.setSoldTickets(1);
    event.setProfit(BigDecimal.valueOf(700L));
    event.setCompleted(true);
    eventRepository.save(event);
    Event event1 = eventRepository.findById(event.getId()).get();
        log.info("PaymentServiceImpTest : saveFundsRequestPositiveScenarioFundsSaved  {}", event1);
    String expectedMessage = "The Funds request saved successfully.";

    String message = paymentService.saveFundsRequest(userOne.getId().toHexString(), request);
    List<FundsRequest> existFunds =
        fundsRequestRepository.findByUserId(userOne.getId().toHexString());
    FundsRequest existRequest = existFunds.get(0);
    Instant now = Instant.now();

    assertAll(
        () -> assertEquals(expectedMessage, message),
        () -> assertFalse(existFunds.isEmpty()),
        () -> assertEquals(1, existFunds.size()),
        () -> assertEquals(request.getAmount(), existRequest.getAmount()),
        () -> assertEquals(request.getCartNumber(), existRequest.getCartNumber()),
        () -> assertEquals(userOne.getId().toHexString(), existRequest.getUserId()),
        () -> assertTrue(existRequest.getEventIds().contains(event.getId().toHexString())),
        () -> assertEquals(FundsStatus.PENDING, existRequest.getStatus()),
        () -> assertTrue(existRequest.getCreationDate().isBefore(now)));
  }

  @Test
  @DisplayName(
          "Test PaymentServiceImp method getOrganizerFunds. Positive Scenario get Organizer Funds.")
  public void testGetOrganizerFundsPositiveScenarioGetFunds() {
    BigDecimal profit = BigDecimal.valueOf(700L);
    event.setSoldTickets(1);
    event.setProfit(profit);
    event.setCompleted(true);
    eventRepository.save(event);

    BigDecimal existFunds =
            paymentService.getOrganizerFunds(userOne.getId().toHexString());

    assertEquals(profit, existFunds);
  }

  @Test
  @DisplayName(
          "Test PaymentServiceImp method getOrganizerFunds. Positive Scenario Get Zero Funds.")
  public void testGetOrganizerFundsPositiveScenarioGetZeroFunds() {
    BigDecimal profit = BigDecimal.valueOf(700L);
    CreateFundsRequestDTO request =
            CreateFundsRequestDTO.builder()
                    .cartNumber("4563772990209409839902")
                    .amount(profit)
                    .build();
    event.setSoldTickets(1);
    event.setProfit(profit);
    event.setCompleted(true);
    eventRepository.save(event);

    String message = paymentService.saveFundsRequest(userOne.getId().toHexString(), request);
    BigDecimal existFunds =
            paymentService.getOrganizerFunds(userOne.getId().toHexString());

    assertEquals(BigDecimal.ZERO, existFunds);
  }

  @Test
  @DisplayName(
          "Test PaymentServiceImp method getOrganizerWithdrawnFunds. Positive Scenario get Organizer Withdrawn Funds.")
  public void testGetOrganizerWithdrawnFundsPositiveScenarioFundsSaved() {
    BigDecimal profit = BigDecimal.valueOf(700L);
    CreateFundsRequestDTO request =
            CreateFundsRequestDTO.builder()
                    .cartNumber("4563772990209409839902")
                    .amount(profit)
                    .build();
    event.setSoldTickets(1);
    event.setProfit(profit);
    event.setCompleted(true);
    eventRepository.save(event);

   paymentService.saveFundsRequest(userOne.getId().toHexString(), request);
    List<FundsRequest> fundsRequests =
            fundsRequestRepository.findByUserId(
                    userOne.getId().toHexString());
    FundsRequest existFundsRequest=  fundsRequests.get(0);
    existFundsRequest.setStatus(FundsStatus.COMPLETED);
    fundsRequestRepository.save(existFundsRequest);
    BigDecimal existFunds =
            paymentService.getOrganizerWithdrawnFunds(userOne.getId().toHexString());

    assertEquals(profit, existFunds);
  }
}
