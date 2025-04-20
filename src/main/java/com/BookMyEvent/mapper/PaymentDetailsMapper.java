package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.PaymentDetails;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.PaymentResponseDTO;
import com.BookMyEvent.entity.dto.PaymentStatusResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface PaymentDetailsMapper {

  PaymentDetails toPaymentDetailsFromPaymentResponseDTO(PaymentResponseDTO paymentResponseDTO);

  @Mapping(target = "merchantAccount", source = "response.merchantAccount")
  @Mapping(target = "merchantSignature", source = "response.merchantSignature")
//  @Mapping(target = "amount", source = "response.amount")
  @Mapping(target = "currency", source = "response.currency")
  @Mapping(target = "clientEmail", source = "response.clientEmail")
  @Mapping(target = "clientPhone", source = "response.clientPhone")
  @Mapping(target = "authCode", source = "status.authCode")
  @Mapping(target = "processingDate", source = "status.processingDate")
  @Mapping(target = "cardPan", source = "status.cardPan")
  @Mapping(target = "issuerBankCountry", source = "status.issuerBankCountry")
  @Mapping(target = "issuerBankName", source = "status.issuerBankName")
  @Mapping(target = "recToken", source = "status.recToken")
  @Mapping(target = "transactionStatus", source = "status.transactionStatus")
  @Mapping(target = "reason", source = "status.reason")
  @Mapping(target = "reasonCode", source = "status.reasonCode")
  @Mapping(target = "fee", source = "status.fee")
  @Mapping(target = "paymentSystem", source = "status.paymentSystem")
  @Mapping(target = "merchantAuthType", source = "response.merchantAuthType")
  @Mapping(target = "merchantDomainName", source = "response.merchantDomainName")
  @Mapping(target = "orderTimeout", source = "response.orderTimeout")
  @Mapping(target = "holdTimeout", source = "response.holdTimeout")
  @Mapping(target = "product", source = "response.product")
  @Mapping(target = "clientFirstName", source = "response.clientFirstName")
  @Mapping(target = "clientLastName", source = "response.clientLastName")
  @Mapping(target = "defaultPaymentSystem", source = "response.defaultPaymentSystem")
  @Mapping(target = "serviceUrl", source = "response.serviceUrl")
  PaymentDetails toPaymentDetails(PaymentDetails response, PaymentStatusResponseDTO status);

  @Mapping(target = "merchantAccount", source = "response.merchantAccount")
  @Mapping(target = "merchantSignature", source = "response.merchantSignature")
//  @Mapping(target = "amount", source = "response.amount")
  @Mapping(target = "currency", source = "response.currency")
  @Mapping(target = "clientEmail", source = "response.clientEmail")
  @Mapping(target = "clientPhone", source = "response.clientPhone")
  @Mapping(target = "authCode", source = "status.authCode")
  @Mapping(target = "processingDate", source = "status.processingDate")
  @Mapping(target = "cardPan", source = "status.cardPan")
  @Mapping(target = "issuerBankCountry", source = "status.issuerBankCountry")
  @Mapping(target = "issuerBankName", source = "status.issuerBankName")
  @Mapping(target = "recToken", source = "status.recToken")
  @Mapping(target = "transactionStatus", source = "status.transactionStatus")
  @Mapping(target = "reason", source = "reason")
  @Mapping(target = "reasonCode", source = "status.reasonCode")
  @Mapping(target = "fee", source = "status.fee")
  @Mapping(target = "paymentSystem", source = "status.paymentSystem")
  @Mapping(target = "merchantAuthType", source = "response.merchantAuthType")
  @Mapping(target = "merchantDomainName", source = "response.merchantDomainName")
  @Mapping(target = "orderTimeout", source = "response.orderTimeout")
  @Mapping(target = "holdTimeout", source = "response.holdTimeout")
  @Mapping(target = "product", source = "response.product")
  @Mapping(target = "clientFirstName", source = "response.clientFirstName")
  @Mapping(target = "clientLastName", source = "response.clientLastName")
  @Mapping(target = "defaultPaymentSystem", source = "response.defaultPaymentSystem")
  @Mapping(target = "serviceUrl", source = "response.serviceUrl")
  PaymentDetails toPaymentDetails(PaymentDetails response, PaymentStatusResponseDTO status, String reason);
}
