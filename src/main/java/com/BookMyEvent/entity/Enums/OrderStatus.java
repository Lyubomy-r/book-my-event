package com.BookMyEvent.entity.Enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
  UNPAID("Не оплачено"),
  PAID("Оплачено"),
  REFUNDED("Повернуто"),
  CANCELED("Скасовано");

  private final String nameUa;

  OrderStatus(String nameUa) {
    this.nameUa = nameUa;
  }
}
