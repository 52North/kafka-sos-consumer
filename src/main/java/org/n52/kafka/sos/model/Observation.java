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
import java.util.Date;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Observation extends BaseEntity {

    private final int seriesId;
    private final String identifier;
    private final Date phenomenonTimeStart;
    private final Date phenomenonTimeEnd;
    private final Date resultTime;

    public Observation(int seriesId, String identifier, Date phenomenonTimeStart, Date phenomenonTimeEnd, Date resultTime, int id) {
        super(id);
        this.seriesId = seriesId;
        this.identifier = identifier;
        this.phenomenonTimeStart = phenomenonTimeStart;
        this.phenomenonTimeEnd = phenomenonTimeEnd;
        this.resultTime = resultTime;
    }

    public static Observation fromJson(JsonNode json) {
        int seriesIdValue = json.get("seriesid").asInt();
        int idValue = json.get("observationid").asInt();
        String identifierString = json.get("identifier").asText();
        Date phenomenonTimeStartValue = Observation.toDate(json.get("phenomenontimestart").asLong());
        Date phenomenonTimeEndValue = Observation.toDate(json.get("phenomenontimeend").asLong());
        Date resultTimeValue = Observation.toDate(json.get("resulttime").asLong());
        return new Observation(seriesIdValue, identifierString, phenomenonTimeStartValue, phenomenonTimeEndValue, resultTimeValue, idValue);
    }

    private static Date toDate(long asLong) {
        if (asLong > 1000000000000000000L) {
            return new Date(asLong / 1000 / 1000);
        }

        if (asLong > 1000000000000000L) {
            return new Date(asLong / 1000);
        }

        return new Date(asLong);
    }

    public int getSeriesId() {
        return seriesId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Date getPhenomenonTimeStart() {
        return phenomenonTimeStart;
    }

    public Date getPhenomenonTimeEnd() {
        return phenomenonTimeEnd;
    }

    public Date getResultTime() {
        return resultTime;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("seriesId", seriesId)
                .add("identifier", identifier)
                .add("phenomenonTimeStart", phenomenonTimeStart)
                .add("phenomenonTimeEnd", phenomenonTimeEnd)
                .add("resultTime", resultTime)
                .toString();
    }

}
