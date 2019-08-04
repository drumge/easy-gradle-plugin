package com.drumge.easy.plugin.extend

import com.drumge.easy.plugin.api.IEasyExtend
import com.drumge.easy.plugin.api.IEasyTransform
import com.drumge.easy.plugin.api.IExtend
import com.drumge.easy.plugin.api.IPlugin

class EasyExtend<T extends IEasyTransform, P extends IPlugin, E extends IExtend> implements IEasyExtend  {
    String name
    T transform
    P plugin
    E extend

    EasyExtend(String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }
}