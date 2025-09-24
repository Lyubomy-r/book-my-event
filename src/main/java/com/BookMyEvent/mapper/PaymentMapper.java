package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.PaymentDetails;
import com.BookMyEvent.entity.dto.PaymentRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
        injectionStrategy = CONSTRUCTOR,
        nullValuePropertyMappingStrategy = IGNORE)
public interface PaymentMapper {
    @Mapping(target = "transactionStatus", constant = "Approved")
    @Mapping(target = "reason", constant = "Ok")
    @Mapping(target = "merchantAccount", ignore = true)
    @Mapping(target = "merchantAuthType", ignore = true)
    @Mapping(target = "merchantDomainName", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "orderTimeout", ignore = true)
    @Mapping(target = "holdTimeout", ignore = true)
    @Mapping(target = "defaultPaymentSystem", ignore = true)
    @Mapping(target = "serviceUrl", ignore = true)
    @Mapping(target = "merchantSignature", ignore = true)
    @Mapping(target = "authCode", ignore = true)
    @Mapping(target = "processingDate", ignore = true)
    @Mapping(target = "cardPan", ignore = true)
    @Mapping(target = "issuerBankCountry", ignore = true)
    @Mapping(target = "issuerBankName", ignore = true)
    @Mapping(target = "recToken", ignore = true)
    @Mapping(target = "reasonCode", ignore = true)
    @Mapping(target = "fee", ignore = true)
    @Mapping(target = "paymentSystem", ignore = true)
    PaymentDetails toPaymentDetailsForFreePurchase(PaymentRequestDTO request);
}