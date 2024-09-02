package com.shop_cafe.contoller;

import com.shop_cafe.dto.ItemSearchDto;
import com.shop_cafe.dto.MainItemDto;
import com.shop_cafe.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    // 기본 페이지 로드
    @GetMapping(value = "/")
    public String main(ItemSearchDto itemSearchDto,
                       Optional<Integer> page,
                       Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5); // 페이지 번호와 페이지 크기 설정
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5); // 페이지네이션의 최대 페이지 수
        return "main";
    }

    // AJAX를 통한 추가 항목 로드
    @GetMapping("/loadItems")
    @ResponseBody
    public Page<MainItemDto> loadItems(ItemSearchDto itemSearchDto,
                                       @RequestParam(defaultValue = "0") int page) {
        System.out.println("");
        Pageable pageable = PageRequest.of(page, 5); // 페이지 크기 설정
        return itemService.getMainItemPage(itemSearchDto, pageable);
    }
}
