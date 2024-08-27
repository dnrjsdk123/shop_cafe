package com.shop_cafe.repository;

import com.shop_cafe.dto.NoticeBoardSearchDto;
import com.shop_cafe.entity.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeBoardRepositoryCustom {
    Page<NoticeBoard> getAdminNoticePage(NoticeBoardSearchDto noticeBoardSearchDto, Pageable pageable);

}
