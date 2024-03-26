/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.futurexskills.custom.processors.DecryptionBinaryFiles;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Tags({"flowFile"})
@CapabilityDescription("Процессор для расшифровки бинарных файлов.")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class DecryptionBinaryFiles extends AbstractProcessor {

    public static final PropertyDescriptor Bytes_childPath = new PropertyDescriptor
            .Builder().name("Bytes_childPath")
            .displayName("Укажите путь к файлу")
            .description("Укажите путь к файлу. \n" +
                    "Пример:\n" +
                    "/app/nifi-ref/bin-1-a/Map_kksid-pointid_binfile_7_a_bin")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship Success = new Relationship.Builder()
            .name("Success")
            .build();

    public static final Relationship Failure = new Relationship.Builder()
            .name("Failure")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        descriptors = new ArrayList<>();
        descriptors.add(Bytes_childPath);
        descriptors = Collections.unmodifiableList(descriptors);

        relationships = new HashSet<>();
        relationships.add(Success);
        relationships.add(Failure);
        relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {

        final Decryption DEP = new Decryption();
        DEP.setBinarPath(context.getProperty(Bytes_childPath.getName() ).getValue());


        FlowFile flowFile = session.get();

        if ( flowFile == null ) {
            return;
        }

        try {
            byte[] bytes_chid = Files.readAllBytes(Paths.get(DEP.getBinarPath()));
            DEP.setBytesChid(bytes_chid);

        } catch (IOException e) {
            session.transfer(flowFile, Failure);
        }

        session.write(flowFile, DEP);
        session.transfer(flowFile,Success);

        var newFlowFile = session.create();

        newFlowFile = session.write(newFlowFile, new OutputStreamCallback() {
            @Override
            public void process(final OutputStream outputStream) throws IOException {
                byte[] binaryContent = new byte[18];
                outputStream.write(binaryContent);
            }
        });

        session.transfer(newFlowFile,Success);
    }
}
