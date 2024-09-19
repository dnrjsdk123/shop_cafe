package com.shop_cafe.service;

import com.shop_cafe.dto.RecentProduct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor // final, @NonNull 변수에 붙으면 자동 주입(Autowired)을 해줍니다.
public class RecentProductService {


    private static final String RECENT_PRODUCTS_COOKIE_NAME = "recentProducts";
    private static final String CHARSET = "UTF-8";
    private static final int MAX_PRODUCTS = 4; // 최대 3개 상품 유지

    public void saveRecentProductToCookie(Long productId, HttpServletResponse response, HttpServletRequest request,
                                          String imageUrl, String itemNm) {
        System.out.println("saveRecentProductToCookie");
        // 쿠키에서 기존의 최근 본 상품 리스트를 가져온다
        ArrayList<RecentProduct> recentProducts = new ArrayList<>(getRecentProductsFromCookie(request));

//        for (int i=0; i<recentProducts.size();i++){
//            Item item = itemService.findByItem(recentProducts.get(i).getProductId());
//            if (item==null){
//                RecentProduct remove = recentProducts.remove(i);
//            }
//        }

        // 새 상품을 추가
        RecentProduct newProduct = new RecentProduct();
        newProduct.setProductId(productId);
        newProduct.setImageUrl(imageUrl);
        newProduct.setItemNm(itemNm);


        // 이미 최근 본 리스트에 같은 상품이 있으면 제거
        recentProducts.removeIf(product -> product.getProductId().equals(productId));
        // 최신 아이템을 리스트의 앞쪽에 추가



        // 리스트의 첫 번째 위치에 추가
        recentProducts.add(0, newProduct);
        // 리스트의 크기가 MAX_PRODUCTS를 초과하지 않도록 유지
        if (recentProducts.size() > MAX_PRODUCTS) {
            recentProducts.remove(recentProducts.size() - 1); // 가장 오래된 아이템 제거 (리스트의 마지막 아이템)
        }


        // 쿠키에 저장
        String cookieValue = convertRecentProductsToString(recentProducts);
        try {
            String encodedValue = URLEncoder.encode(cookieValue, CHARSET);
            Cookie cookie = new Cookie(RECENT_PRODUCTS_COOKIE_NAME, encodedValue);
            cookie.setMaxAge(60 * 60); // 쿠키를 1일 동안 유지
            cookie.setPath("/");
            response.addCookie(cookie);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public List<RecentProduct> getRecentProductsFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (RECENT_PRODUCTS_COOKIE_NAME.equals(cookie.getName())) {
                    try {
                        String decodedValue = URLDecoder.decode(cookie.getValue(), CHARSET);
                        return convertStringToRecentProducts(decodedValue);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return new ArrayList<>();
    }


    private String convertRecentProductsToString(List<RecentProduct> recentProducts) {
        StringBuilder sb = new StringBuilder();
        for (RecentProduct product : recentProducts) {
            sb.append(product.getProductId()).append(",")
                    .append(product.getImageUrl()).append(",")
                    .append(product.getItemNm()).append(";");
        }
        return sb.toString();
    }

    private List<RecentProduct> convertStringToRecentProducts(String cookieValue) {

        List<RecentProduct> recentProducts = new ArrayList<>();
        if (cookieValue != null && !cookieValue.isEmpty()) {
            String[] items = cookieValue.split(";");
            for (String item : items) {
                if (item.isEmpty()) continue;
                String[] parts = item.split(",");
                System.out.println(parts[2]);
                if (parts.length == 3) {
                    RecentProduct product = new RecentProduct();
                    product.setProductId(Long.parseLong(parts[0]));
                    product.setImageUrl(parts[1]);
                    product.setItemNm(parts[2]);
                    recentProducts.add(product);
                }
            }
        }
        return recentProducts;
    }

}
