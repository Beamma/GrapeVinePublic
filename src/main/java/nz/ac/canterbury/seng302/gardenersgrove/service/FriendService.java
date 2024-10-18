package nz.ac.canterbury.seng302.gardenersgrove.service;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friend;
import nz.ac.canterbury.seng302.gardenersgrove.exception.FriendNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    Logger logger = LoggerFactory.getLogger(FriendService.class);

    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;
    
    public FriendService(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    /**
     * Gets all Friends from persistence
     *
     * @return all Friends currently saved in persistence
     */
    public List<Friend> getFriends() {
        return friendRepository.findAll();
    }

    public Friend addFriend(Friend friend) throws FriendNotFoundException {
        // Check if there is already pending request
        if (!getFriendRequestsByUser1User2(friend.getSender().getId(), friend.getRecipient().getId()).isEmpty()) {
            throw new FriendNotFoundException("A request has previously existed between you and this user");
        }
        return friendRepository.save(friend);
    }

    public List<Friend> getIncomingRequests(Long id) {
        
        return friendRepository.findAllPendingByUser2Id(id).orElse(null);
    }
    public List<Friend> getOutGoingRequests(Long id) {

        return friendRepository.findAllPendingAndDeclinedBySenderId(id).orElse(null);
    }


    public List<User> getCurrentUsersFriends(User currentUser) {
        return userRepository.getAllCurrentUsersFriends(currentUser.getId()).orElse(null);
    }

    public List<Friend> getFriendRequestsByUser1User2(long user1, long user2) {
        return friendRepository.findAllPendingByBothUsers(user1, user2).orElse(null);
    }

    public Friend changeFriendStatus(Long friendId, String status) throws FriendNotFoundException {
        if (friendRepository.findById(friendId).isEmpty()) {
            throw new FriendNotFoundException(String.format("Friend with the id %d does not exist", friendId));
        }
        Friend friend = friendRepository.findById(friendId).get();
        friend.setStatus(status);
        friendRepository.save(friend);
        return friend;
    }


    public boolean checkIfFriends(long user1, long user2) {
        if (user1 == user2) {
            return true;
        }

        Optional<List<Friend>> friends = friendRepository.findAllAcceptedByBothUserId(user1, user2);
        if (friends.get().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkFriendRequest(String acceptId, String declineId) {

        if (acceptId.isEmpty() && declineId.isEmpty()) {
            return false;
        }

        if (!acceptId.isEmpty() && !acceptId.matches("-?\\d+")) { // Regex checks that it is a number
            return false;
        }


        if (!declineId.isEmpty() && !declineId.matches("-?\\d+")) { // Regex checks that it is a number
            return false;
        }

        if (!acceptId.isEmpty() && Long.parseLong(acceptId) <= 0) {
            return false;
        }

        if (!declineId.isEmpty() && Long.parseLong(declineId) <= 0) {
            return false;
        }

        return true;
    }

    public void deleteFriend(Long friendId) throws FriendNotFoundException {
        if (friendRepository.findById(friendId).isEmpty()) {
            throw new FriendNotFoundException(String.format("Friend with the id %d does not exist", friendId));
        }
        friendRepository.deleteById(friendId);
    }

    public Optional<Friend> getByFriendshipId(Long friendshipId) {
        return friendRepository.findById(friendshipId);
    }
}
