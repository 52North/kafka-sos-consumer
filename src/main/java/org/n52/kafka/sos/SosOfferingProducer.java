/**
 * Copyright 2017-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.kafka.sos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.n52.kafka.sos.model.MeasurementObservation;
import org.n52.kafka.sos.model.Offering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SosOfferingProducer {
    
    private static final Logger LOG = LoggerFactory.getLogger(SosOfferingProducer.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    
    private final Offering offering;
    private final String bootstrapServers;
    private Producer<Integer, String> producer;
    private int valueCounter;
    private String topicName;
    
    public SosOfferingProducer(Offering off, String bootstrapServers) {
        this.offering = off;
        this.bootstrapServers = bootstrapServers;
    }
    
    public void initialize() throws IOException {
        Properties props = new Properties();
        props.put("bootstrap.servers", this.bootstrapServers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", IntegerSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        
        this.producer = new KafkaProducer<>(props);
        this.topicName = "sos.offerings."+this.offering.getId();
        this.valueCounter = 0;
        
        Properties adminConfig = new Properties();
        adminConfig.put("bootstrap.servers", this.bootstrapServers);
        
        //TODO make the partition and replication factor configurable (e.g. dependent on the cluster size)
        CreateTopicsResult result = AdminClient.create(adminConfig).createTopics(Collections.singletonList(
                new NewTopic(this.topicName, 1, (short) 1)));
        
        try {
            result.values().entrySet().forEach((entry) -> {
                try {
                    entry.getValue().get();
                    LOG.info("topic {} created", entry.getKey());
                } catch (InterruptedException | ExecutionException e) {
                    if (Throwables.getRootCause(e) instanceof TopicExistsException) {
                        LOG.warn("topic {} existed", entry.getKey());
                    }
                    else {
                        throw new RuntimeException(new IOException("Could not create topic", e));
                    }
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
        
        LOG.info("initialized producer for offering '{}'", getOfferingId());
    }
    
    public String getOfferingId() {
        return Integer.toString(this.offering.getId());
    }
    
    public String getTopicName() {
        return topicName;
    }
    
    void newMeasurement(MeasurementObservation mo) throws JsonProcessingException {
        LOG.debug("New measurement for producer {}", this);
        Future<RecordMetadata> response = this.producer.send(new ProducerRecord<>(this.topicName,
                this.valueCounter++,
                MAPPER.writeValueAsString(mo)));
        
        try {
            RecordMetadata meta = response.get();
            LOG.debug("Topic response: {}", meta.toString());
        }
        catch (InterruptedException | ExecutionException e) {
            LOG.warn("Could not send to topic: {}", e.getMessage());
            LOG.debug(e.getMessage(), e);
        }
    }
    
}
