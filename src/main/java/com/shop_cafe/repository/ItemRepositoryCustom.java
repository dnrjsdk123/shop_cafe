package com.shop_cafe.repository;

import com.shop_cafe.dto.ItemSearchDto;
import com.shop_cafe.dto.MainItemDto;
import com.shop_cafe.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
}
