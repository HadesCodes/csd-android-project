package com.example.idrive.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.idrive.R;
import com.example.idrive.activities.AuthActivity;
import com.example.idrive.activities.HomePageActivity;
import com.example.idrive.data.db.FirebaseHandler;
import com.example.idrive.data.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {
    Button btnRegister, btnGoLogin;
    private EditText etFirstName, etLastName, etEmail, etPhone, etPassword;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseHandler handler = new FirebaseHandler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    private void registerUser(String firstName, String lastName, String email, String phone, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser == null) return;
                User newUser = new User(firstName, lastName, phone, email, password);

                handler.addUser(newUser, firebaseUser.getUid(),
                        aVoid -> {
                            Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                            goToHomepage(newUser);
                        },
                        e -> Toast.makeText(getContext(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToHomepage(User user) {
        Intent intent = new Intent(getActivity(), HomePageActivity.class);
        intent.putExtra("is_admin", user.getIsAdmin());
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etFirstName = view.findViewById(R.id.etFirstNameRegister);
        etLastName = view.findViewById(R.id.etLastNameRegister);
        etEmail = view.findViewById(R.id.etEmailRegister);
        etPhone = view.findViewById(R.id.etPhoneRegister);
        etPassword = view.findViewById(R.id.etPasswordRegister);
        btnRegister = view.findViewById(R.id.btnConfirmRegister);
        btnGoLogin = view.findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Invalid email");
                etEmail.requestFocus();
                return;
            }
            if (!Patterns.PHONE.matcher(phone).matches()) {
                etPhone.setError("Invalid Phone");
                etPhone.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            registerUser(firstName, lastName, email, phone, password);
        });

        btnGoLogin.setOnClickListener(v -> {
            if(getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).loadFragment(new LoginFragment());
            }
        });
    }
}
