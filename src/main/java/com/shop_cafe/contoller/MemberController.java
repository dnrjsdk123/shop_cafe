package com.shop_cafe.contoller;

import com.shop_cafe.dto.MemberFormDto;
import com.shop_cafe.entity.Member;
import com.shop_cafe.service.MailService;
import com.shop_cafe.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    String confirm = "";
    boolean confirmCheck = false;

    @GetMapping(value = "/new")
    public String memberForm(Model model){
        System.out.println("맴버체크1");
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }
    @PostMapping(value = "/new")
    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult,
                             Model model) {
        System.out.println("맴버체크2");
        // @Valid 붙은 객체를 검사해서 결과에 에러가 있으면 실행
        if(bindingResult.hasErrors()){
            return "member/memberForm";//다시 회원가입으로 돌려보닙니다.
        }

        try{
            //Member 객체 생성
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            //데이터베이스에 저장
            memberService.saveMember(member);
            System.out.println("회원가입 완료: " + member);
        }
        catch (IllegalStateException e){
            model.addAttribute("errorMessage",e.getMessage());
            return "member/memberForm";
        }
        return "redirect:/";
    }

    @GetMapping(value = "/login")
    public String loginMember(){
        return "/member/memberLoginForm";
    }
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg","아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }

    @GetMapping(value = "/myPage")//마이페이지
    public String myPage(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println(username+"넌누구니");
        model.addAttribute("username", username);
        return "/member/memberMyPage";
    }

    @PostMapping("/{email}/emailConfirm")
    public @ResponseBody ResponseEntity emailConfirm(@PathVariable("email") String email)
            throws Exception{
        confirm = mailService.sendSimpleMessage(email);
        return new ResponseEntity<String>("인증 메일을 보냈습니다.", HttpStatus.OK);
    }

    @PostMapping("/{code}/codeCheck")
    public @ResponseBody ResponseEntity codeConfirm(@PathVariable("code")String code)
            throws Exception{
        if (confirm.equals(code)){
            confirmCheck = true;
            return new ResponseEntity<String>("인증 성공 하였습니다.",HttpStatus.OK);
        }
        return new ResponseEntity<String>("인증 코드를 올바르게 입력해주세요.", HttpStatus.BAD_REQUEST);
    }
}
