package com.shop_cafe.repository;

import com.shop_cafe.entity.NoticeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface NoticeBoardRepository extends JpaRepository<NoticeBoard, Long>,
        QuerydslPredicateExecutor<NoticeBoard>, NoticeBoardRepositoryCustom {

    // select * from NoticeBd where title = ?(String title)
    List<NoticeBoard> findByTitle(String title); // 제목


}
