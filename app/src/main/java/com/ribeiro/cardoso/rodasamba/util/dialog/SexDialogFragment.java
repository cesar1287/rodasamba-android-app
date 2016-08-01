package com.ribeiro.cardoso.rodasamba.util.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;

import com.ribeiro.cardoso.rodasamba.R;
import com.ribeiro.cardoso.rodasamba.UserRegistrationActivity;
import com.ribeiro.cardoso.rodasamba.data.SambaContract;

/**
 * Created by diegopc86 on 23/08/14.
 */
public class SexDialogFragment extends UserRegistrationDialogFragment {

    private static final String LOG_TAG = SexDialogFragment.class.getSimpleName();

    public SexDialogFragment() {}


    public static SexDialogFragment newInstance (Listener listener) {
        SexDialogFragment f = new SexDialogFragment();

        f.mListener = listener;

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = getAlertDialogBuilder();

        mTitleTextView.setText(getActivity().getString(R.string.sex_text));

        final Cursor cursor = mSqlClient.query(SambaContract.SexEntry.TABLE_NAME,
                new String[]{SambaContract.SexEntry._ID, SambaContract.SexEntry.COLUMN_KEY, SambaContract.SexEntry.COLUMN_NAME},
                null,
                null,
                null,
                null,
                null);

        String[] fromColumn = new String[]{
                SambaContract.SexEntry.COLUMN_NAME
        };

        int[] toView = new int[]{
                R.id.user_registration_item_name
        };

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.user_registration_dialog_item, cursor, fromColumn, toView, 0);

        builder.setAdapter(cursorAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cursor.moveToPosition(i);
                final int idxName = cursor.getColumnIndex(SambaContract.SexEntry.COLUMN_NAME);
                final String NAME = cursor.getString(idxName);

                final int idxKey = cursor.getColumnIndex(SambaContract.SexEntry.COLUMN_KEY);
                final String KEY = cursor.getString(idxKey);

                mListener.onItemSelected(UserRegistrationActivity.SEX_KEY, KEY, NAME);

            }
        });

        return builder.create();
    }
}
