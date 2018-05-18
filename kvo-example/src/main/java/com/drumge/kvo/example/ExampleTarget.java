package com.drumge.kvo.example;

import com.drumge.kvo.api.KvoEvent;
import com.drumge.kvo.annotation.KvoWatch;

/**
 * Created by chenrenzhan on 2018/5/3.
 */

public class ExampleTarget {

    public ExampleTarget() {
//        ExampleSource source;
//        Kvo.getInstance().bind(this, source);
//        Kvo.getInstance().bind(this, source, "tag1");

    }

    @KvoWatch(name = K_ExampleSource.example, tag = "tag1")
    public void onUpdateExample(KvoEvent<ExampleSource, String> event) {

    }

//    @KvoWatch(name = K_ExampleSource.index)
//    public void onUpdateIndex(KvoEvent<ExampleSource, Integer> event) {
//
//    }
}
