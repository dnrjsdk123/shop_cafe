package com.shop_cafe.entity;

import com.shop_cafe.dto.NoticeBoardDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "notice_bd")
@Getter
@Setter
@ToString
public class NoticeBoard extends BaseEntity {
    // 기본키 컬럼명
    @Id
    @Column(name = "notice_bd_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // 게시글 번호
    private String title; // 제목
    private String content; // 내용
    @ColumnDefault("0")
    @Column(nullable = false)
    private int view;

    @ManyToOne
    @JoinColumn(name = "member_id") // 외래키 컬럼명
    private Member member; // 작성자


    public void updateNoticeBd(NoticeBoardDto noticeBoardDto){
        this.title = noticeBoardDto.getTitle();
        this.content = noticeBoardDto.getContent();
    }
}
