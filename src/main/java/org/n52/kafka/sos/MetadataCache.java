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

import org.n52.kafka.sos.model.Procedure;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.n52.kafka.sos.model.Feature;
import org.n52.kafka.sos.model.MeasurementObservation;
import org.n52.kafka.sos.model.ObservableProperty;
import org.n52.kafka.sos.model.Observation;
import org.n52.kafka.sos.model.Offering;
import org.n52.kafka.sos.model.Series;
import org.n52.kafka.sos.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class MetadataCache {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataCache.class.getName());

    private final Map<Integer, Observation> observations = new HashMap<>();
    private final Map<Integer, Series> series = new HashMap<>();
    private final Map<Integer, Procedure> procedures = new HashMap<>();
    private final Map<Integer, Offering> offerings = new HashMap<>();
    private final Map<Integer, ObservableProperty> observableProperties = new HashMap<>();
    private final Map<Integer, Unit> units = new HashMap<>();
    private final Map<Integer, Feature> features = new HashMap<>();

    void newObservation(JsonNode json) {
        Observation obs = Observation.fromJson(json);
        LOG.info("newObservation " + obs);
        this.observations.put(obs.getId(), obs);
    }

    void newSeries(JsonNode json) {
        Series series = Series.fromJson(json);
        LOG.info("newSeries " + series);
        this.series.put(series.getId(), series);
    }

    void newProcedure(JsonNode json) {
        Procedure proc = Procedure.fromJson(json);
        LOG.info("newProcedure " + proc);
        this.procedures.put(proc.getId(), proc);
    }

    void newOffering(JsonNode json) {
        Offering off = Offering.fromJson(json);
        LOG.info("newOffering " + off);
        this.offerings.put(off.getId(), off);
    }

    void newObservableProperty(JsonNode json) {
        ObservableProperty obsp = ObservableProperty.fromJson(json);
        LOG.info("newObservableProperty " + obsp);
        this.observableProperties.put(obsp.getId(), obsp);
    }

    void newUnit(JsonNode json) {
        Unit unit = Unit.fromJson(json);
        LOG.info("newUnit " + unit);
        this.units.put(unit.getId(), unit);
    }

    void newFeature(JsonNode json) {
        Feature feature = Feature.fromJson(json);
        LOG.info("newFeature " + feature);
        this.features.put(feature.getId(), feature);
    }

    public Map<Integer, Observation> getObservations() {
        return observations;
    }

    public Map<Integer, Series> getSeries() {
        return series;
    }

    public Map<Integer, Procedure> getProcedures() {
        return procedures;
    }

    public Map<Integer, Offering> getOfferings() {
        return offerings;
    }

    public Map<Integer, ObservableProperty> getObservableProperties() {
        return observableProperties;
    }

    public Map<Integer, Unit> getUnits() {
        return units;
    }

    public Map<Integer, Feature> getFeatures() {
        return features;
    }

    public Offering resolveOffering(MeasurementObservation mo) {
        int id = mo.getSeries().getOfferingId();
        return offerings.get(id);
    }

}
