package com.mars.infra.router.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by JohnnySwordMan on 2022/1/10
 */
class MarsRouterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        println("MarsRouterPlugin apply >>> 😄")
    }
}