package com.parse.starter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    ListView lstvwUsers;
    String strCurrentUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        setTitle("Tweeters");

        lstvwUsers = findViewById(R.id.lstvwUsers);
        lstvwUsers.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        final ArrayList<String> arrlststrUsernames = new ArrayList<String>();

        //arrlststrUsernames.add("");
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, arrlststrUsernames);

        lstvwUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;

                if (checkedTextView.isChecked()) {
                    ParseUser.getCurrentUser().add("Following", arrlststrUsernames.get(position));
                } else {
                    ParseUser.getCurrentUser().getList("Following").remove(arrlststrUsernames.get(position));
                    List lstTempUsers = ParseUser.getCurrentUser().getList("Following");
                    ParseUser.getCurrentUser().remove("Following");
                    ParseUser.getCurrentUser().put("Following", lstTempUsers);
                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });

        strCurrentUser = ParseUser.getCurrentUser().getUsername();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", strCurrentUser);
        query.addAscendingOrder("username");

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseUser user : objects) {
                            arrlststrUsernames.add(user.getUsername());
                        }
                        lstvwUsers.setAdapter(arrayAdapter);
                        //arrayAdapter.notifyDataSetChanged();
                        for (String strUsername : arrlststrUsernames) {
                            if (ParseUser.getCurrentUser().getList("Following").contains(strUsername)) {
                                lstvwUsers.setItemChecked(arrlststrUsernames.indexOf(strUsername), true);
                            }
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.tweet) {
            //Tweet
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Send a Tweet");
            final EditText edttxtTweet = new EditText(this);
            alertDialogBuilder.setView(edttxtTweet);
            alertDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Log.i("Tweet sent", edttxtTweet.getText().toString());
                    ParseObject objparseTweet = new ParseObject("Tweet");
                    objparseTweet.put("tweet", edttxtTweet.getText().toString());
                    objparseTweet.put("username", ParseUser.getCurrentUser().getUsername());
                    objparseTweet.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(UserListActivity.this, "Tweet sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserListActivity.this, "Sorry, something went wrong :(", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("Tweet cancelled", "Change of Mind");
                    dialog.cancel();
                }
            });

            alertDialogBuilder.show();

        } else if (item.getItemId() == R.id.logout) {
            //Logout
            ParseUser.logOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        } else if (item.getItemId() == R.id.feed) {
            Intent intent = new Intent(getApplicationContext(), UserFeedActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage = data.getData();
        try {
            if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
                Bitmap objBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                objBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                byte[] bytarrStream = outStream.toByteArray();
                ParseFile parseFile = new ParseFile("image.png", bytarrStream);
                ParseObject parseObject = new ParseObject("Image");
                parseObject.put("image", parseFile);
                parseObject.put("username", strCurrentUser);
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(UserListActivity.this, "Image has been shared!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Log.i("", "");
            } else {
                Toast.makeText(this, "There has been an issue uploading the image :(", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

}