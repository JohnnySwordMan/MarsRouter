package com.mars.infra.router

import android.app.Application
import com.mars.infra.router.runtime.Router

/**
 * Created by JohnnySwordMan on 2022/1/7
 */
class RouterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Router.init(this)
    }
}