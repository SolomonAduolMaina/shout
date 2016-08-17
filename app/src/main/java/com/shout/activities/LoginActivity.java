package com.shout.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.GraphJSONObjectCallback;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.shout.R;
import com.shout.applications.ShoutApplication;
import com.shout.notificationsProvider.NotificationsProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends AccountAuthenticatorActivity {
    private final String LOGIN_PHP_PATH = "http://shouttestserver.ueuo.com/login.php";
    private final String ACCOUNT_TYPE = "http://shouttestserver.ueuo.com";
    private final String SEND_TOKEN_PHP_PATH = "http://shouttestserver.ueuo.com/send_message.php";
    private final AccountAuthenticatorActivity THIS_INSTANCE = this;
    private CallbackManager callbackManager;
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountManager = AccountManager.get(this);

        callbackManager = CallbackManager.Factory.create();
        FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphJSONObjectCallback loginCallback = new GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("login_type", "Facebook");
                            jsonObject.put("user_name", object.getString("name"));
                            jsonObject.put("password", JSONObject.NULL);
                            jsonObject.put("email_address", JSONObject.NULL);
                            jsonObject.put("facebook_id", object.getString("id"));
                            jsonObject.put("new_user", JSONObject.NULL);
                            JSONObject friendsObject = object.getJSONObject("friends");
                            jsonObject.put("facebook_friends", friendsObject.getJSONArray("data"));
                            Intent intent = new Intent(THIS_INSTANCE, ShoutActivity.class);
                            Pair<Intent, JSONObject> pair = new Pair<>(intent, jsonObject);

                            new LoginTask().execute(new Pair<>(LOGIN_PHP_PATH, pair));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };

                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                GraphRequest request = new GraphRequest().newMeRequest(accessToken, loginCallback);
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, friends");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        };

        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback);

        setContentView(R.layout.login_activity);

        final EditText emailEditText = (EditText) findViewById(R.id.email_editText);
        final EditText usernameEditText = (EditText) findViewById(R.id.username_editText);
        final EditText passwordEditText = (EditText) findViewById(R.id.password_editText);
        final EditText email2EditText = (EditText) findViewById(R.id.email2_editText);
        final EditText password2EditText = (EditText) findViewById(R.id.password2_editText);
        final Button signUpButton = (Button) findViewById(R.id.signUp_button);
        Button signInButton = (Button) findViewById(R.id.signIn_button);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try {
                    boolean isSignUp = view == signUpButton;
                    jsonObject.put("new_user", isSignUp ? "Yes" : "No");
                    jsonObject.put("user_name", isSignUp ? usernameEditText.getText().toString()
                            : JSONObject.NULL);
                    String password = isSignUp ? passwordEditText.getText().toString() :
                            password2EditText.getText().toString();
                    jsonObject.put("password", password);
                    String emailAddress = isSignUp ? emailEditText.getText().toString() :
                            email2EditText.getText().toString();
                    jsonObject.put("email_address", emailAddress);
                    jsonObject.put("facebook_id", JSONObject.NULL);
                    jsonObject.put("facebook_friends", JSONObject.NULL);
                    jsonObject.put("login_type", "Shout");
                    Intent intent = new Intent(THIS_INSTANCE, ShoutActivity.class);
                    Pair<Intent, JSONObject> pair = new Pair<>(intent, jsonObject);
                    new LoginTask().execute(new Pair<>(LOGIN_PHP_PATH, pair));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        signUpButton.setOnClickListener(onClickListener);
        signInButton.setOnClickListener(onClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class LoginTask extends ShoutApplication.SendAndReceiveJSON<Intent> {
        @Override
        protected Pair<Intent, JSONObject> doInBackground(Pair<String, Pair<Intent, JSONObject>>...
                                                                  args) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("registrationId", FirebaseInstanceId.getInstance().getToken());
                Pair<String, JSONObject> pair = new Pair<>(SEND_TOKEN_PHP_PATH, jsonObject);
                JSONObject serverResponse = ShoutApplication.getJsonResponse(pair);
                Log.v("MyTag", serverResponse.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return super.doInBackground(args);
        }


        @Override
        protected void onPostExecute(Pair<Intent, JSONObject> pair) {
            JSONObject response = pair.second;
            try {
                if (response.getString("insert").equals("Success!")) {
                    JSONObject token = response.getJSONObject("token");
                    String userName = token.getString("user_name");
                    String password = token.getString("password");
                    Account account = new Account(userName, ACCOUNT_TYPE);
                    accountManager.addAccountExplicitly(account, password, null);
                    accountManager.setAuthToken(account, "InsertRow", token.toString());
                    accountManager.setPassword(account, password);

                    Bundle bundle = new Bundle();
                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                    bundle.putString("userId", token.getString("user_id"));
                    ContentResolver.requestSync(account, NotificationsProvider.AUTHORITY, bundle);

                    Intent result = new Intent();
                    result.putExtra(AccountManager.KEY_ACCOUNT_NAME, userName);
                    result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
                    result.putExtra(AccountManager.KEY_AUTHTOKEN, token.toString());
                    setAccountAuthenticatorResult(result.getExtras());
                    setResult(RESULT_OK, result);

                    JSONArray jsonArray = response.getJSONArray("friends");
                    ArrayList<String> friendIds = new ArrayList<>();
                    ArrayList<String> friendNames = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject friend = jsonArray.getJSONObject(i);
                        friendIds.add(friend.getString("friend_id"));
                        friendNames.add(friend.getString("user_name"));
                    }

                    Intent intent = pair.first;
                    intent.putExtra("userId", token.getString("user_id"));
                    intent.putStringArrayListExtra("friendIds", friendIds);
                    intent.putStringArrayListExtra("friendNames", friendNames);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getBaseContext(), "Success!", Toast.LENGTH_LONG).show();

                } else {
                    String error_message = response.getString("error_message");
                    Toast.makeText(getBaseContext(), error_message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}