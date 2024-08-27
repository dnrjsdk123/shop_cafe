package com.shop_cafe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeBoardSearchDto {
    private String searchDateType; // 조회날짜

    private String searchBy; // 조회 유형

    private String searchQuery = ""; // 검색 단어
}
