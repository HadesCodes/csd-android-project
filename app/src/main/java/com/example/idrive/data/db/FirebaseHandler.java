package com.example.idrive.data.db;
import com.example.idrive.data.models.Lesson;
import com.example.idrive.data.models.Suggestion;
import com.example.idrive.data.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.QuerySnapshot;

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
