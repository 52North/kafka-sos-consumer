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
package org.n52.kafka.sos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.n52.kafka.sos.ObservationNotAvailableException;
import java.util.Date;
import org.n52.kafka.sos.MetadataCache;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class MeasurementObservation {

    private final String procedure;
    private final String feature;
    private final String observableProperty;
    private final Date phenomenonTimeStart;
    private final Date phenomenonTimeEnd;
    private final Date resultTime;
    private final String unit;
    private final double value;
    @JsonIgnore private final Series series;

    public MeasurementObservation(String procedure, String feature, String observableProperty, Date phenomenonTimeStart, Date phenomenonTimeEnd, Date resultTime, String unit, double value, Series series) {
        this.procedure = procedure;
        this.feature = feature;
        this.observableProperty = observableProperty;
        this.phenomenonTimeStart = phenomenonTimeStart;
        this.phenomenonTimeEnd = phenomenonTimeEnd;
        this.resultTime = resultTime;
        this.unit = unit;
        this.value = value;
        this.series = series;
    }


    public String getProcedure() {
        return procedure;
    }

    public String getFeature() {
        return feature;
    }

    public String getObservableProperty() {
        return observableProperty;
    }

    public String getUnit() {
        return unit;
    }

    public double getValue() {
        return value;
    }

    public Date getPhenomenonTimeEnd() {
        return phenomenonTimeEnd;
    }

    public Date getPhenomenonTimeStart() {
        return phenomenonTimeStart;
    }

    public Date getResultTime() {
        return resultTime;
    }

    @JsonIgnore
    public Series getSeries() {
        return series;
    }
    
    public static MeasurementObservation fromValue(Value value, MetadataCache cache) throws ObservationNotAvailableException {
        Observation obs = cache.getObservations().get(value.getObservationId());

        if (obs == null) {
            throw new ObservationNotAvailableException("Observation not available for id: " + value.getObservationId());
        }

        Series series = cache.getSeries().get(obs.getSeriesId());
        if (series == null) {
            throw new ObservationNotAvailableException("Observation not available. No series for id: " + obs.getSeriesId());
        }

        if (!series.isEnriched()) {
            Unit unit = cache.getUnits().get(series.getUnitId());
            Feature feature = cache.getFeatures().get(series.getFeatureId());
            ObservableProperty obsProp = cache.getObservableProperties().get(series.getObservablePropertyId());
            Procedure proc = cache.getProcedures().get(series.getProcedureId());
            series.setFeature(feature);
            series.setUnit(unit);
            series.setObservableProperty(obsProp);
            series.setProcedure(proc);
        }

        return new MeasurementObservation(series.getProcedure() != null ? series.getProcedure().getIdentifier() : null,
                series.getFeature() != null ? series.getFeature().getIdentifier() : null,
                series.getObservableProperty() != null ? series.getObservableProperty().getIdentifier() : null,
                obs.getPhenomenonTimeStart(),
                obs.getPhenomenonTimeEnd(),
                obs.getResultTime(),
                series.getUnit() != null ? series.getUnit().getUnit() : null,
                value.getValue(),
                series);
    }

}
