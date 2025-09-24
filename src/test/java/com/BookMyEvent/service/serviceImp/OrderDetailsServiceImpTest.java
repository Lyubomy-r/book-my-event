package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.OrderDetailsRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.*;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import com.BookMyEvent.entity.dto.ProductDTO;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class OrderDetailsServiceImpTest {
  @Mock
  private OrderDetailsRepository orderDetailsRepository;

  @InjectMocks
  private OrderDetailsServiceImp orderDetailsService;

  @Test
  void findAllUserOrders() {
    LocalTime localTime = LocalTime.now();
    Event event = new Event();
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
    event.setLocation(new Location(
        "Київ",
        "вул. Успішна, 1",
        "",
        "50.426129",
        "30.514067"));
    event.setAboutOrganizer("Text About Organizer");
    event.setRating(4.2D);
    event.setImages(List.of());
    event.setOrganizers(User.builder().id(new ObjectId("67a7b34c48d0462fabc62d22"))
        .email("test@email.com")
        .createdEvents(new ArrayList<>())
        .build());
    event.setDate(new DateDetails(
        LocalDate.of(LocalDate.now().plusYears(1).getYear(),
            10, 21
        ).toString(),
        localTime.toString(),
        localTime.plusHours(2L).toString()));
    User userOne = User.builder()
        .id(new ObjectId("67a7b34c48d0462fabc62d22"))
        .name("Ronald")
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .creationDate(LocalDateTime.now())
        .build();

    PaymentDetails paymentDetails = PaymentDetails.builder()
        .merchantAccount("evently_book_vercel_app")
        .merchantAuthType("SimpleSignature")
        .merchantDomainName("https://evently-book.vercel.app/")
        .currency("UAH")
        .orderTimeout("600")
        .holdTimeout("600")
        .product(new ProductDTO("event-one", "700", "1", "700"))
        .clientFirstName("Joh")
        .clientLastName("Feris")
        .clientEmail("clientest@code.com")
        .clientPhone("+380345728991")
        .defaultPaymentSystem("card")
        .serviceUrl("http://localhost:8080/api/v1/pay/status/verification")
        .merchantSignature("9bc3d5875985abae06a9d8d20e95ef9b")
        .build();
    OrderDetails orderDetails = OrderDetails.builder()
        .id(new ObjectId("68050733834e4213c066b2a6"))
        .orderReference("ON1375089945192")
        .orderDate(Instant.ofEpochSecond(Long.parseLong("1745143687")))
        .paymentDetails(paymentDetails)
        .user(userOne)
        .event(event)
        .status(OrderStatus.PAID)
        .build();
    EventResponseDto eventResponseDto = new EventResponseDto();
    eventResponseDto.setId(event.getId().toHexString());
    eventResponseDto.setTitle(event.getTitle());
    eventResponseDto.setDescription(event.getDescription());
    eventResponseDto.setEventType(event.getEventType().getUkrainianName());
    eventResponseDto.setEventCategory(event.getEventCategory().toString());
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    eventResponseDto.setEventFormat(event.getEventFormat().toString());
    eventResponseDto.setAvailableTickets(event.getAvailableTickets());
    eventResponseDto.setNumberOfTickets(event.getNumberOfTickets());
    eventResponseDto.setUnlimitedTickets(event.getUnlimitedTickets());
    eventResponseDto.setPhoneNumber(event.getPhoneNumber());
    eventResponseDto.setTicketPrice(event.getTicketPrice());
    eventResponseDto.setLocation(event.getLocation());
    eventResponseDto.setAboutOrganizer(event.getAboutOrganizer());
    eventResponseDto.setRating(event.getRating());
    eventResponseDto.setImages(event.getImages());
    eventResponseDto.setDate(event.getDate());
    ZoneId kyivZone = ZoneId.of("Europe/Kiev");
    OrderDetailsDto orderDetailsDto =new OrderDetailsDto(
            orderDetails.getId().toHexString(),
            orderDetails.getOrderReference(),
            orderDetails.getOrderDate().atZone(kyivZone).toString(),
            paymentDetails.getProduct().productCount(),
            paymentDetails.getProduct().amount(),
            eventResponseDto,
            null,
            orderDetails.getStatus().getNameUa()
    );

    PageRequest pageRequest = PageRequest.of(0, 1);
    Page<OrderDetails> orderDetailsList = new PageImpl<>(List.of(orderDetails), pageRequest, 1);
    Page<OrderDetailsDto> orderDetailsDtoPage = new PageImpl<>(List.of(orderDetailsDto), pageRequest, 1);
    ObjectId objectId = new ObjectId("66c648b600179737a3d5c235");
    when(orderDetailsRepository.findByUser_Id(objectId, pageRequest)).thenReturn(orderDetailsList);

    Page<OrderDetailsDto> response = orderDetailsService.findAllUserOrders(objectId.toHexString(), pageRequest);
    response.forEach(System.out::println);

    ZonedDateTime kyivTime = orderDetails.getOrderDate().atZone(kyivZone);
    String orderDate = String.valueOf(kyivTime.toEpochSecond());
    assertFalse(response.isEmpty());
    assertEquals(1, response.getContent().size());
    assertEquals(orderDetailsDto.id(), response.getContent().get(0).id());
    assertEquals(orderDetailsDto.orderReference(), response.getContent().get(0).orderReference());
    assertEquals(orderDetailsDto.orderDate(), response.getContent().get(0).orderDate());
    assertEquals(event.getTicketPrice(), response.getContent().get(0).event().getTicketPrice());
    assertEquals(orderDetailsDto.status(), response.getContent().get(0).status());
  }

}
