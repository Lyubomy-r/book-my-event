package com.BookMyEvent.controller;

import com.BookMyEvent.entity.*;
import com.BookMyEvent.entity.Enums.*;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.OrderDetailsDto;
import com.BookMyEvent.entity.dto.ProductDTO;
import com.BookMyEvent.service.OrderDetailsService;
import com.BookMyEvent.service.PaymentService;
import com.BookMyEvent.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.awt.print.Pageable;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:integrationtest.properties")
@Slf4j
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderDetailsService orderDetailsService;
    @MockBean
    private  UserService userService;
    @MockBean
    private PaymentService paymentService;

    private User userOne;
    private Event event;
    private EventResponseDto eventResponseDto;
    @BeforeEach
    public void setUp(){
        LocalTime localTime = LocalTime.now();
        userOne = User.builder()
                .id(new ObjectId("66c648b600179737a3d5c235"))
                .name("Ronald")
                .email("sewewt@code.com")
                .password("As123ertyuer")
                .location("Kyiv")
                .mailConfirmation(true)
                .status(Status.ACTIVE)
                .role(Role.VISITOR)
                .creationDate(LocalDateTime.now())
                .build();

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
        eventResponseDto = new EventResponseDto();
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
    }

    @Test
    void findUserInfoById() {
      }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void findAllUserOrders() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .orderTimeout("600")
                .holdTimeout("600")
                .product(new ProductDTO("event-one", "700", "1", "700"))
                .clientFirstName("Joh")
                .clientLastName("Feris")
                .clientEmail("clientest@code.com")
                .clientPhone("+380345728991")
                .build();
//        OrderDetails orderDetails = OrderDetails.builder()
//                .id(new ObjectId("68050733834e4213c066b2a6"))
//                .orderReference("ON1375089945192")
//                .orderDate(Instant.ofEpochSecond(Long.parseLong("1745143687")))
//                .paymentDetails(paymentDetails)
//                .user(userOne)
//                .event(event)
//                .status(OrderStatus.PAID)
//                .build();
//        List<OrderDetails> orderDetailsList = List.of(orderDetails);
        ZoneId kyivZone = ZoneId.of("Europe/Kiev");
        ZonedDateTime kyivTime = Instant.ofEpochSecond(Long.parseLong("1745143687")).atZone(kyivZone);
        OrderDetailsDto orderDetails =new OrderDetailsDto(
                "68050733834e4213c066b2a6",
               "ON1375089945192",
                kyivTime.toString(),
                paymentDetails.getProduct().productCount(),
                paymentDetails.getProduct().amount(),
                eventResponseDto,
               null,
               OrderStatus.PAID.getNameUa()
                );
        PageRequest pageRequest = PageRequest.of(0, 6);
        Page<OrderDetailsDto> orderDetailsDtoPage = new PageImpl<>(List.of(orderDetails), pageRequest, 1);

        when(orderDetailsService.findAllUserOrders(userOne.getId().toHexString(), pageRequest)).thenReturn(orderDetailsDtoPage);
//        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
//                Map.of("id", userOne.getId()), null, "ADMIN");
//        SecurityContextHolder.getContext().setAuthentication(authentication);

//        mockMvc.perform(get("/users/orders/{userId}", userOne.getId().toHexString()))
//                .andExpect(status().isOk())
//                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.content.content.size()").value(1))
//                .andExpect(jsonPath("$.content.[*].id", containsInAnyOrder(orderDetails.id())))
//                .andExpect(jsonPath("$.content[0].orderReference").value(orderDetails.orderReference()))
//                .andExpect(jsonPath("$.content[0].orderDate").value(orderDetails.orderDate()))
//                .andExpect(jsonPath("$.content[0].productCount").value(orderDetails.productCount()))
//                .andExpect(jsonPath("$.content[0].amount").value(orderDetails.amount()))
//                .andExpect(jsonPath("$.content[0].amount").value(orderDetails.amount()))
//                .andExpect(jsonPath("$.content[0].event.title").value(event.getTitle()))
//                .andExpect(jsonPath("$.content[0].event.date.day").value(event.getDate().day()))
//                .andExpect(jsonPath("$.content[0].event.title").value(event.getTitle()))
//                .andExpect(jsonPath("$.content[0].status").value(orderDetails.status()));
      }

    @Test
    void updateFields() {
      }

    @Test
    void updateUserAvatar() {
      }

    @Test
    void delete() {
      }

    @Test
    void deleteUserAvatar() {
      }
}