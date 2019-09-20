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
package com.hivemq.cli.commands.shell;

import com.hivemq.cli.commands.CliCommand;
import com.hivemq.cli.mqtt.ClientCache;
import com.hivemq.cli.mqtt.ClientData;
import com.hivemq.cli.mqtt.MqttClientExecutor;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientConfig;
import org.jetbrains.annotations.NotNull;
import org.pmw.tinylog.Logger;
import picocli.CommandLine;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.*;

@CommandLine.Command(name = "ls",
        aliases = "list",
        description = "List all connected clients with their respective identifieres"
)

public class ListClientsCommand implements Runnable, CliCommand {

    private final MqttClientExecutor mqttClientExecutor;

    public ListClientsCommand() {
        this(null);
    }

    @Inject
    ListClientsCommand(final @NotNull MqttClientExecutor mqttClientExecutor) {
        this.mqttClientExecutor = mqttClientExecutor;
    }

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-t"}, defaultValue = "false", description = "sort by creation time, newest first")
    private boolean sortByTime;

    @CommandLine.Option(names = {"-U"}, defaultValue = "false", description = "do not sort")
    private boolean doNotSort;

    @CommandLine.Option(names = {"-r", "--reverse"}, defaultValue = "false", description = "reverse order while sorting")
    private boolean reverse;

    @CommandLine.Option(names = {"-l", "--long"}, defaultValue = "false", description = "use a long listing format")
    private boolean longOutput;

    @CommandLine.Option(names = {"-s", "--subscriptions"}, defaultValue = "false", description = "list subscribed topics of clients")
    private boolean listSubscriptions;

    @CommandLine.Option(names = {"-a"}, defaultValue = "false", description = "list disconnected client")
    private boolean includeDisconnectedClients;



    @Override
    public void run() {


        if (isVerbose()) {
            Logger.trace("Command: {}", this);
        }


        final ClientCache<String, MqttClient> cache = mqttClientExecutor.getClientCache();
        final Map<String, ClientData> clientDataMap = mqttClientExecutor.getClientDataMap();
        Set<String> clientKeys = cache.keySet();


        final String[] sortedKeys = getSortedClientKeys();
        final Map<String, String> keyToPretty = mapKeyToPrettyOuput(sortedKeys);

        if (longOutput) {
            System.out.println("total " + sortedKeys.length);

            if (sortedKeys.length == 0) {
                return;
            }


            final String longestIDKey = clientKeys.stream()
                    .max((s1, s2) -> Integer.compare(cache.get(s1).getConfig().getClientIdentifier().get().toString().length(),
                            cache.get(s2).getConfig().getClientIdentifier().get().toString().length()))
                    .get();

            final int longestID = cache.get(longestIDKey).getConfig().getClientIdentifier().get().toString().length();

            final String longestHostKey = clientKeys.stream()
                    .max((s1, s2) -> Integer.compare(cache.get(s1).getConfig().getServerHost().length(), cache.get(s2).getConfig().getServerHost().length()))
                    .get();
            final int longestHost = cache.get(longestHostKey).getConfig().getServerHost().length();

            final String format = new String("%-12s " +
                    "%02d:%02d:%02d " +
                    "%-" + longestID + "s " +
                    "%-" + longestHost + "s " +
                    "%5d " +
                    "%8s " +
                    "%s\n");

            for (final String key : sortedKeys) {

                final MqttClient client = cache.get(key);

                final LocalDateTime dateTime = clientDataMap.get(key).getCreationTime();

                String connectionState = null;
                if (client.getState().isConnected()) {
                    connectionState = "CONNECTED";
                }
                else if (client.getState().isConnectedOrReconnect()) {
                    connectionState = "RECONNECTING";
                }
                else {
                    if (!includeDisconnectedClients) {
                        continue;
                    }
                    connectionState = "DISCONNECTED";
                }

                System.out.printf(format,
                        connectionState,
                        dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(),
                        client.getConfig().getClientIdentifier().get().toString(),
                        client.getConfig().getServerHost(),
                        client.getConfig().getServerPort(),
                        client.getConfig().getMqttVersion(),
                        client.getConfig().getSslConfig().map(ssl -> ssl.getProtocols().get().toString()).orElse("NO_SSL"));

                if (listSubscriptions) {
                    System.out.printf(" -subscribed topics: %s\n", clientDataMap.get(key).getSubscribedTopics());
                }
            }


        } else {
            for (final String key : sortedKeys) {
                if (!includeDisconnectedClients && !cache.get(key).getState().isConnectedOrReconnect()) {
                    continue;
                }
                System.out.println(keyToPretty.get(key));
                if (listSubscriptions) {
                    System.out.printf(" -subscribed topics: %s\n", clientDataMap.get(key).getSubscribedTopics());
                }
            }
        }


    }

    private Map<String, String> mapKeyToPrettyOuput(final @NotNull String[] sortedKeys) {
        final ClientCache<String, MqttClient> cache = mqttClientExecutor.getClientCache();
        final Map<String, String> keyToPretty = new HashMap<>();
        for (int i = 0; i < sortedKeys.length; i++) {
            MqttClient client = cache.get(sortedKeys[i]);
            keyToPretty.put(sortedKeys[i], client.getConfig().getClientIdentifier().get() + "@" + client.getConfig().getServerHost());
        }
        return keyToPretty;
    }

    public String[] getSortedClientKeys() {
        final Set<String> keys = mqttClientExecutor.getClientCache().keySet();
        final Map<String, ClientData> clientDataMap = mqttClientExecutor.getClientDataMap();
        String[] keysArr = keys.toArray(new String[0]);

        if (doNotSort) {
            // do nothing
        }
        else if (sortByTime) {
            Arrays.sort(keysArr, new Comparator<String>() {
                @Override
                public int compare(final String s1, final String s2) {
                    return clientDataMap.get(s1).getCreationTime().compareTo(clientDataMap.get(s2).getCreationTime());
                }
            });
        }
        else {
            Arrays.sort(keys.toArray(new String[0]));
        }
        if (reverse) {
            final String[] reversedKeyArr = new String[keysArr.length];
            for (int i = keysArr.length - 1, j = 0; i >= 0; i--, j++) {
                reversedKeyArr[j] = keysArr[i];
            }
            keysArr = reversedKeyArr;
        }
        return keysArr;
    }


    @Override
    public String toString() {
        return "List:: {" +
                "sortByTime=" + sortByTime +
                ", detailedOutput=" + longOutput +
                '}';
    }

    private String getKey(final MqttClient client) {
        return client.getConfig().getClientIdentifier() + client.getConfig().getServerHost();
    }

    @Override
    public boolean isVerbose() {
        return ShellCommand.isVerbose();
    }

    @Override
    public boolean isDebug() {
        return ShellCommand.isDebug();
    }
}
