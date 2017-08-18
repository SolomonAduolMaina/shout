package com.shout.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.shout.activities.LoginActivity;
/*
import android.text.TextUtils;
import android.Util.Log;
import com.shout.activities.LoginActivity;
import android.accounts.AccountManager;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import android.content.Intent;*/


public class Authenticator extends AbstractAccountAuthenticator {
    /*public static final String ACCOUNT_TYPE = "http://shouttestserver.ueuo.com";
    public static final String ACCOUNT_NAME = "Shout";
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to a Shout " +
            "Account";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to a Shout Account";
*/
    private final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    private final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    private final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    private final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    
    private final Context context;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String
            authTokenType, String[] requiredFeatures, Bundle options) throws
            NetworkErrorException {
        final Intent intent = new Intent(this.context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_IS_ADDING_NEW_ACCOUNT, true);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                     Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account
            account, String authTokenType, Bundle options) throws NetworkErrorException {

        /*// If the caller requested an authToken type we don't support, then return an error
        if (!authTokenType.equals(AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals
                (AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
*/
        // Extract the username and password from the Account Manager, and ask the server for an
        // appropriate AuthToken.
        final AccountManager accountManager = AccountManager.get(context);

        String authToken = accountManager.peekAuthToken(account, authTokenType);


        if (TextUtils.isEmpty(authToken)) {
            final String password = accountManager.getPassword(account);
            if (password != null) {
                authToken = "Dummy Access Token";
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we need to re-prompt
        // them for their credentials. We do that by creating an intent to display our
        // 
        final Intent intent = new Intent(this.context, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }



    @Override
    public String getAuthTokenLabel(String authTokenType) {
        /*if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";*/
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                    Account account, String s, Bundle bundle) throws
            NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account
            account, String[] strings) throws NetworkErrorException {
        /*final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;*/
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                 String s) {
        return null;
    }
}