/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
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
 *
 */
package com.hivemq.cli.mqtt.test.results;

import com.hivemq.cli.utils.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClientIdLengthTestResults {
    private final int maxClientIdLength;
    private List<Tuple<@NotNull Integer, @Nullable String>> testResults;

    public ClientIdLengthTestResults(final int maxClientIdLength, final @NotNull List<Tuple<Integer, String>> testResults) {
        this.maxClientIdLength = maxClientIdLength;
        this.testResults = testResults;
    }

    public int getMaxClientIdLength() {
        return maxClientIdLength;
    }

    public @NotNull List<Tuple<@NotNull Integer, @Nullable String>> getTestResults() {
        return testResults;
    }
}
