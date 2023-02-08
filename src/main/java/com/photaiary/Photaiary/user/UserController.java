package com.photaiary.Photaiary.user;

import com.photaiary.Photaiary.user.dto.*;
import com.photaiary.Photaiary.user.service.UserService;
import com.photaiary.Photaiary.user.validation.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/user/signup")
    public ResponseDto signup(@RequestBody UserSaveRequestDto requestDto){
        int result=userService.save(requestDto);
        if (result==0)
            return new ResponseDto(true);
        else return new ResponseDto(false);
    }

    @PostMapping("/user/login/duplicationCheck")
    public ResponseDto idChk(@RequestBody Map<String,String> nicknameMap){
        int result=userService.idChk(nicknameMap.get("nickname"));
        if (result==0)
            return new ResponseDto(true);
        else return new ResponseDto(false);
    }

    @PostMapping("/user/emailCheck")
    public EmailCheckResponseDto emailCheck(@RequestBody Map<String,String> emailMap) throws Exception {

        String authCode=emailService.sendEmail(emailMap.get("email"));

        return new EmailCheckResponseDto(true,authCode);
    }

}
