package com.byteshaft.shout;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Player extends Fragment implements View.OnClickListener {

//    private View mBaseView;
    public Button mPlaybackButton;
    private static Player sInstance;
    private boolean mFreshRun = true;
    public ProgressBar mProgressBar;
    private ObjectAnimator animation;
    private Button shareButton;
    public TextView textView;

    private void setInstance(Player activity) {
        sInstance = activity;
    }

    static Player getInstance() {
        return sInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout, R.layout.player);
        return frameLayout;
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup, int layout) {
        viewGroup.removeAllViewsInLayout();
        View mBaseView = inflater.inflate(layout, viewGroup);
        setInstance(this);
        mProgressBar = (ProgressBar) mBaseView.findViewById(R.id.progress_bar);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mProgressBar.setProgressDrawable(getResources().getDrawable
                    (R.drawable.progress_bar_less_than_lollipop));
        } else {
            mProgressBar.setProgressDrawable(getResources().getDrawable
                    (R.drawable.progress_bar_for_greater_than_lollipop));

        }
        shareButton = (Button) mBaseView.findViewById(R.id.share_button);
        shareButton.setOnClickListener(this);
        mPlaybackButton = (Button) mBaseView.findViewById(R.id.button_toggle_playback);
        textView = (TextView) mBaseView.findViewById(R.id.view);
        mPlaybackButton.setOnClickListener(this);

        Intent intent = new Intent(getActivity().getApplicationContext(), StreamService.class);
        intent.putExtra(AppGlobals.READY_STREAM, true);
        if (getService() == null) {
            getActivity().startService(intent);
        }
        if (AppGlobals.getSongStatus()) {
            mPlaybackButton.setBackgroundResource(R.drawable.pause);
        } else {
            mPlaybackButton.setBackgroundResource(R.drawable.play_image);
        }
        if (AppGlobals.getSongStatus()) {
            textView.setText("Playing");
        } else {
            textView.setText("Stopped");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void updateProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        animation = ObjectAnimator.ofInt(mProgressBar, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration(5000); //in milliseconds
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    public void stopProgressBar() {
        mProgressBar.clearAnimation();
        mProgressBar.animate().cancel();
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_toggle_playback:
                if (animation != null && animation.isRunning()) {
                    stopProgressBar();
                }
                togglePlayPause();
                break;
            case R.id.share_button:
                share();
                break;
        }
    }

    private void share(){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Hey Listen to the program on 8CCC now by going to http://8ccc.com.au/live";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private StreamService getService() {
        return StreamService.getInstance();
    }

    public void togglePlayPause() {
        if (!AppGlobals.getSongStatus()) {
            if (mFreshRun) {
                mFreshRun = false;
            }
            if (getService() == null) {
                Intent intent = new Intent(AppGlobals.getContext(), StreamService.class);
                intent.putExtra(AppGlobals.READY_STREAM, false);
            }
            getService().playStream();
        } else {
            if (getService() != null) {
                getService().stopStream();
                AppGlobals.setSongPlaying(false);
                Helpers.updateMainViewButton();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("TAG", "landscape");
            ((AppCompatActivity)getActivity()).getSupportActionBar().invalidateOptionsMenu();
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            populateViewForOrientation(inflater, (ViewGroup) getView(), R.layout.player_land);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("TAG", "portrait");
//            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            populateViewForOrientation(inflater, (ViewGroup) getView(), R.layout.player);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager winMan = (WindowManager)getActivity().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        if (winMan != null) {
            int orientation = winMan.getDefaultDisplay().getOrientation();

            if (orientation == 0) {
                // Portrait
//                ((AppCompatActivity)getActivity()).getSupportActionBar().show();
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                populateViewForOrientation(inflater, (ViewGroup) getView(), R.layout.player);

            }
            else if (orientation == 1) {
                // Landscape
//                ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                populateViewForOrientation(inflater, (ViewGroup) getView(), R.layout.player_land);
            }
        }
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            stopProgressBar();
        }
        if (AppGlobals.getSongStatus()) {
            mPlaybackButton.setBackgroundResource(R.drawable.pause);
        } else {
            mPlaybackButton.setBackgroundResource(R.drawable.play_image);
        }
    }
}
