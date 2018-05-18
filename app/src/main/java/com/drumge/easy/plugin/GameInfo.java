package com.drumge.easy.plugin;


import com.drumge.kvo.annotation.KvoBind;
import com.drumge.kvo.annotation.KvoIgnore;
import com.drumge.kvo.annotation.KvoSource;

/**
 * Created by chenrenzhan on 2018/4/29.
 */

@KvoSource(check = false)
public class GameInfo {

    @KvoIgnore
    public long gameId;
    private String name;
    private String bgUrl;

    public void setGameId(long gameId) {
//        if (KvoUtils.deepEquals(this.gameId, gameId)) {
//            return;
//        }
        this.gameId = gameId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @KvoBind(name = K_GameInfo.bgUrl)
    public void setUrl(String bgUrl) {
        this.bgUrl = bgUrl;
    }

    public void setBgUrl(String bgUrl) {

        this.bgUrl = bgUrl;
    }
}
