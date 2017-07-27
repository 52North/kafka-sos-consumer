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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.n52.kafka.sos.model.MeasurementObservation;
import org.n52.kafka.sos.model.Offering;
import org.n52.kafka.sos.model.Value;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class KafkaSosConsumer implements Runnable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(KafkaSosConsumer.class.getName());

    private final KafkaConsumer<String, String> consumer;
    private final List<String> tables;
    private final int id;

    private static final String SOS_NAME = "sos";
    private static final String SOS_DB_SCHEMA = "public";

    private static final String OBSERVATION_TABLE = SOS_DB_SCHEMA + ".observation";
    private static final String SERIES_TABLE = SOS_DB_SCHEMA + ".series";
    private static final String PROCEDURE_TABLE = SOS_DB_SCHEMA + ".procedure";
    private static final String OBSERVABLEPROPERTY_TABLE = SOS_DB_SCHEMA + ".observableproperty";
    private static final String OFFERING_TABLE = SOS_DB_SCHEMA + ".offering";
    private static final String UNIT_TABLE = SOS_DB_SCHEMA + ".unit";
    private static final String FEATUREOFINTEREST_TABLE = SOS_DB_SCHEMA + ".featureofinterest";
    private static final String NUMERICVALUE_TABLE = SOS_DB_SCHEMA + ".numericvalue";

    private final MetadataCache cache = new MetadataCache();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final String bootstrapServers;
    private final String kafkaConnectRestBaseUrl;
    private final Properties kafkaConnectSettings;
    
    private final Map<Integer, SosOfferingProducer> producers = new HashMap<>();


    public KafkaSosConsumer(int id, String groupId, String bootstrapServers, String kafkaConnectRestBaseUrl,
            Properties kafkaConnectSettings) {
        this.id = id;
        this.tables = Arrays.asList(OBSERVATION_TABLE,
                SERIES_TABLE,
                PROCEDURE_TABLE,
                OFFERING_TABLE,
                OBSERVABLEPROPERTY_TABLE,
                UNIT_TABLE,
                FEATUREOFINTEREST_TABLE,
                NUMERICVALUE_TABLE);

        this.bootstrapServers = bootstrapServers;
        this.kafkaConnectRestBaseUrl = kafkaConnectRestBaseUrl;
        this.kafkaConnectSettings = kafkaConnectSettings;
        
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", this.bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer<>(props);
        

        List<String> topics = this.tables.stream().map(t -> SOS_NAME + "." + t).collect(Collectors.toList());
        consumer.subscribe(topics);
        LOG.info("Subscribed to the topics: {}", topics);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
            initializeDebeziumConnector();
        } catch (IOException ex) {
            LOG.error("Could not initialize debezium connector: " + ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
            return;
        } catch (InterruptedException ex) {
            LOG.warn(ex.getMessage());
        }

        try {
            while (true) {
                /*
                * listen for updates from topics forever
                */
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Map<String, Object> data = new HashMap<>();

                    try {
                        JsonNode valueJson = mapper.readTree(record.value());
                        JsonNode payload = valueJson.path("payload");
                        if (payload.isMissingNode()) {
                            return;
                        }
                        JsonNode after = payload.path("after");
                        if (after.isMissingNode()) {
                            return;
                        }
                        switch (record.topic()) {
                            case SOS_NAME + "." + OBSERVATION_TABLE:
                                cache.newObservation(after);
                                break;
                            case SOS_NAME + "." + SERIES_TABLE:
                                cache.newSeries(after);
                                break;
                            case SOS_NAME + "." + PROCEDURE_TABLE:
                                cache.newProcedure(after);
                                break;
                            case SOS_NAME + "." + OFFERING_TABLE:
                                initializeProducer(cache.newOffering(after));
                                break;
                            case SOS_NAME + "." + OBSERVABLEPROPERTY_TABLE:
                                cache.newObservableProperty(after);
                                break;
                            case SOS_NAME + "." + UNIT_TABLE:
                                cache.newUnit(after);
                                break;
                            case SOS_NAME + "." + FEATUREOFINTEREST_TABLE:
                                cache.newFeature(after);
                                break;
                            case SOS_NAME + "." + NUMERICVALUE_TABLE:
                                JsonNode source = payload.path("source");
                                if (!source.isMissingNode()) {
                                    JsonNode snapshot = source.path("snapshot");
                                    if (!snapshot.asBoolean()) {
                                        LOG.debug("got a snapshot value, ignoring");
                                    }
                                    else {
                                        Value val = Value.fromJson(after);
                                        sendEnrichedMeasurement(val);
                                    }
                                }
                                break;
                            default:
                                break;
                        }

                    } catch (IOException | NullPointerException ex) {
                        LOG.warn("Error processing database update: " + ex.getMessage());
                        LOG.debug("Value causing below exception: " + record.value(), ex);
                    }

                }
            }
        } catch (WakeupException e) {
            /*
             * ignore for shutdown
             */
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }

    private void initializeDebeziumConnector() throws IOException {
        LOG.info("initializeDebeziumConnector");
        Map<String, Object> map = new HashMap<>();
        map.put("name", "sos-connector");

        Map<String, Object> config = new HashMap<>();
        config.put("connector.class", "io.debezium.connector.postgresql.PostgresConnector");
        config.put("database.hostname", this.kafkaConnectSettings.getProperty("database.hostname", "postgres"));
        config.put("database.port", this.kafkaConnectSettings.getProperty("database.port", "5432"));
        config.put("database.user", this.kafkaConnectSettings.getProperty("database.user", "postgres"));
        config.put("database.password", this.kafkaConnectSettings.getProperty("database.password", "postgres"));
        config.put("database.dbname", this.kafkaConnectSettings.getProperty("database.dbname", "postgres"));
        config.put("database.server.name", SOS_NAME);
        config.put("snapshot.mode", this.kafkaConnectSettings.getProperty("snapshot.mode", "never"));
        config.put("schema.whitelist", SOS_DB_SCHEMA);
        config.put("table.blacklist", "public.geography_columns,public.geometry_columns,public.raster_columns,public.raster_overviews");

        map.put("config", config);

        String jsonNode = mapper.valueToTree(map).toString();
        HttpResponse response = Request.Post(this.kafkaConnectRestBaseUrl + "connectors/")
                .bodyString(jsonNode, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
            //connector was there before, delete it and then retry
            LOG.info("Connector is already installed, deleting first.");
            StatusLine deleteResponse = Request.Delete(this.kafkaConnectRestBaseUrl + "connectors/" + map.get("name"))
                    .execute()
                    .returnResponse()
                    .getStatusLine();
            if (deleteResponse.getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
                String retryResponse = Request.Post(this.kafkaConnectRestBaseUrl + "connectors/")
                        .bodyString(jsonNode, ContentType.APPLICATION_JSON)
                        .execute()
                        .returnContent()
                        .asString();
                LOG.info("Response from kafka connect: " + retryResponse);
            }
            else {
                throw new IOException("Could not delete old sos-connector");
            }
        }
        else if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            String responseString = EntityUtils.toString(response.getEntity());
            throw new IOException(responseString);
        }
        else {
            String responseString = EntityUtils.toString(response.getEntity());
            LOG.info("Response from kafka connect: " + responseString);
        }
        
    }

    private void sendEnrichedMeasurement(Value val) {
        executor.submit(() -> {
            boolean success = false;
            int retries = 0;
            while (!success && retries++ < 3) {
                try {
                    MeasurementObservation mo = MeasurementObservation.fromValue(val, cache);
                    Offering targetOffering = cache.resolveOffering(mo);
                    
                    LOG.info("new measurement for offering '{}': {}", targetOffering, mo);
                    
                    SosOfferingProducer targetProducer = this.producers.get(targetOffering.getId());
                    if (targetProducer != null) {
                        targetProducer.newMeasurement(mo);
                    }
                    else {
                        LOG.info("no producer available for offering: {}", targetOffering.getIdentifier());
                    }
                    success = true;
                } catch (ObservationNotAvailableException | JsonProcessingException ex) {
                    LOG.debug("Could not send enriched observation: " + ex.getMessage());
                    LOG.trace(ex.getMessage(), ex);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        LOG.debug(ex1.getMessage(), ex1);
                    }
                }
            }

            if (!success) {
                LOG.warn("Could not send enriched observation. See related debug/trace-level logs above");
            }
        });
    }
    
    private void initializeProducer(Offering off) {
        SosOfferingProducer prod = new SosOfferingProducer(off, this.bootstrapServers);
        try {
            prod.initialize();
            this.producers.put(off.getId(), prod);
        } catch (IOException ex) {
            LOG.warn("Could not create producer", ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
        }
        
    }
    
    public List<String> getProducerTopics() {
        return this.producers.values().stream()
                .map(p -> p.getOfferingIdentifier())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "KafkaSosConsumer{" + "consumer=" + consumer + ", id=" + id + '}';
    }

}
