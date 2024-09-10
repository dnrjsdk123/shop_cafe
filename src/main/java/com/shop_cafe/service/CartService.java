package com.shop_cafe.service;

import com.shop_cafe.dto.CartDetailDto;
import com.shop_cafe.dto.CartItemDto;
import com.shop_cafe.dto.CartOrderDto;
import com.shop_cafe.dto.OrderDto;
import com.shop_cafe.entity.*;
import com.shop_cafe.repository.*;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;
    public Long addCart(CartItemDto cartItemDto, String email){
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityExistsException::new);
        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(),item.getId());
        if(savedCartItem != null){
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        }
        else{
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email){
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            return cartDetailDtoList;
        }
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        System.out.println(email + " 이메일 로그확인");

        // Step 1: Member 엔티티를 먼저 조회
        Member curMember = memberRepository.findByEmail(email);

        // Step 2: Member가 없거나 Member의 이메일이 null인 경우 User로 조회
        if (curMember == null || curMember.getEmail() == null) {
            System.out.println("Member가 없거나 이메일이 null입니다, User로 조회 시도: " + email);

            // User 엔티티 조회
            Optional<User> curUserOptional = userRepository.findBySocialId(email);
            if (!curUserOptional.isPresent()) {
                System.out.println("사용자를 찾을 수 없습니다: " + email);
                return false;
            }

            User curUser = curUserOptional.get();
            System.out.println(curUser + " 사용자 값");

            // User의 이메일로 다시 Member 조회
            curMember = memberRepository.findByEmail(curUser.getEmail());
            if (curMember == null) {
                System.out.println("User의 이메일로 Member를 찾을 수 없습니다: " + curUser.getEmail());
                return false;
            }
        }

        System.out.println(curMember + " 최종 Member 값");

        // Step 3: cartItemId를 이용해서 CartItem 엔티티 객체 추출
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);

        // Step 4: Cart -> Member 엔티티 객체를 추출
        Member savedMember = cartItem.getCart().getMember();

        // Step 5: 현재 로그인된 Member와 CartItem에 있는 Member를 비교
        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }

        return true;
    }

    public void updateCartItemCount(Long cartItemId, int count){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
        // 주문DTO List 객체 생성
        List<OrderDto> orderDtoList = new ArrayList<>();
        // 카트 주문 List에 있는 목록 -> 카트 아이템 객체로 추출
        // 주문 Dto에 CartItem 정보를 담고
        // 위에 선언된 주문 Dto List에 추가
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityExistsException::new);
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }
        // 주문DTO리스트 현재 로그인된 이메일 매개변수 넣고
        // 주문 서비스 실행 -> 주문
        Long orderId = orderService.orders(orderDtoList, email);

        //Cart에서 있던 Item 주문이 되니까 CartItem 모두 삭제
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityExistsException::new);
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }
    public void deleteCartItemsAfterOrder(List<OrderDto> orderDtos) {
        for (OrderDto orderDto : orderDtos) {
            CartItem cartItem = cartItemRepository.findByItemId(orderDto.getItemId())
                    .orElseThrow(EntityExistsException::new);
            cartItemRepository.delete(cartItem);
        }
    }
}
