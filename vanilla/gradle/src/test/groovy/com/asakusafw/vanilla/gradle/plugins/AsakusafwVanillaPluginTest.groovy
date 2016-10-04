/*
 * Copyright 2011-2016 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.vanilla.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.vanilla.gradle.plugins.internal.AsakusaVanillaOrganizerPlugin
import com.asakusafw.vanilla.gradle.plugins.internal.AsakusaVanillaSdkPlugin

/**
 * Test for {@link AsakusafwVanillaPlugin}.
 */
class AsakusafwVanillaPluginTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.apply plugin: 'asakusafw-vanilla'
            return stmt
        }
    }

    Project project

    /**
     * activates {@code 'asakusafw-sdk'} plug-in.
     */
    @Test
    public void activate_asakusafw_sdk() {
        assert project.plugins.hasPlugin(AsakusaVanillaSdkPlugin) == false
        assert project.plugins.hasPlugin(AsakusaVanillaOrganizerPlugin) == false

        project.apply plugin: 'asakusafw-sdk'
        assert project.plugins.hasPlugin(AsakusaVanillaSdkPlugin)
        assert project.plugins.hasPlugin(AsakusaVanillaOrganizerPlugin) == false
    }

    /**
     * activates {@code 'asakusafw-organizer'} plug-in.
     */
    @Test
    public void activate_asakusafw_organizer() {
        assert project.plugins.hasPlugin(AsakusaVanillaSdkPlugin) == false
        assert project.plugins.hasPlugin(AsakusaVanillaOrganizerPlugin) == false

        project.apply plugin: 'asakusafw-organizer'
        assert project.plugins.hasPlugin(AsakusaVanillaSdkPlugin) == false
        assert project.plugins.hasPlugin(AsakusaVanillaOrganizerPlugin)
    }
}
