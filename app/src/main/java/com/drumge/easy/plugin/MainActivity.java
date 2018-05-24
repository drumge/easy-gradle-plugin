package com.drumge.easy.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.drumge.kvo.annotation.KvoWatch;
import com.drumge.kvo.api.Kvo;
import com.drumge.kvo.api.KvoEvent;


public class MainActivity extends Activity {

    private String oldName;
    UserInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfo = new UserInfo(222112L, "name", "age", "header");
        Kvo.getInstance().bind(this, mInfo);

    }

    @KvoWatch(name = K_UserInfo.name)
//    @KvoWatch(name = "name", thread = KvoWatch.Thread.MAIN)
    public void onUpdateUserName(KvoEvent<UserInfo, String> event) {
        Log.d("chenrenzhan", "oldName = " + oldName + " , newName = " + event.getNewValue());
    }

    @KvoWatch(name = K_GameInfo.bgUrl, tag = "game1")
    public void onUpdateGameId(KvoEvent<GameInfo, String> event) {

    }
}
