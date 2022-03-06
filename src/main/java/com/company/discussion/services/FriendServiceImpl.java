package com.company.discussion.services;

import com.company.discussion.models.Friend;
import com.company.discussion.models.FriendRequest;
import com.company.discussion.models.User;
import com.company.discussion.repositories.FriendRepository;
import com.company.discussion.repositories.FriendRequestRepository;
import com.company.discussion.repositories.UserRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    // accepting friend
    public ResponseEntity acceptFriend(Long requesterId, Long recipientId) {
        // need to add condition for the case where person A and person B BOTH sent a friend request to each other, but naunang i accept ni person A si person B (but still, the friend request person B has from person A is still existing), pero we can just add a conditional na kapag in-accept ito ni person B, sasabihin ("You're already friends [with Person A])
        for (Friend friend : friendRepository.findAll()) {
            if ((friend.getRequester().getId() == requesterId && friend.getRecipient().getId() == recipientId) || (friend.getRequester().getId() == recipientId && friend.getRecipient().getId() == requesterId)) {

                // add a code here that deletes the record in the friend_requests table where the friend request is already INVALID since both of the users are already friends! (basically deleting the record in the database coming from the person na nahuli sa pag accept ng friend request)
                for (FriendRequest friendRequest : friendRequestRepository.findAll()) {
                    if ((friendRequest.getSender().getId() == friend.getRequester().getId() && friendRequest.getReceiver().getId() == friend.getRecipient().getId()) || (friendRequest.getSender().getId() == friend.getRecipient().getId() && friendRequest.getReceiver().getId() == friend.getRequester().getId())) {
                        friendRequestRepository.deleteById(friendRequest.getId());
                    }
                }
                return new ResponseEntity("You're already friends!", HttpStatus.OK);
            }
        }
        // IMPORTANT: the code above ensures that for every record in the friends table, yung mga ids dun ay pwedeng pag baliktarin, e.g. (1, 2) is the same as (2, 1), so di na magcrecreate ng new record na (2, 1)

        // for now, we need the recipientId since we still don't have the token. this will serve as the 'logged-in acc'

        // grabbing the record in the FriendRequest Model with the same requesterId and recipientId
        // since we're going to use the FriendRequest model, make sure that we create a conditional statement that utilizes that model. To do this, access the friend_requests table, and then loop through all of its records, then add the necessary conditional statement
        for (FriendRequest friendRequest : friendRequestRepository.findAll()) {
            if (friendRequest.getSender().getId() == requesterId && friendRequest.getReceiver().getId() == recipientId) {

                // creating the requester and recipient (User) objects
                User requester = userRepository.findById(requesterId).get();
                User recipient = userRepository.findById(recipientId).get();

                // creating the Friend object based on the requester and recipient (User) objects
                Friend friend = new Friend(requester, recipient);

                friendRepository.save(friend);

                // deleting the record with the very same requesterId and recipientId in the friend_requests table
                friendRequestRepository.deleteById(friendRequest.getId());

                return new ResponseEntity("Accepted the friend request!", HttpStatus.CREATED);
            }
        }
        return new ResponseEntity("Exception message", HttpStatus.CREATED);
    }

    // deleting friend
    public ResponseEntity deleteFriend(Long requesterId, Long recipientId) {
        for (Friend friend : friendRepository.findAll()) {
            if ((friend.getRequester().getId() == requesterId && friend.getRecipient().getId() == recipientId) || (friend.getRequester().getId() == recipientId && friend.getRecipient().getId() == requesterId)) {
                friendRepository.deleteById(friend.getId());
                return new ResponseEntity("Delete the friend successfully!", HttpStatus.OK);
            }
        }
        return new ResponseEntity("Exception message", HttpStatus.CREATED);
    }

    // VERY IMPORTANT NOTE: the friends table is actually really complex because 'friends' is bi-directional! we just added a conditional statement for all the methods here to cover this bi-directional relationship
}
