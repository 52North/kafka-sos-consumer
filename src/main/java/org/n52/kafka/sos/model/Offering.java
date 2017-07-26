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
public class Offering extends BaseEntity {

    private final String identifier;
    private final String name;

    public Offering(String identifier, String name, int id) {
        super(id);
        this.identifier = identifier;
        this.name = name;
    }

    public static Offering fromJson(JsonNode json) {
        int idValue = json.get("offeringid").asInt();
        String identifierString = json.get("identifier").asText();
        String nameString = json.get("name").asText();
        return new Offering(identifierString, nameString, idValue);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", name)
                .add("identifier", identifier)
                .toString();
    }

}
