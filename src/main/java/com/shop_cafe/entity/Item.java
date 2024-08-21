package com.shop_cafe.entity;

import com.shop_cafe.constant.ItemSellStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity{
    @Id
    @Column(name = "item_Id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; //상품고유번호
    @Column(nullable = false)
    private Long categoryId; //상품 카테고리 아이디
    @Column(nullable = false, length = 50)
    private String itemNm; //상품명
    @Column(nullable = false, name = "price")
    private int price; //가격
    @Column(nullable = false)
    private int stockNumber; // 수량
    @Lob
    @Column(nullable = false)
    private String itemDetail; //상품상세 설명
    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; //상품판매 상태
}
