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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Series extends BaseEntity {

    private final int featureId;
    private final int observablePropertyId;
    private final int procedureId;
    private final int offeringId;
    private final int unitId;

    private Unit unit;
    private Feature feature;
    private ObservableProperty observableProperty;
    private Procedure procedure;

    public Series(int featureId, int observablePropertyId, int procedureId, int offeringId, int unitId, int id) {
        super(id);
        this.featureId = featureId;
        this.observablePropertyId = observablePropertyId;
        this.procedureId = procedureId;
        this.offeringId = offeringId;
        this.unitId = unitId;
    }

    public static Series fromJson(JsonNode json) {
        int featureIdValue = json.get("featureofinterestid").asInt();
        int observablePropertyIdValue = json.get("observablepropertyid").asInt();
        int procedureIdValue = json.get("procedureid").asInt();
        int offeringIdValue = json.get("offeringid").asInt();
        int unitIdValue = json.get("unitid").asInt();
        int idValue = json.get("seriesid").asInt();
        return new Series(featureIdValue, observablePropertyIdValue, procedureIdValue, offeringIdValue, unitIdValue, idValue);
    }

    public int getFeatureId() {
        return featureId;
    }

    public int getObservablePropertyId() {
        return observablePropertyId;
    }

    public int getProcedureId() {
        return procedureId;
    }

    public int getOfferingId() {
        return offeringId;
    }

    public int getUnitId() {
        return unitId;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public ObservableProperty getObservableProperty() {
        return observableProperty;
    }

    public void setObservableProperty(ObservableProperty observableProperty) {
        this.observableProperty = observableProperty;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    public boolean isEnriched() {
        return this.feature != null || this.unit != null || this.observableProperty != null || this.procedure != null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("featureId", featureId)
                .add("observablePropertyId", observablePropertyId)
                .add("procedureId", procedureId)
                .add("offeringId", offeringId)
                .add("unitId", unitId)
                .toString();
    }

}
