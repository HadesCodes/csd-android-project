package com.example.idrive.data.db;
import com.example.idrive.data.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class FirebaseHandler {
    private final FirebaseFirestore db;

    public FirebaseHandler() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(User user, String uid,
                        OnSuccessListener<Void> successListener,
                        OnFailureListener failureListener) {
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void getUser(String uid,
                        OnSuccessListener<DocumentSnapshot> successListener,
                        OnFailureListener failureListener) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
}
