package com.ribeiro.cardoso.rodasamba;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;


public class AboutFragment extends Fragment {

    private ViewHolder mHolder;

    private static String trackedFragmentName = "Sobre";

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        SplashScreenActivity.tracker.setScreenName(AboutFragment.trackedFragmentName);
        SplashScreenActivity.tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        this.mHolder = new ViewHolder(rootView);

        EventPublishListener publishListener = new EventPublishListener();

        this.mHolder.mPublishEvent.setOnClickListener(publishListener);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private static class ViewHolder {
        public Button mPublishEvent;

        public ViewHolder(View view) {
            this.mPublishEvent = (Button)view.findViewById(R.id.event_publish);
        }
    }

    private class EventPublishListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "pedro.vieira@venturafilmes.com.br", null));
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inclusão de roda no app Na Roda do Samba");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Olá! Estava navegando pelo aplicativo Na Roda do Samba e gostaria de incluir uma roda de samba no app...");
//            startActivity(Intent.createChooser(emailIntent, "Enviar e-mail"));

            String url = "https://m.facebook.com/?_rdr#!/profile.php?id=1393268073&tsid=0.4919371942376319";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            AboutFragment.this.startActivity(i);
        }
    }
}
