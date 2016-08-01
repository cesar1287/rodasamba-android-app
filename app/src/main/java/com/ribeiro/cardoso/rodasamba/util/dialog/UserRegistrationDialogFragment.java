package com.ribeiro.cardoso.rodasamba.util.dialog;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ribeiro.cardoso.rodasamba.R;
import com.ribeiro.cardoso.rodasamba.data.SambaDbHelper;

/**
 * Created by diegopc86 on 23/08/14.
 */
public abstract class UserRegistrationDialogFragment extends DialogFragment {
    private static final String LOG_TAG = UserRegistrationDialogFragment.class.getSimpleName();

    protected SQLiteDatabase mSqlClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SambaDbHelper sambaDbHelper = SambaDbHelper.getInstance(getActivity());
        mSqlClient = sambaDbHelper.getReadableDatabase();
    }

    public interface Listener {
        void onItemSelected(String type, Object value, String name);
    };

    protected TextView mTitleTextView;

    protected Listener mListener;

    public UserRegistrationDialogFragment() { }

    protected AlertDialog.Builder getAlertDialogBuilder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View titleView = inflater.inflate(R.layout.user_registration_dialog, null);
        builder.setCustomTitle(titleView);

        mTitleTextView = (TextView) titleView.findViewById(R.id.user_registration_title);

        return  builder;
    }
}