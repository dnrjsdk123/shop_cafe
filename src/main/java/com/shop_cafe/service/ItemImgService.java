package com.shop_cafe.service;

import com.shop_cafe.entity.ItemImg;
import com.shop_cafe.repository.ItemImgRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {
    @Value("${itemImgLocation}") //application.properties에 itemImgLocation
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;
    private final FileService fileService;
    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{

        String oriImgName = itemImgFile.getOriginalFilename(); // 오리지날 이미지 경로
        String imgName = "";
        String imgUrl ="";
        System.out.println(oriImgName);
        //파일 업로드
        if(!StringUtils.isEmpty(oriImgName)){ // oriImgName 문자열로 비어 있지 않으면 실행
            System.out.println("******");
            imgName = fileService.uploadFile(itemImgLocation, oriImgName,
                    itemImgFile.getBytes());
            System.out.println(imgName);
            imgUrl = "/images/item/"+imgName; //C:/Shop/item/sdkfjsldkfjlsf.jpg
        }
        System.out.println("테스트1111");
        //상품 이미지 정보 저장
        // oriImgName : 상품 이미지 파일의 원래 이름
        // imgName : 실제 로컬에 저장된 상품 이미지 파일의 이름
        // imgUrl :  로컬에 저장된 상품 이미지 파일을 불러오는 경로
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        System.out.println("테스트(((((");
        itemImgRepository.save(itemImg);
    }

    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception{
        if(!itemImgFile.isEmpty()){ // 상품의 이미지를 수정한 경우 상품 이미지 업데이트
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId).
                    orElseThrow(EntityNotFoundException::new); // 기존 엔티티 조회
            // 기존에 등록된 상품 이미지 파일이 있는경우 파일 삭제
            if(!StringUtils.isEmpty(savedItemImg.getImgName())){
                fileService.deleteFile(itemImgLocation+"/"+savedItemImg.getImgName());
            }
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName,
                    itemImgFile.getBytes()); // 파일 업로드
            String imgUrl = "/images/item/"+imgName;
            //변경된 상품 이미지 정보를 세팅
            //상품 등록을 하는 경우에는 ItemImgRepository.save()로직을 호출 하지만
            //호출을 하지 않았습니다.
            //savedItemImg 엔티티는 현재 영속성 상태이다.
            // 그래서 데이터를 변경하는 것만으로 변경을 감지기능이 동작
            // 트랜잭션이 끝날때 update 쿼리가 실행 된다.
            //※ 영속성 상태여야함 사용가능
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }
    }

    @Transactional(readOnly = true)
    public String selectProductImageUrlByProductId(Long id) {
        // 아이템 이미지 리스트를 아이템 ID로 조회
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(id);

        // 첫 번째 이미지만 가져오기
        if (itemImgList.isEmpty()) {
            throw new EntityNotFoundException("No images found for the item with ID " + id);
        }

        // 첫 번째 이미지의 URL 반환
        ItemImg firstItemImg = itemImgList.get(0);
        System.out.println(itemImgList.get(0)+"제대로 읽어오는지 확인");
        return firstItemImg.getImgUrl(); // `getImageUrl()`은 이미지 URL을 반환하는 메소드입니다
    }
}
