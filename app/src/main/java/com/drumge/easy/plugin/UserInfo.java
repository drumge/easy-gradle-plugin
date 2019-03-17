package com.drumge.easy.plugin;


import com.drumge.kvo.annotation.KvoBind;
import com.drumge.kvo.annotation.KvoIgnore;
import com.drumge.kvo.annotation.KvoSource;

import java.util.List;

/**
 * Created by chenrenzhan on 2018/4/29.
 */

@KvoSource(check = true)
public class UserInfo {
    public static final String LOGIN = "login";
    public static final String HOME_PAGE = "home_page";


    @KvoIgnore
    public long uid;
    private int index;
    private String name;
    private String age;
    @KvoIgnore
    public String header;

    private String dec;

    private GameInfo gameInfo;

    private List<String> sign;

    private String city;


    public UserInfo(long uid, String name, String age, String header) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.header = header;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setIndex(int index) {
//        Integer nnvv = index;
//        Integer oovv = this.index;
//        Kvo.getInstance().notifyWatcher(this, "index", oovv, nnvv);

        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @KvoBind(name = K_UserInfo.dec)
    public void setDec(String dec) {
        this.dec = dec;
    }

    @KvoBind(name = K_UserInfo.gameInfo)
    public void setGame(GameInfo gameInfo) {
//        Objects.deepEquals(this.gameInfo, gameInfo);
        this.gameInfo = gameInfo;
    }

    public void setSign(List<String> sign) {
        this.sign = sign;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
