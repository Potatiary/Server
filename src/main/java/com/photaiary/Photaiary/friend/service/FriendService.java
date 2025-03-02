package com.photaiary.Photaiary.friend.service;

import com.photaiary.Photaiary.friend.dto.FriendFollowRequestDto;
import com.photaiary.Photaiary.friend.entity.Friend;
import com.photaiary.Photaiary.friend.entity.FriendRepository;
import com.photaiary.Photaiary.friend.exception.custom.AlreadyInitializedExceptionFriend;
import com.photaiary.Photaiary.friend.exception.custom.NoUserException;
import com.photaiary.Photaiary.user.entity.User;
import com.photaiary.Photaiary.user.entity.UserRepository;
import com.photaiary.Photaiary.user.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public HttpStatus makeFriend(FriendFollowRequestDto requestDto) throws Exception { //👨‍💻
        // 상대방&내 회원 정보 존재 확인
        String fromUserEmail = jwtProvider.getEmail(requestDto.getFromUserToken());
        Optional<User> fromUser = userRepository.findByEmail(fromUserEmail);

        String toUserEmail = requestDto.getToUserEmail();
        Optional<User> toUser = userRepository.findByEmail(toUserEmail);

        boolean isFriend;

        if (toUser.isPresent()) { // 상대가 회원인가?
            // YES
            Friend requestedFriend = Friend.builder()
                    .toUser(toUser.get())
                    .fromUser(fromUser.get())
                    .build();

            List<Friend> friends = friendRepository.findAll();
            Iterator<Friend> iterFriends = friends.iterator();

            while (iterFriends.hasNext()) {

                Friend iterFriend = iterFriends.next();

                isFriend = (
                        (toUserEmail.equals(iterFriend.getToUser().getEmail()))
                                &&(fromUserEmail.equals(iterFriend.getFromUser().getEmail()))
                );

                if (isFriend) { // 이미 친구?
                    // YES
                    throw new AlreadyInitializedExceptionFriend("이미 존재하는 친구입니다.");
                    //return HttpStatus.BAD_REQUEST;
                }
            }

            //생성
            friendRepository.save(Friend.builder()
                    .toUser(toUser.get())
                    .fromUser(fromUser.get())
                    .build());

            return HttpStatus.OK;

        } else if (toUser.isEmpty()) {
            throw  new NoUserException("상대방이 존재 x");
        }
        // CASE: 존재하지 않는 회원일 경우 (UserNotFoundException)
        //return HttpStatus.NOT_FOUND;
        return null;
    }

    @Transactional
    public HttpStatus unFollow(FriendFollowRequestDto requestDto) throws Exception{// 👨‍💻
        // 상대방&내 회원 정보 존재 확인 In DB (If not exist, then impossible!)

        String fromUserEmail = jwtProvider.getEmail(requestDto.getFromUserToken());
        Optional<User> fromUser = userRepository.findByEmail(fromUserEmail);

        String toUserEmail = requestDto.getToUserEmail();
        Optional<User> toUser = userRepository.findByEmail(toUserEmail);

        boolean isFriend;

        if (toUser.isPresent()) {
            // 친구가 없으면 절교도 할 수 없다
            // YES

            List<Friend> friends = friendRepository.findAll();
            Iterator<Friend> iterFriends = friends.iterator();


            //이 반복문 stream() 구조 탐색해도 되려나?
            while (iterFriends.hasNext()) {

                Friend iterFriend = iterFriends.next();

                // 친구관계 확인 (리팩토링 0219 07:24)
                isFriend = ((toUserEmail.equals(iterFriend.getToUser().getEmail()))
                        &&(fromUserEmail.equals(iterFriend.getFromUser().getEmail())));


                if (isFriend) { // Already friend?
                    // YES!(possible to deleting)
                    friendRepository.delete(iterFriend);

                    return HttpStatus.OK;
                }
                //⚠️[ISSUE: O(N) -> 정보가 많을 수록 느려진다. 어떻게 할 것 인가?]
            }
            throw new AlreadyInitializedExceptionFriend("존재하는 친구지만, 당신과 친구가 아닙니다.(삭제불가)");
        } else if (toUser.isEmpty()) {
            // CASE: this user is not exist (UserNotFoundException)
            throw new NoUserException("상대방이 존재하지 않는 회원입니다.(삭제불가)");
        }
        return null;
    }

    @Transactional
    public List<String> readFriends(String token) throws Exception{ // 😊
        // Check myUserId(fromUser) exist in useDB. (If not exist, then impossible!) (second develop -> using user token)
        List<String> myFriends= new ArrayList<>();
        String fromUserEmail = jwtProvider.getEmail(token);
        Optional<User> fromUser = userRepository.findByEmail(fromUserEmail);

        List<Friend> friends = friendRepository.findAll();
        Iterator<Friend> iterFriends = friends.iterator();

        while (iterFriends.hasNext()) {
            Friend iterFriend = iterFriends.next();

            //Find a friend of the myUser.
            //필요한 것 : iterFriend의 토큰 값 -> 대신 토큰으로 이메일을 찾아서 이메일 비교
            if (iterFriend.getFromUser().getEmail().equals(fromUserEmail)) { //yes( unique case )
                myFriends.add(iterFriend.getToUser().getNickname());
            }
        }

        if(myFriends.isEmpty()){
            throw new AlreadyInitializedExceptionFriend("당신은 왕따입니다. 친구 0명");
        }

        return myFriends; // the friends of the myUser (LIST TYPE)
    }

    @Transactional
    public String findByNicknameStartingWith(String keyword) throws Exception{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = userRepository.findByEmail(auth.getName());

        if(!user.isPresent()){
            throw new NoUserException("유효하지 않은 사용자입니다.");
        }

        Optional<String> searchedUsers = userRepository.findByNicknameContaining(keyword);//🔨

        if(!searchedUsers.isPresent()){//🔨
            throw new NoUserException("존재하지 않는 닉네임 입니다.");
        }

        return searchedUsers.get();//🔨
    }
}