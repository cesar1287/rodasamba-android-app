package com.ribeiro.cardoso.rodasamba;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ribeiro.cardoso.rodasamba.util.DownloadImageTask;


public class EventPictureFragment extends Fragment {

    private String mPictureUrl;

    public static EventPictureFragment newInstance(String picutreUrl) {
        EventPictureFragment fragment = new EventPictureFragment();

        fragment.mPictureUrl = picutreUrl;

        return fragment;
    }
    public EventPictureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_picture, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DownloadImageTask eventImageDownloader = new DownloadImageTask((ImageView)this.getView().findViewById(R.id.event_picture_image), this.mPictureUrl);
        eventImageDownloader.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
