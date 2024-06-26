package com.lec.spring.controller;

import com.lec.spring.config.PrincipalDetails;
import com.lec.spring.domain.Post;
import com.lec.spring.domain.User;
import com.lec.spring.domain.UserAuthority;
import com.lec.spring.service.ManagerService;
import com.lec.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("mypage/customer")
public class CustomerController {

    @Autowired
    private UserService userService;


//    private ManagerService managerService;

    @GetMapping("/ManageAccount")
    public String manageAccount(Model model) {
        User user = getLoggedUser();
        System.out.println("user: " + user);
        model.addAttribute("user", user);

        List<UserAuthority> userAuthorities = userService.getAllUserAuthorities();
        model.addAttribute("userAuthorities", userAuthorities);
        return "mypage/customer/ManageAccount";
    }

    @PostMapping("/ManageAccount")
    public String updateAccount(@RequestParam(required = false) String nickname,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String currentPassword,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        User user = getLoggedUser();

        // 현재 비밀번호가 입력되었는지 확인
        if (currentPassword != null && !currentPassword.isEmpty()) {
            // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
            if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                return "redirect:/mypage/customer/ManageAccount";
            }

            // 비밀번호를 업데이트 파라미터로 포함
            password = newPassword;
        }

        // 다른 사용자 정보 업데이트
        userService.updateUser(user.getUserId(), nickname, password, email, phone);

        // 수정이 성공적으로 완료되었음을 알림
        redirectAttributes.addFlashAttribute("success", "수정되었습니다.");

        // 홈으로 리다이렉트
        return "/home";
    }

    @PostMapping("/check-password")
    public String checkPassword(@RequestParam String currentPassword) {
        User user = getLoggedUser();
        if (userService.checkPassword(user.getUserId(), currentPassword)) {
            return "success";
        } else {
            return "failure";
        }
    }

    @GetMapping("/getProvider")
    @ResponseBody
    public String getProvider() {
        User user = getLoggedUser();
        System.out.println(user.getProvider());
        return user.getProvider();
    }

    private User getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof PrincipalDetails) {
            PrincipalDetails principalDetails = (PrincipalDetails) principal;
            System.out.println(principalDetails.getUser());
            return principalDetails.getUser();
        } else if (principal instanceof DefaultOAuth2User) {
            Map<String, Object> attributes = ((DefaultOAuth2User) principal).getAttributes();
            String nickname = (String) attributes.get("nickname");
            return userService.findByNickname(nickname);
        } else if (principal instanceof String) {
            String username = (String) principal;
            return userService.findByUsername(username);
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }
    }




}






