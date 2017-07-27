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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.kafka.sos.model.Feature;
import org.n52.kafka.sos.model.MeasurementObservation;
import org.n52.kafka.sos.model.Observation;
import org.n52.kafka.sos.model.Offering;
import org.n52.kafka.sos.model.Procedure;
import org.n52.kafka.sos.model.Series;
import org.n52.kafka.sos.model.Unit;
import org.n52.kafka.sos.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class MetadataCacheTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataCacheTest.class.getName());

    @Test
    public void testWorkflow() throws IOException, ObservationNotAvailableException {
        MetadataCache cache = new MetadataCache();

        cache.newProcedure(readJsonContents("/procedure1.json"));
        cache.newOffering(readJsonContents("/offering1.json"));
        cache.newFeature(readJsonContents("/feature1.json"));
        cache.newUnit(readJsonContents("/unit1.json"));
        cache.newSeries(readJsonContents("/series1.json"));
        cache.newObservation(readJsonContents("/observation1.json"));

        Procedure proc = cache.getProcedures().get(1);
        Assert.assertThat(proc.getIdentifier(), CoreMatchers.equalTo("http://www.52north.org/test/procedure/1"));

        Offering off = cache.getOfferings().get(1);
        Assert.assertThat(off.getName(), CoreMatchers.equalTo("Offering for sensor 1"));

        Feature feat = cache.getFeatures().get(1);
        Assert.assertThat(feat.getIdentifier(), CoreMatchers.equalTo("http://www.52north.org/test/featureOfInterest/world"));

        Unit un = cache.getUnits().get(1);
        Assert.assertThat(un.getUnit(), CoreMatchers.equalTo("test_unit_1"));

        Series ser = cache.getSeries().get(1);
        Assert.assertThat(ser.getFeatureId(), CoreMatchers.is(1));

        Observation obs = cache.getObservations().get(1);
        Assert.assertTrue(obs.getResultTime().equals(new Date(1353330000000L)));

        Value value = Value.fromJson(readJsonContents("/value1.json"));

        MeasurementObservation mo = MeasurementObservation.fromValue(value, cache);

        String jsonMo = new ObjectMapper().setSerializationInclusion(Include.NON_NULL).writeValueAsString(mo);
        LOG.info(jsonMo);
    }

    private JsonNode readJsonContents(String procedure1json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.readTree(getClass().getResourceAsStream(procedure1json));
        return result.path("payload").path("after");
    }

}
