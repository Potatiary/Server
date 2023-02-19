package com.photaiary.Photaiary.friend.service;

import com.photaiary.Photaiary.friend.dto.FriendFollowRequestDto;
import com.photaiary.Photaiary.friend.entity.Friend;
import com.photaiary.Photaiary.friend.entity.FriendRepository;
import com.photaiary.Photaiary.user.dto.SignResponseDto;
import com.photaiary.Photaiary.user.entity.User;
import com.photaiary.Photaiary.user.entity.UserRepository;
import com.photaiary.Photaiary.user.security.JwtProvider;
import com.photaiary.Photaiary.user.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final SignService signService;
    /**
     * #Issue[Friend About addFollow Method]:
     * 1.해결
     * 2.service에서 controller로 반환할 때, 데이터를 옮기는 Dto 클래스를 사용해야 좋은 코드가 아닐까? (현재는 상태코드를 반환해주었음)
     * 3.해결
     * 4.Friend Entity에서 fromUser와 toUser의 fetch=LAZY 일때, JPA를 통한 조회 변경이 안됨.
     */
    @Transactional
    public HttpStatus makeFriend(FriendFollowRequestDto requestDto) throws Exception {
        // 상대방&내 회원 정보 존재 확인 In DB
        User toUser = userRepository.findById(requestDto.getToUserId()).get();
        User fromUser = userRepository.findById(requestDto.getFromUserId()).get();

        // 엑세스 토큰을 발급 받은 시점부터 fromUser는 이미 회원임이 증명되었다. 즉, 상대방이 없는 것만 확인하면 됨.
        if (toUser != null /*&& fromUser != null*/) { // 상대가 회원인가? (차후: 로그인 개발하고 token을 통한 구현으로 refactoring)

            // YES
            List<Friend> isFriend = friendRepository.findAll();
            Iterator<Friend> iterFriends = isFriend.iterator();

            while (iterFriends.hasNext()) {

                Friend friend = iterFriends.next();

                if (friend.equals(requestDto) ) { // 이미 친구?
                    // YES
                    return HttpStatus.BAD_REQUEST;
                }
            }

            //생성
            friendRepository.save(Friend.builder()
                    .toUser(toUser)
                    .fromUser(fromUser)
                    .build());

            return HttpStatus.OK;
        }
        // CASE: 존재하지 않는 회원일 경우 (UserNotFoundException)
        return HttpStatus.NOT_FOUND;
    }


}
// 예외 핸들링 잊지 말고 리팩토링 하자.