package com.example.mentalhealth.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.mentalhealth.LoginActivity;
import com.example.mentalhealth.R;

public class PersonalFragment extends Fragment {
    private TextView tvUserEmail;
    private TextView tvUserNickname;
    private EditText editNickname;
    private ImageView imgProfileAvatar;
    private Button btnSaveProfile;
    private Button btnChangeAvatar;
    private Button btnLogout;

    private SharedPreferences preferences;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<String[]> openImageLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserNickname = view.findViewById(R.id.tv_user_nickname);
        editNickname = view.findViewById(R.id.edit_nickname);
        imgProfileAvatar = view.findViewById(R.id.img_profile_avatar);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        btnChangeAvatar = view.findViewById(R.id.btn_change_avatar);
        btnLogout = view.findViewById(R.id.btn_logout);

        preferences = requireActivity().getSharedPreferences("app_prefs", 0);
        initLaunchers();
        bindProfile();
        bindActions();
    }

    private void initLaunchers() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        openImageLauncher.launch(new String[] {"image/*"});
                    } else {
                        Toast.makeText(getContext(), "Permission denied: cannot pick photo", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        openImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }
                    preferences.edit().putString("profile_avatar_uri", uri.toString()).apply();
                    imgProfileAvatar.setImageURI(uri);
                }
        );
    }

    private void bindProfile() {
        String userEmail = preferences.getString("user_email", "N/A");
        String nickname = preferences.getString("profile_nickname", "");
        String avatarUri = preferences.getString("profile_avatar_uri", null);

        tvUserEmail.setText("Email: " + userEmail);
        tvUserNickname.setText("Nickname: " + (nickname.isEmpty() ? "-" : nickname));
        editNickname.setText(nickname);

        if (avatarUri != null && !avatarUri.trim().isEmpty()) {
            imgProfileAvatar.setImageURI(Uri.parse(avatarUri));
        } else {
            imgProfileAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void bindActions() {
        btnSaveProfile.setOnClickListener(v -> {
            String newNickname = editNickname.getText().toString().trim();
            preferences.edit().putString("profile_nickname", newNickname).apply();
            tvUserNickname.setText("Nickname: " + (newNickname.isEmpty() ? "-" : newNickname));
            Toast.makeText(getContext(), "Profile saved", Toast.LENGTH_SHORT).show();
        });

        btnChangeAvatar.setOnClickListener(v -> {
            String permission = Build.VERSION.SDK_INT >= 33
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                openImageLauncher.launch(new String[] {"image/*"});
            } else {
                permissionLauncher.launch(permission);
            }
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}
