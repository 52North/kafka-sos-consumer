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
public class Feature extends BaseEntity {

    private final String identifier;
    private final String name;
    private final int type;

    public Feature(String identifier, String name, int type, int id) {
        super(id);
        this.identifier = identifier;
        this.name = name;
        this.type = type;
    }

    public static Feature fromJson(JsonNode json) {
        int idValue = json.get("featureofinterestid").asInt();
        String identifierString = json.get("identifier").asText();
        String nameString = json.get("name").asText();
        int typeValue = json.get("featureofinteresttypeid").asInt();

        return new Feature(identifierString, nameString, typeValue, idValue);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
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
