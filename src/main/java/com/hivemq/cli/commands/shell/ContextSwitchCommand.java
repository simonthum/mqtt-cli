/*
 * Copyright 2019-present HiveMQ and the HiveMQ Community
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
package com.hivemq.cli.commands.shell;

import com.google.common.base.Throwables;
import com.hivemq.cli.commands.Context;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "switch",
        description = "Switch the current context")
public class ContextSwitchCommand extends ShellContextCommand implements Runnable, Context {

    //needed for pico cli - reflection code generation
    public ContextSwitchCommand() {
        this(null);
    }

    @Inject
    public ContextSwitchCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        super(mqttClientExecutor);
    }

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Parameters(index = "0", arity = "0..1", description = "The name of the context, e.g. client@localhost")
    @Nullable
    private String contextName;

    @CommandLine.Option(names = {"-i", "--identifier"}, description = "The client identifier UTF-8 String (default randomly generated string)")
    @Nullable
    private String identifier;

    @CommandLine.Option(names = {"-h", "--host"}, defaultValue = "localhost", description = "The hostname of the message broker (default 'localhost')")
    @Nullable
    private String host;


    @Override
    public void run() {

        if (contextName == null && identifier == null) {
            ShellCommand.usage(this);
            return;
        }

        if (contextName != null) {
            try {
                extractKeyFromContextName(contextName);
            }
            catch (final IllegalArgumentException ex) {
                Logger.error(ex, Throwables.getRootCause(ex).getMessage());
                return;
            }
        }

        Logger.trace("Command {} ", this);

        final MqttClient client = mqttClientExecutor.getMqttClient(this);

        if (client != null) {
            updateContext(client);
        } else {
            Logger.error("Context {}@{} not found", identifier, host);
        }
    }

    private void extractKeyFromContextName(final String contextName) {
        final String[] context = contextName.split("@");

        if (context.length == 1) {
            identifier = context[0];
        } else if (context.length == 2) {
            identifier = context[0];
            host = context[1];
        } else {
            throw new IllegalArgumentException("Context name is not valid: " + contextName);
        }
    }

    @Override
    public String getKey() {
        return "client {" +
                "identifier='" + getIdentifier() + '\'' +
                ", host='" + getHost() + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "identifier=" + identifier +
                ", host=" + host +
                '}';
    }

    @NotNull
    public String getHost() {
        return host;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }
}
