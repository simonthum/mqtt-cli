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
package com.hivemq.cli.converters;

import picocli.CommandLine;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class FileToByteBufferConverter implements CommandLine.ITypeConverter<ByteBuffer> {
    @Override
    public ByteBuffer convert(String value) throws Exception {
        final ByteBufferConverter byteBufferConverter = new ByteBufferConverter();
        final FileConverter fileConverter = new FileConverter();

        final File file = fileConverter.convert(value);
        final String password = new String(Files.readAllBytes(file.toPath()));

        return byteBufferConverter.convert(password);
    }
}
