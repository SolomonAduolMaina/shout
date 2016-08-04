package com.shout.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.shout.R;
/**
 * In charge of the Sign up process. Since it's not an AuthenticatorActivity decendent,
 * it returns the result back to the calling activity, which is an AuthenticatorActivity,
 * and it return the result back to the Authenticator
 * <p/>
 * User: udinic
 */
public class SignUpActivity extends Activity {

    private String mAccountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountType = getIntent().getStringExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE);

        setContentView(R.layout.act_register);

        findViewById(R.id.alreadyMember).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void createAccount() {

        // Validation!

        new AsyncTask<String, Void, Intent>() {

            String accountName = ((TextView) findViewById(R.id.accountName)).getText().toString()
                    .trim();
            String accountPassword = ((TextView) findViewById(R.id.accountPassword)).getText()
                    .toString().trim();

            @Override
            protected Intent doInBackground(String... params) {

                String token;
                Bundle data = new Bundle();
                try {
                    token = "example token";

                    data.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, token);
                    data.putString(AuthenticatorActivity.PARAM_USER_PASS, accountPassword);
                } catch (Exception e) {
                    data.putString(AuthenticatorActivity.KEY_ERROR_MESSAGE, e.getMessage());
                }

                final Intent res = new Intent();
                res.putExtras(data);
                return res;
            }

            @Override
            protected void onPostExecute(Intent intent) {
                if (intent.hasExtra(AuthenticatorActivity.KEY_ERROR_MESSAGE)) {
                    Toast.makeText(getBaseContext(), intent.getStringExtra(AuthenticatorActivity
                            .KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                } else {
                    setResult(RESULT_OK, intent);
                    finish();
                    startActivity(new Intent(getBaseContext(), AuthenticatorActivity.class));
                }
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
