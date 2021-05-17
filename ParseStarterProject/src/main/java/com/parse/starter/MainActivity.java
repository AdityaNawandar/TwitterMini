/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class MainActivity extends AppCompatActivity implements View.OnKeyListener {

  Button btnLogin;
  TextView tvSignupOrLogin;
  EditText etUsername;
  EditText etPassword;
  EditText etConfirmPassword;
  LinearLayout linlayConfirmPassword;
  ImageView ivLogo;
  boolean isSigningUp = true;
  RelativeLayout rellayBackground;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setTitle("Twitter Mini");

    etUsername = findViewById(R.id.edttxtUsername);
    etPassword = findViewById(R.id.edttxtPassword);
    etConfirmPassword = findViewById(R.id.edttxtConfirmPassword);
    btnLogin = findViewById(R.id.btnLogin);
    tvSignupOrLogin = findViewById(R.id.txtvwSignupOrLogin);
    linlayConfirmPassword = findViewById(R.id.linlayConfirmPassword);
    ivLogo = findViewById(R.id.imgvwLogo);
    rellayBackground = findViewById(R.id.rellayBackground);
    //clicking on backgroung will hide the keyboard
    rellayBackground.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
      }
    });
    //clicking enter will call signup/login function
    etPassword.setOnKeyListener((View.OnKeyListener) this);
    etConfirmPassword.setOnKeyListener((View.OnKeyListener) this);

    //if logged in, redirect to list page
    if (ParseUser.getCurrentUser() != null) {
      showUserList();
    }

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

  public void toggleLoginSignup(View view) {
    if (isSigningUp) {
      btnLogin.setText("Signup");
      tvSignupOrLogin.setText("Already registered? Login.");
      linlayConfirmPassword.setVisibility(View.VISIBLE);
      isSigningUp = false;
    } else {
      btnLogin.setText("Login");
      tvSignupOrLogin.setText("Not registered? Signup.");
      linlayConfirmPassword.setVisibility(View.GONE);
      isSigningUp = true;
    }
  }

  public void loginSignup(View view) {

    if (etUsername.getText().toString().matches("") || etPassword.getText().toString().matches("")) {
      Toast.makeText(this, "Please provide Username and Password.", Toast.LENGTH_SHORT).show();
      return;
    } else {
      if (btnLogin.getText().toString().toLowerCase().equals("signup")) {
        if (!(etPassword.getText().toString().equals(etConfirmPassword.getText().toString()))) {
          Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
        } else {
          //Signup
          ParseUser user = new ParseUser();
          user.setUsername(etUsername.getText().toString());
          user.setPassword(etPassword.getText().toString());

          user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
              if (e == null) {
                showUserList();
                Log.i("SignUp", "Successful!");
              } else {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
              }
            }
          });
        }
      } else {
        //Log in
        ParseUser.logInInBackground(etUsername.getText().toString(), etPassword.getText().toString(), new LogInCallback() {
          @Override
          public void done(ParseUser user, ParseException e) {
            if (user != null) {
              showUserList();
              Log.i("Login: ", "Successful!");
            } else {
              Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    }

  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
      loginSignup(v);
    }

    return false;
  }


  public void showUserList() {
    Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
    startActivity(intent);
  }

}