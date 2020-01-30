package com.structurecode.alto.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.structurecode.alto.AuthActivity;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Setting;
import com.structurecode.alto.R;

import java.util.HashMap;
import java.util.Map;

public class SettingFragment extends PreferenceFragmentCompat {
    private CheckBoxPreference is_library_downloaded;
    private CheckBoxPreference is_playlist_downloaded;
    private ListPreference cache_size;
    private Preference log_out;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        is_library_downloaded=findPreference("is_library_downloaded");
        is_playlist_downloaded=findPreference("is_playlist_downloaded");
        cache_size=findPreference("cache_size");
        log_out=findPreference("log_out");

        mAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DocumentReference docRef = db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                .collection(Utils.COLLECTION_SETTINGS).document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("ABC", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {

                    Setting setting = snapshot.toObject(Setting.class);
                    if (setting.getIs_library_downloaded()==1) is_library_downloaded.setChecked(true);
                    else is_library_downloaded.setChecked(false);

                    if (setting.getIs_playlist_downloaded()==1) is_playlist_downloaded.setChecked(true);
                    else is_playlist_downloaded.setChecked(false);

                    int cache_index=0;
                    if (setting.getCache_size()==100) cache_index=0;
                    else if (setting.getCache_size()==500) cache_index=1;
                    else if (setting.getCache_size()==1000) cache_index=2;
                    else if (setting.getCache_size()==5000) cache_index=3;

                    cache_size.setValueIndex(cache_index);
                    cache_size.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());

                } else {
                    Log.d("ABC", "Current data: null");
                }
            }
        });



        is_library_downloaded.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int value;
                if ((boolean)newValue==true) value=1;
                else value=0;

                Map<String, Object> map = new HashMap<>();
                map.put("is_library_downloaded", value);

                db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                        .collection(Utils.COLLECTION_SETTINGS).document(user.getUid())
                        .set(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("ABC", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("ABC", "Error writing document", e);
                            }
                        });


                return true;
            }
        });

        is_playlist_downloaded.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value;
                if ((boolean)newValue==true) value=1;
                else value=0;

                Map<String, Object> map = new HashMap<>();
                map.put("is_playlist_downloaded", value);

                db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                        .collection(Utils.COLLECTION_SETTINGS).document(user.getUid())
                        .set(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("ABC", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("ABC", "Error writing document", e);
                            }
                        });
                return true;
            }
        });

        cache_size.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Map<String, Object> map = new HashMap<>();
                map.put("cache_size", newValue.toString());

                db.collection(Utils.COLLECTION_USERS).document(user.getUid())
                        .collection(Utils.COLLECTION_SETTINGS).document(user.getUid())
                        .set(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("ABC", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("ABC", "Error writing document", e);
                            }
                        });
                return true;
            }
        });

        log_out.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mAuth.signOut();
                Intent i=new Intent(getContext(), AuthActivity.class);
                startActivity(i);
                getActivity().finish();
                return false;
            }
        });
    }
}
