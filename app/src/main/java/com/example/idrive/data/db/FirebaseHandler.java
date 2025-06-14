package com.example.idrive.data.db;
import com.example.idrive.data.models.Lesson;
import com.example.idrive.data.models.Suggestion;
import com.example.idrive.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FirebaseHandler {
    private final FirebaseFirestore db;
    private static final String USERS = "users";
    private static final String LESSONS = "lessons";
    private static final String SUGGESTIONS = "suggestions";

    public FirebaseHandler() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(User user, String uid) {
        db.collection(USERS).document(uid).set(user);
    }

    public void getUser(String uid,
                        OnSuccessListener<DocumentSnapshot> successListener,
                        OnFailureListener failureListener) {
        db.collection(USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void getCurrentUser(OnSuccessListener<DocumentSnapshot> successListener,
                               OnFailureListener failureListener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            getUser(currentUser.getUid(), successListener, failureListener);
        } else {
            failureListener.onFailure(new Exception("No current user exists"));
        }
    }

    public void getAllMembers(Consumer<Map<String, String>> onSuccess,
                              Consumer<Exception> onFailure) {
        db.collection(USERS)
                .whereEqualTo("isAdmin", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, String> uidToName = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String fullName = doc.getString("firstName") + " " + doc.getString("lastName");
                        uidToName.put(doc.getId(), fullName);
                    }
                    onSuccess.accept(uidToName);
                })
                .addOnFailureListener(onFailure::accept);
    }

    public void addLesson(Lesson lesson) {
        String docId = db.collection(LESSONS).document().getId();
        lesson.setId(docId);
        db.collection(LESSONS).document(docId).set(lesson);
    }

    public void getLessonsWithUID(String userId,
                                  OnSuccessListener<QuerySnapshot> successListener,
                                  OnFailureListener failureListener) {
        db.collection(LESSONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void getLessonList(
            BiConsumer<List<Lesson>, Map<String, String>> onSuccess,
            Consumer<Exception> onFailure) {
        db.collection(LESSONS).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Lesson> lessons = new ArrayList<>();
                    Set<String> uids = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Lesson lesson = doc.toObject(Lesson.class);
                        if (lesson != null) {
                            lessons.add(lesson);
                            uids.add(lesson.getUserId());
                        }
                    }
                    db.collection(USERS).whereIn(FieldPath.documentId(), new ArrayList<>(uids))
                            .get()
                            .addOnSuccessListener(userSnapshot -> {
                                Map<String, String> uidToFullName = new HashMap<>();
                                for (DocumentSnapshot doc : userSnapshot.getDocuments()) {
                                    String fullName = doc.getString("firstName") + " " + doc.getString("lastName");
                                    uidToFullName.put(doc.getId(), fullName);
                                }
                                onSuccess.accept(lessons, uidToFullName);
                            })
                            .addOnFailureListener(onFailure::accept);
                })
                .addOnFailureListener(onFailure::accept);
    }

    public void updateLesson(String id, Lesson newLesson) {
        db.collection(LESSONS).document(id).set(newLesson);
    }

    public void deleteLesson(String id) {
        db.collection(LESSONS).document(id).delete();
    }

    public void addSuggestion(Suggestion suggestion) {
        String docId = db.collection(SUGGESTIONS).document().getId();
        suggestion.setId(docId);
        db.collection(SUGGESTIONS).document(docId).set(suggestion);
    }

    public void getSuggestionsWithUID(String userId,
                                      OnSuccessListener<QuerySnapshot> successListener,
                                      OnFailureListener failureListener) {
        db.collection(SUGGESTIONS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void updateSuggestion(String id, String newSuggestion) {
        db.collection(SUGGESTIONS).document(id).set(newSuggestion);
    }

    public void deleteSuggestion(String id) {
        db.collection(SUGGESTIONS).document(id).delete();
    }
}
