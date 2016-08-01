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

import java.util.List;

/**
 * Created by diegopc86 on 23/08/14.
 */
public class AgeGroupDialogFragment extends UserRegistrationDialogFragment {

    public AgeGroupDialogFragment() {}

    public static AgeGroupDialogFragment newInstance (Listener listener) {
        AgeGroupDialogFragment f = new AgeGroupDialogFragment();

        f.mListener = listener;

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getAlertDialogBuilder();

        mTitleTextView.setText(getActivity().getString(R.string.age_group_text));

        final Cursor cursor = mSqlClient.query(SambaContract.AgeGroupEntry.TABLE_NAME,
                new String[]{SambaContract.AgeGroupEntry._ID, SambaContract.AgeGroupEntry.COLUMN_NAME},
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
                final int idxName = cursor.getColumnIndex(SambaContract.AgeGroupEntry.COLUMN_NAME);
                final String NAME = cursor.getString(idxName);

                final int idxId = cursor.getColumnIndex(SambaContract.AgeGroupEntry._ID);
                final int ID = cursor.getInt(idxId);

                mListener.onItemSelected(UserRegistrationActivity.AGE_GROUP_KEY, ID, NAME);

            }
        });

        return builder.create();
    }
}
