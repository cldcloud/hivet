package com.acukanov.hivet.ui.start;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.acukanov.hivet.R;
import com.acukanov.hivet.data.database.model.Messages;
import com.acukanov.hivet.data.database.model.Users;
import com.acukanov.hivet.data.preference.UserPreferenceManager;
import com.acukanov.hivet.events.GpsStateChanged;
import com.acukanov.hivet.injection.annotations.ActivityContext;
import com.acukanov.hivet.ui.base.BaseActivity;
import com.acukanov.hivet.ui.main.MainActivity;
import com.acukanov.hivet.utils.DialogFactory;
import com.acukanov.hivet.utils.GpsUtils;
import com.acukanov.hivet.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class StartActivity extends BaseActivity implements IStartView, View.OnClickListener {
    private static final String LOG_TAG = LogUtils.makeLogTag(StartActivity.class);
    private static final int REQUEST_PERMISSION_FILE_LOCATION = 0;
    @Inject StartPresenter mStartPresenter;
    @Inject UserPreferenceManager mPreferenceManager;
    @InjectView(R.id.text_enable_gps) TextView mGpsAlertMessage;
    @InjectView(R.id.et_user_name) EditText mUserName;
    @InjectView(R.id.btn_login) Button mLoginButton;
    private Users mUsers;
    private Messages mMessages;

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, StartActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);
        ButterKnife.inject(this);
        mStartPresenter.attachView(this);
        EventBus.getDefault().register(this);

        mUsers = new Users();
        mMessages = new Messages();
        if (mPreferenceManager.getLoggedInUserId() != 0) {
            MainActivity.startActivity(this, mPreferenceManager.getLoggedInUserId(), "");
        }
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStartPresenter.detachView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GpsStateChanged event) {
        if (!GpsUtils.isGpsEnabled(this)) {
            mGpsAlertMessage.setVisibility(View.VISIBLE);
        } else {
            mGpsAlertMessage.setVisibility(View.GONE);
        }
    }

    @Override
    @OnClick({R.id.btn_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (!mUserName.getText().toString().equals("")) {
                    if (GpsUtils.isGpsEnabled(this)) {
                        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            mUsers.setUserName(mUserName.getText().toString());
                            mStartPresenter.createUser(mUsers);
                        } else {
                            requestPermissionsSafely(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSION_FILE_LOCATION);
                        }
                    } else {
                        DialogFactory.createSimpleOkErrorDialog(this,
                                R.string.text_enable_gps).show();
                    }
                } else {
                    DialogFactory.createSimpleOkErrorDialog(this,
                            R.string.text_empty_user_name).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_FILE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mUsers.setUserName(mUserName.getText().toString());
                    mStartPresenter.createUser(mUsers);
                    /*mStartPresenter.openMainActivity(this);*/
                } else {
                    DialogFactory.createSimpleOkErrorDialog(this,
                            R.string.title_permission_file_location,
                            R.string.text_permission_file_location_error).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onOpenMainActivity(@ActivityContext Context context) {
        Location currentLocation = GpsUtils.getLastKnownLocationIntenetProvider(context);
        String loc = currentLocation.getLatitude() + " " + currentLocation.getLongitude();
        LogUtils.error(LOG_TAG, currentLocation.getLatitude() + " " + currentLocation.getLongitude());
        mPreferenceManager.saveLoggedInUserId(mUsers.getId());
        MainActivity.startActivity(this, mUsers.getId(), loc);
    }

    @Override
    public void onNewUserCreated() {
        mStartPresenter.openMainActivity(this);
    }
}
