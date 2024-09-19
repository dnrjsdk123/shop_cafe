package com.shop_cafe.contoller;

import com.shop_cafe.dto.RecentProduct;
import com.shop_cafe.service.RecentProductService;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RecentProductController {
    private final RecentProductService recentProductService;


    @GetMapping("/recentViews")
    @ResponseBody
    public ResponseEntity<List<RecentProduct>> getRecentProducts(HttpServletRequest request) throws IOException {
        // 쿠키에서 최근 본 상품 정보를 읽어옴
        List<RecentProduct> recentProducts = recentProductService.getRecentProductsFromCookie(request);
        // 현재 상품의 이미지 URL을 포함한 전체 정보를 반환
        return ResponseEntity.ok(recentProducts);
    }
}