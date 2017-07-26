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
public class Value {

    private final int observationId;
    private final double value;

    public Value(int observationId, double value) {
        this.observationId = observationId;
        this.value = value;
    }

    public static Value fromJson(JsonNode json) {
        int idValue = json.get("observationid").asInt();
        double val = json.get("value").asDouble();
        return new Value(idValue, val);
    }

    public int getObservationId() {
        return observationId;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("observationId", observationId)
                .add("value", value)
                .toString();
    }

}
