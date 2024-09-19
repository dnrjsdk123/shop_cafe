package com.shop_cafe.contoller;

import com.shop_cafe.dto.CartDetailDto;
import com.shop_cafe.dto.OrderDto;
import com.shop_cafe.dto.OrderHistDto;
import com.shop_cafe.dto.PayDto;
import com.shop_cafe.entity.Order;
import com.shop_cafe.repository.OrderRepository;
import com.shop_cafe.service.CartService;
import com.shop_cafe.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Log
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    // 기존 엔드포인트
    @PostMapping(value = "/KGInigisOrderValidCheck")
    public @ResponseBody ResponseEntity<Map<String, Object>> order(@RequestBody @Valid OrderDto orderDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", sb.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String email;
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            if ("kakao".equals(registrationId)) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttributes().get("kakao_account");
                email = (String) kakaoAccount.get("email");
            } else if ("naver".equals(registrationId)) {
                Map<String, Object> naverAccount = (Map<String, Object>) oauth2User.getAttributes().get("response");
                email = (String) naverAccount.get("email");
            } else if ("google".equals(registrationId)) {
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                throw new IllegalArgumentException("Unexpected registration id: " + registrationId);
            }
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
            email = authToken.getName();
        } else {
            throw new IllegalArgumentException("Unexpected principal type");
        }

        log.info("유효성 검사 체크 완료 문제 없음. 로그인된 이메일:" + email);

        Order order;
        try {
            order = orderService.order(orderDto, email);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        PayDto payDto = new PayDto();
        payDto.setMerchant_uid(order.getOrderItems().get(0).getId().toString());
        payDto.setPayName(order.getOrderItems().get(0).getItem().getItemNm());
        payDto.setPayAmount(String.valueOf(order.getTotalPrice()));
        payDto.setBuyerEmail(email);
        payDto.setBuyerName(order.getMember().getName());
        payDto.setBuyerTel(order.getMember().getPhone());
        payDto.setBuyerAddr(order.getMember().getAddress());
        payDto.setBuyerPostcode("TEST");

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        successResponse.put("PayDto", payDto);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    // 새 엔드포인트 추가
    @PostMapping(value = "/KGInigisOrderCartValidCheck")
    public @ResponseBody ResponseEntity<Map<String, Object>> orderCart(@RequestBody @Valid List<OrderDto> orderDtos, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", sb.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String email;
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            if ("kakao".equals(registrationId)) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttributes().get("kakao_account");
                email = (String) kakaoAccount.get("email");
            } else if ("naver".equals(registrationId)) {
                Map<String, Object> naverAccount = (Map<String, Object>) oauth2User.getAttributes().get("response");
                email = (String) naverAccount.get("email");
            } else if ("google".equals(registrationId)) {
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                throw new IllegalArgumentException("Unexpected registration id: " + registrationId);
            }
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
            email = authToken.getName();
        } else {
            throw new IllegalArgumentException("Unexpected principal type");
        }

        log.info("유효성 검사 체크 완료 문제 없음. 로그인된 이메일:" + email);

        Long orderId;
        try {
            // 주문 처리
            orderId = orderService.orders(orderDtos, email);

            // 장바구니 아이템 삭제
            cartService.deleteCartItemsAfterOrder(orderDtos);
        } catch (Exception e) {
            System.out.println("주문 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "주문 처리 중 오류 발생");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        PayDto payDto = new PayDto();
        payDto.setMerchant_uid(order.getOrderItems().get(0).getId().toString());
        payDto.setPayName(order.getOrderItems().get(0).getItem().getItemNm());
        payDto.setPayAmount(String.valueOf(order.getTotalPrice()));
        payDto.setBuyerEmail(email);
        payDto.setBuyerName(order.getMember().getName());
        payDto.setBuyerTel(order.getMember().getPhone());
        payDto.setBuyerAddr(order.getMember().getAddress());
        payDto.setBuyerPostcode("TEST");

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        successResponse.put("PayDto", payDto);
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }



    @DeleteMapping("/KGInigisOrderDELETE")
    public @ResponseBody ResponseEntity<?> KGInigisOrderCancel(@RequestBody Map<String, Long> requestBody, Principal principal) {
        Long orderId = requestBody.get("orderId");
        orderService.cancelOrder(orderId);
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok("Order deleted successfully");
    }

    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        String email = "";
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            if ("kakao".equals(registrationId)) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttributes().get("kakao_account");
                email = (String) kakaoAccount.get("email");
            } else if ("naver".equals(registrationId)) {
                Map<String, Object> naverAccount = (Map<String, Object>) oauth2User.getAttributes().get("response");
                email = (String) naverAccount.get("email");
            } else if ("google".equals(registrationId)) {
                email = (String) oauth2User.getAttributes().get("email");
            } else {
                throw new IllegalArgumentException("Unexpected registration id: " + registrationId);
            }
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
            email = authToken.getName();
        } else {
            throw new IllegalArgumentException("Unexpected principal type");
        }

        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(email, pageable);

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);
        return "order/orderHist";
    }

    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity<?> cancelOrder(@PathVariable("orderId") Long orderId, Principal principal) {
        if (!orderService.validateOrder(orderId, principal.getName())) {
            return new ResponseEntity<>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }
}
