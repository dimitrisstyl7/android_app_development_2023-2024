package com.dimstyl.chatroom;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
    // Initialize Firebase Authentication
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Initialize Firebase Realtime Database
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();

    static FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    static String getUID() {
        return getUser().getUid();
    }

    static String getNickname() {
        return getUser().getDisplayName();
    }

    static void signIn(String email, String password, MainActivity activity) {
        auth.signInWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (getUser() == null) {
                            activity.showMessage("Error", "Something went wrong, please try again.");
                            signOut();
                            return;
                        }
                        activity.openChatroom();
                    } else {
                        activity.showMessage("Error", "Please check your credentials.");
                    }
                }
        );
    }

    static void signUp(String email, String password, String nickname, MainActivity activity) {
        auth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                            if (getUser() == null) {
                                activity.showMessage("Error", "Something went wrong, contact support!");
                                signOut();
                                return;
                            }

                            // Set authenticated user's nickname
                            setUserNickname(nickname);

                            // Add user's uid, email and nickname to database (for future use - chatroom)
                            addUserToDatabase(email, nickname);

                            activity.showMessage("Success", "User profile created successfully!");
                            activity.openChatroom();
                        }
                )
                .addOnFailureListener(e -> {
                            activity.showMessage("Error", "Please check your credentials.\n\nWarning:\n\t> Password must be at least 6 characters long!\n\t> Email must not be already in use!");
                        }
                );
    }

    static void signOut() {
        auth.signOut();
    }

    private static void setUserNickname(String nickname) {
        getUser().updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build()
        );
    }

    private static void addUserToDatabase(String email, String nickname) {
        DatabaseReference reference = database.getReference().child("users").child(getUID());
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("nickname", nickname);
        reference.setValue(userData);
    }
}