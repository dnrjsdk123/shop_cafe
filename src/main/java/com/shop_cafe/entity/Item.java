package com.shop_cafe.entity;

import com.shop_cafe.constant.ItemSellStatus;
import com.shop_cafe.dto.ItemFormDto;
import com.shop_cafe.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="member_item",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Member> member;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemImg> images = new ArrayList<>();

    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.categoryId=itemFormDto.getCategoryId();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber; // 10,  5 / 10, 20
        if(restStock<0){
            throw new OutOfStockException("상품의 재고가 부족합니다.(현재 재고 수량: "+this.stockNumber+")");
        }
        this.stockNumber = restStock; // 5
    }

    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }
}
