package com.parse.starter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFeedActivity extends AppCompatActivity {

    ListView lstvwTweets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        setTitle("My Feed");

        lstvwTweets = findViewById(R.id.lstvwTweets);

        final List<Map<String, String>> tweetData = new ArrayList<>();



        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Tweet");;
        query.whereContainedIn("username", ParseUser.getCurrentUser().getList("Following"));
        query.orderByDescending("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    for(ParseObject objParseTweet: objects){
                        Map<String, String> tweetInfo = new HashMap<>();
                        tweetInfo.put("content", objParseTweet.getString("tweet"));
                        tweetInfo.put("username", objParseTweet.getString("username"));

                        tweetData.add(tweetInfo);
                    }

                    SimpleAdapter simpleAdapter =
                            new SimpleAdapter(UserFeedActivity.this, tweetData, android.R.layout.simple_list_item_2,
                                    new String[] {"content", "username"}, new int[] {android.R.id.text1, android.R.id.text2});

                    lstvwTweets.setAdapter(simpleAdapter);
                }
            }
        });


    }
}