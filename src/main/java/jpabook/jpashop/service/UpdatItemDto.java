package jpabook.jpashop.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatItemDto {
    String name;
    int price;
    int stockQuantity;
}
