package com.example.jtodolister;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //region Variable declarations
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab_add, fab_long, fab_pic, fab_loc, fab_time;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    public Boolean isNightModeChecked = false;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ViewGroup fragData;
    private TextView literallyNothingAdded;
    private static FragmentTransaction ft_general;
    //TODO: edit text focus listener to deflate FAB
    private static int fragCount = 0;
    public static final int STORAGE_CAMERA_PERMISSIONS = 456;
    public static final int PICK_IMAGE_FROM_STORAGE = 345;
    public static final int LOCATION_PERMISSIONS = 675;
    ImageButton dapImageButton;
    private String imageChosenPath;
    final String[] cameraStorageRWPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private Date now;
    //endregion

    //region onCreate - find views, assign them, etc
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: change theme in manifest to android:theme="@style/splashScreenTheme"
        //TODO: and uncomment next line to get splash screen
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_toolbar);

        //my beautiful expanding FAB
        getNiceFloatingActionButton();

        //nothing text gone if fragment placed
        literallyNothingAdded = (TextView) findViewById(R.id.nothing_here_textview);
    }
    //endregion

    //region Toolbar-related stuff
    //TODO: landscape mode settings (icons)
    //TODO: toolbar height dynamically changes
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //SHARE button - if it doesn't work, convert SAP to class variables
        MenuItem item_share = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item_share);
        Intent shareButtonIntent = new Intent(Intent.ACTION_SEND);
        shareButtonIntent.setType("message/rfc822");
        shareButtonIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        shareActionProvider.setShareIntent(shareButtonIntent);

        //ABOUT button in the menu
        MenuItem item_about = menu.findItem(R.id.about_menu);
        item_about.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MainActivity.this.goToAbout(item);
                return false;
            }
        });
        return true;
        //settings and night mode below
    }

    public void goToSettings(MenuItem item) {
        //TODO: intent to settings page + settings page
        //if fab is open, close it before going to about
        if (isFabOpen) {
            findViewById(R.id.fab).callOnClick();
        }
    }


    public void goToAbout(MenuItem item) {
        //if fab is open, close it before going to about
        if (isFabOpen) {
            findViewById(R.id.fab).callOnClick();
        }
        //go to about (class: AboutScreen, layout about_screen.xml (port/land separately)
        Intent aboutIntent = new Intent(this, AboutScreen.class);
        startActivity(aboutIntent);
    }

    public void toggleNightMode(MenuItem item) {
        //TODO: Night mode
        //check if night mode is on/off and toggle to a different position
        if (isNightModeChecked) {
            item.setChecked(false);
            isNightModeChecked = false;
        } else {
            item.setChecked(true);
            isNightModeChecked = true;
        }
    }
    //endregion

    //region Fragment-related stuff, addFragment methods, "Nothing here", etc
    private void removeNothingIfItsPresent() {
        if (literallyNothingAdded.getVisibility() != View.GONE) {
            literallyNothingAdded.setVisibility(View.GONE);
        }
    }

    private void addSimpleFragment(String str) {
        fragCount++;
        removeNothingIfItsPresent();
        now = new Date();
        FragmentTransaction ft_add_simple = getSupportFragmentManager().beginTransaction();
        ft_add_simple.add(R.id.content_main, SimpleAddFragment.newInstance(str, now, getFragCount()));
        ft_add_simple.commit();
    }

    private void addLongFragment(String title, String content) {
        fragCount++;
        removeNothingIfItsPresent();
        now = new Date();
        FragmentTransaction ft_add_long = getSupportFragmentManager().beginTransaction();
        ft_add_long.add(R.id.content_main, LongAddFragment.newInstance(title, content, now, getFragCount()));
        ft_add_long.commit();
    }

    private void addPicFragment(String picPath, String str) {
        fragCount++;
        removeNothingIfItsPresent();
        now = new Date();
        FragmentTransaction ft_add_pic = getSupportFragmentManager().beginTransaction();
        ft_add_pic.add(R.id.content_main, PictureAddFragment.newInstance(picPath, str, now, getFragCount()));
        ft_add_pic.commit();
    }

    private void addLocFramgent(String text) {
        fragCount++;
        //TODO: transfer chosen location
        removeNothingIfItsPresent();
        now = new Date();
        //get user's location
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        FragmentTransaction ft_add_pic = getSupportFragmentManager().beginTransaction();
        ft_add_pic.add(R.id.content_main,MapAddFragment.newInstance(text,now,latitude,longitude,getFragCount()));
        ft_add_pic.commit();
    }

    //endregion

    //region FloatingActionButton-related stuff

    private void getNiceFloatingActionButton() {
        //TODO: FAB horizontal on rotation
        //fabs for add/long/loc/pic/time are invisible and unclickable if (!isFabOpen)
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_add = (FloatingActionButton) findViewById(R.id.fab_action_add);
        fab_long = (FloatingActionButton) findViewById(R.id.fab_action_add_long);
        fab_loc = (FloatingActionButton) findViewById(R.id.fab_action_add_loc);
        fab_pic = (FloatingActionButton) findViewById(R.id.fab_action_add_pic);
        fab_time = (FloatingActionButton) findViewById(R.id.fab_action_add_time);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fab.setOnClickListener(this);
        fab_add.setOnClickListener(this);
        fab_long.setOnClickListener(this);
        fab_loc.setOnClickListener(this);
        fab_pic.setOnClickListener(this);
        fab_time.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //action based on button pressed
        //initial fab press - first case, expands/hides the speed dial
        switch (id){
            case R.id.fab:
                //if edit_text is not empty, create a simplefragment and add it to layout
                if (!editTextMainEmpty()) {
                    addSimpleFragment(getMainEditTextStringAndClearIt());
                    hideKeyBoardLoseFocus(v);
                } else {
                    animateFAB();
                }
                break;
            case R.id.fab_action_add:
                animateFAB();
                SADinit();
                break;
            case R.id.fab_action_add_long:
                animateFAB();
                LADinit();
                break;
            case R.id.fab_action_add_loc:
                animateFAB();
                MADinit();
                break;
            case R.id.fab_action_add_pic:
                animateFAB();
                PADinit();
                break;
            case R.id.fab_action_add_time:
                Log.d("XXX", "Fab time");
                break;
        }
    }

    public void animateFAB(){
        //TODO: Horizontal FAB
        if(isFabOpen){
            //then close animations, set unclickable
            fab.startAnimation(rotate_backward);
            fab_add.startAnimation(fab_close);
            fab_long.startAnimation(fab_close);
            fab_loc.startAnimation(fab_close);
            fab_pic.startAnimation(fab_close);
            fab_time.startAnimation(fab_close);
            fab_add.setClickable(false);
            fab_long.setClickable(false);
            fab_loc.setClickable(false);
            fab_pic.setClickable(false);
            fab_time.setClickable(false);
            isFabOpen = false;
        } else {
            //open animations, set clickable
            fab.startAnimation(rotate_forward);
            fab_add.startAnimation(fab_open);
            fab_long.startAnimation(fab_open);
            fab_loc.startAnimation(fab_open);
            fab_pic.startAnimation(fab_open);
            fab_time.startAnimation(fab_open);
            fab_add.setClickable(true);
            fab_long.setClickable(true);
            fab_loc.setClickable(true);
            fab_pic.setClickable(true);
            fab_time.setClickable(true);
            isFabOpen = true;
        }
    }

    //endregion

    //region Utility methods
    public static int getFragCount() {
        return fragCount;
    }

    private boolean editTextMainEmpty() {
        EditText editTextMain = (EditText) findViewById(R.id.entryEditTextMain);
        return editTextMain.getText().toString().matches("");
    }

    private String getMainEditTextStringAndClearIt() {
        EditText editTextMain = (EditText) findViewById(R.id.entryEditTextMain);
        String text = editTextMain.getText().toString().trim();
        editTextMain.setText("");
        return text;
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void hideKeyBoardLoseFocus(View v) {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static boolean hasCameraAndStoragePermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    //endregion

    //region Dialog constructors for each type of note

    private void SADinit() {
        //show dialog with custom layout
        AlertDialog.Builder xBuilder = new AlertDialog.Builder(MainActivity.this);
        View xView = getLayoutInflater().inflate(R.layout.dialog_add_simple,null);
        final EditText dasET = xView.findViewById(R.id.das_editText);
        xBuilder.setView(xView);
        final AlertDialog dialog = xBuilder.create();

        //----YES BUTTON----
        xView.findViewById(R.id.das_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: what happens if the field is empty
                Toast.makeText(MainActivity.this,getString(R.string.note_added),Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                addLocFramgent(dasET.getText().toString().trim());
            }
        });
        //----NO BUTTON----
        xView.findViewById(R.id.das_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void MADinit() {
        //ask for location permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: explanation dialog + what if no
            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSIONS);
        }

        //create dialog window
        AlertDialog.Builder xBuilder = new AlertDialog.Builder(MainActivity.this);
        View xView = getLayoutInflater().inflate(R.layout.dialog_add_loc,null);
        final EditText damET = xView.findViewById(R.id.dam_editText);
        xBuilder.setView(xView);
        final AlertDialog dialog = xBuilder.create();

        //----Yes button
        xView.findViewById(R.id.dam_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: what happens if the field is empty
                //TODO: map
                Toast.makeText(MainActivity.this,getString(R.string.note_added),Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                addLocFramgent(damET.getText().toString().trim());
            }
        });
        //----No button
        xView.findViewById(R.id.dam_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void LADinit() {
        //show dialog with custom layout
        AlertDialog.Builder xBuilder = new AlertDialog.Builder(MainActivity.this);
        View xView = getLayoutInflater().inflate(R.layout.dialog_add_long,null);
        final EditText dalTitle = xView.findViewById(R.id.dal_editText_title);
        final EditText dalContent = xView.findViewById(R.id.dal_editText_content);
        xBuilder.setView(xView);
        final AlertDialog dialog = xBuilder.create();

        //----YES BUTTON----
        xView.findViewById(R.id.dal_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: what happens if the field is empty
                Toast.makeText(MainActivity.this,getString(R.string.note_added),Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                addLongFragment(dalTitle.getText().toString().trim(),dalContent.getText().toString().trim());
            }
        });
        //----NO BUTTON----
        xView.findViewById(R.id.dal_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void PADinit() {
        //init dialog w dialog_add_pic layout
        AlertDialog.Builder xBuilder = new AlertDialog.Builder(MainActivity.this);
        View xView = getLayoutInflater().inflate(R.layout.dialog_add_pic,null);
        final EditText dapText = xView.findViewById(R.id.dap_editText);
        dapImageButton = xView.findViewById(R.id.dap_pic_upload);
        xBuilder.setView(xView);
        final AlertDialog dialog = xBuilder.create();

        //imageButton to select a pic - oh boy, here we go
        xView.findViewById(R.id.dap_pic_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasCameraAndStoragePermissions(MainActivity.this, cameraStorageRWPermissions)){
                    //TODO: dialog to explain why you need permissions - shouldShowRequestPermissionRationale()
                    //TODO: add camera option along with Gallery
                    ActivityCompat.requestPermissions(MainActivity.this, cameraStorageRWPermissions, STORAGE_CAMERA_PERMISSIONS);
                } else {
                    //this actually calls apps that the user usually views the gallery with
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                    // i have no clue about why data and type conflict, let's try what's above instead of below, it works
//                  Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                  pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE_FROM_STORAGE);

                }
            }
        });

        //----Yes button
        //TODO: add check for image, toast "image not selected" if pressed
        xView.findViewById(R.id.dap_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,R.string.note_added,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                //TODO: actually choose a pic
                addPicFragment(imageChosenPath,dapText.getText().toString().trim());
            }
        });
        //----No button
        xView.findViewById(R.id.dap_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    //endregion

    //region ActivityResult

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_FROM_STORAGE && resultCode == Activity.RESULT_OK) {
            //if we successfully got the pic
            if (data == null) {
                //error msg here if the "user" cancels or such
            } else {
                Uri selectedImageUri = data.getData();
                // Get the path from the Uri - USING CUSTOM METHOD (IT'S IN UTILITY METHODS)
                final String path = getPathFromURI(selectedImageUri);
                if (path != null) {
                    File f = new File(path);
                    selectedImageUri = Uri.fromFile(f);
                }
                dapImageButton.setImageURI(selectedImageUri);
                imageChosenPath = path;
            }
        } else {
            //nothing, i guess
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //endregion
}

