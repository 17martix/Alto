package com.structurecode.alto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.structurecode.alto.Fragments.AlbumFragment;
import com.structurecode.alto.Fragments.ArtistFragment;
import com.structurecode.alto.Fragments.PlaylistFragment;
import com.structurecode.alto.Fragments.SongFragment;
import com.structurecode.alto.Helpers.Utils;
import com.structurecode.alto.Models.Playlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.structurecode.alto.Helpers.Utils.db;
import static com.structurecode.alto.Helpers.Utils.mAuth;
import static com.structurecode.alto.Helpers.Utils.user;

public class LibraryActivity extends BaseActivity  {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton floating_actions;
    private FloatingActionButton shuffle_action;
    private FloatingActionButton add_action;
    private FloatingActionButton setting_action;
    private boolean is_rotated = false;

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = findViewById(R.id.pager_music);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs_music);
        tabLayout.setupWithViewPager(viewPager);
        floating_actions = findViewById(R.id.floating_actions);
        shuffle_action = findViewById(R.id.shuffle_action);
        add_action = findViewById(R.id.add_action);
        setting_action = findViewById(R.id.setting_action);

        Utils.init(shuffle_action);
        Utils.init(add_action);
        Utils.init(setting_action);

        floating_actions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_rotated = Utils.rotateFab(v,!is_rotated);
                if(is_rotated){
                    Utils.showIn(setting_action);
                    Utils.showIn(add_action);
                    Utils.showIn(shuffle_action);
                }else{
                    Utils.showOut(setting_action);
                    Utils.showOut(add_action);
                    Utils.showOut(shuffle_action);
                }
            }
        });

        setting_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_actions.performClick();
                Intent intent=new Intent(LibraryActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });

        add_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_actions.performClick();
                display_new_playlist_dialog(R.string.new_playlist,R.string.create,"",false,0);
            }
        });

        shuffle_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_actions.performClick();
                SongFragment fragment = (SongFragment) mFragmentList.get(1);
                if (fragment.getAdapter().getItemCount() > 0) {
                    int total_Count = fragment.getRecyclerView().getAdapter().getItemCount();
                    int random = new Random().nextInt(total_Count);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragment.getRecyclerView().findViewHolderForAdapterPosition(random).itemView.performClick();
                        }
                    },1);
                }
            }
        });

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PlaylistFragment(), getString(R.string.playlists));
        adapter.addFragment(new SongFragment(), getString(R.string.songs));
        adapter.addFragment(new ArtistFragment(), getString(R.string.artists));
        adapter.addFragment(new AlbumFragment(), getString(R.string.albums));
        viewPager.setAdapter(adapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    int getLayoutId() {
        return R.layout.activity_library;
    }

    @Override
    int getBottomNavigationMenuItemId() {
        return R.id.navigation_library;
    }

    @Override
    Context getContext() {
        return LibraryActivity.this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void display_new_playlist_dialog(@StringRes int title, @StringRes int positiveText, final String playlistTitle,
                                             boolean is_private_retrieved, final int playlistId){

        View view = getLayoutInflater().inflate(R.layout.dialog_playlist, null);

        AlertDialog alertDialog = new AlertDialog.Builder(this,R.style.DialogTheme)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String exposure=getString(R.string.exposure_public);
                        boolean is_private = ((CheckBox) view.findViewById(R.id.exposure)).isChecked();
                        if(is_private) exposure=getString(R.string.exposure_private);

                        String inputTitle = ((EditText) view.findViewById(R.id.title_txt)).getText().toString();
                        if (inputTitle==null || inputTitle.isEmpty()) {
                            inputTitle = getString(R.string.untitled);
                        }else {
                            inputTitle=inputTitle.trim();
                        }

                        ArrayList<String> followers = new ArrayList<>();
                        followers.add(user.getUid());
                        String id = db.collection(Utils.COLLECTION_PLAYLISTS).document().getId();
                        Playlist playlist=new Playlist(id,inputTitle,exposure,user.getUid(),user.getDisplayName(),
                                followers);

                        DocumentReference doc = db.collection(Utils.COLLECTION_PLAYLISTS).document(id);

                        doc.set(playlist).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        /*Map<String,Object> content = new HashMap<>();
                                        content.put("id",doc.getId());
                                        doc.update(content);*/
                                        Log.d("ABC", "DocumentSnapshot successfully written!");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("ABC", "Error writing document", e);
                                    }
                                });
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }
}
