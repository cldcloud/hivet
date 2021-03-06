package com.acukanov.hivet.ui.chat;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.acukanov.hivet.R;
import com.acukanov.hivet.data.database.model.Messages;
import com.acukanov.hivet.events.ChatMessageSent;
import com.acukanov.hivet.service.BotMessageService;
import com.acukanov.hivet.ui.base.BaseActivity;
import com.acukanov.hivet.ui.base.BaseFragment;
import com.acukanov.hivet.utils.DateUtils;
import com.acukanov.hivet.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChatFragment extends BaseFragment implements IChatView, View.OnClickListener {
    private static final String LOG_TAG = LogUtils.makeLogTag(ChatFragment.class);
    private static final String EXTRA_USER_ID = "extra_user_id";
    private static final String EXTRA_USER_LOCATION = "extra_user_location";
    private Activity mActivity;
    private ArrayList<Messages> mMessageList;
    private Messages mMessage;
    private long mUserId;
    @Inject ChatPresenter mChatPresenter;
    @InjectView(R.id.list_chat) RecyclerView mChatList;
    @InjectView(R.id.text_empty_chat) TextView mEmptyChatMessage;
    @InjectView(R.id.text_message_field) EditText mMessageField;
    @InjectView(R.id.btn_send_message) ImageButton mSendMessageButton;
    @InjectView(R.id.progress_chat) ProgressBar mProgressChat;
    private ChatAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private boolean mNeedScroll;
    private String mUserLocation;

    public ChatFragment() {

    }

    public static ChatFragment newInstance(long userId, String userLocation) {
        ChatFragment instance = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_USER_ID, userId);
        args.putString(EXTRA_USER_LOCATION, userLocation);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) mActivity).activityComponent().inject(this);;
        mMessageList = new ArrayList<>();
        Bundle args = getArguments();
        if (args != null) {
            mUserId = args.getLong(EXTRA_USER_ID);
            mUserLocation = args.getString(EXTRA_USER_LOCATION);
        }
        mMessage = new Messages();
        mAdapter = new ChatAdapter(mActivity, mUserId);
        mLayoutManager = new LinearLayoutManager(mActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.inject(this, rootView);
        mChatPresenter.attachView(this);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(mLayoutManager);
        mChatList.setAdapter(mAdapter);

        if (!mUserLocation.equals("")) {
            mChatPresenter.sendLocationMassage(mUserId, mUserLocation);
        }

        mChatPresenter.loadMessages();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent botMessageService = BotMessageService.getStartIntent(mActivity);
        if (BotMessageService.isServiceRunning(mActivity, BotMessageService.class)) {
            mActivity.stopService(BotMessageService.getStartIntent(mActivity));
        }
        mActivity.startService(botMessageService);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mChatPresenter.detachView();
    }

    @Override
    @OnClick({R.id.btn_send_message})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:
                if (!mMessageField.getText().equals("")) {
                    mMessage.message = mMessageField.getText().toString();
                    mMessage.dateTime = DateUtils.getDateTime();
                    mMessage.userId = mUserId;
                    mMessageField.setText("");
                    mChatPresenter.createMessage(mMessage);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ChatMessageSent event) {
        mChatPresenter.addMessage(event.getId());
    }

    @Override
    public void onMessagesLoaded(ArrayList<Messages> messagesList) {
        mMessageList.clear();
        mMessageList.addAll(messagesList);
        mAdapter.setMessages(mMessageList);
        mAdapter.notifyDataSetChanged();
        mChatList.scrollToPosition(mMessageList.size() - 1);
        mEmptyChatMessage.setVisibility(View.GONE);
    }

    @Override
    public void onMessageAdded(Messages message) {
        int position = mLayoutManager.findLastVisibleItemPosition();
        if (position == (mMessageList.size() - 1)) {
            mNeedScroll = true;
        } else {
            mNeedScroll = false;
        }
        mMessageList.add(message);
        mAdapter.setMessages(mMessageList);
        mAdapter.notifyDataSetChanged();
        if (mNeedScroll) {
            mChatList.smoothScrollToPosition(mMessageList.size());
        }
    }

    @Override
    public void showProgress(boolean show) {
        if (show) {
            mProgressChat.setVisibility(View.VISIBLE);
        } else {
            mProgressChat.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyMessage() {
        mEmptyChatMessage.setVisibility(View.VISIBLE);
    }
}
