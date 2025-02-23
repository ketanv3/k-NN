/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.knn.index;

import com.google.common.collect.ImmutableMap;
import org.opensearch.knn.KNNTestCase;
import org.opensearch.common.xcontent.ToXContent;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.index.mapper.MapperParsingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.opensearch.knn.common.KNNConstants.NAME;
import static org.opensearch.knn.common.KNNConstants.PARAMETERS;

public class MethodComponentContextTests extends KNNTestCase {
    /**
     * Test parse where input is invalid
     */
    public void testParse_invalid() throws IOException {
        // Input is not a Map
        Integer invalidIn = 12;
        expectThrows(MapperParsingException.class, () -> MethodComponentContext.parse(invalidIn));

        // Name not passed in
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject().endObject();
        Map<String, Object> in0 = xContentBuilderToMap(xContentBuilder);
        expectThrows(MapperParsingException.class, () -> MethodComponentContext.parse(in0));

        // Invalid name type
        xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(NAME, 12)
                .endObject();
        Map<String, Object> in1 = xContentBuilderToMap(xContentBuilder);
        expectThrows(MapperParsingException.class, () -> MethodComponentContext.parse(in1));

        // Invalid parameter type
        xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(PARAMETERS, 12)
                .endObject();
        Map<String, Object> in2 = xContentBuilderToMap(xContentBuilder);
        expectThrows(MapperParsingException.class, () -> MethodComponentContext.parse(in2));

        // Invalid key
        xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field("invalid", 12)
                .endObject();
        Map<String, Object> in3 = xContentBuilderToMap(xContentBuilder);
        expectThrows(MapperParsingException.class, () -> MethodComponentContext.parse(in3));
    }

    /**
     * Test name getter
     */
    public void testGetName() {
        String name = "test-name";
        MethodComponentContext methodContext = new MethodComponentContext(name, null);
        assertEquals(name, methodContext.getName());
    }


    /**
     * Test parameters getter
     */
    public void testGetParameters() throws IOException {
        String name = "test-name";
        String paramKey1 = "p-1";
        String paramVal1 = "v-1";
        String paramKey2 = "p-2";
        Integer paramVal2 = 1;
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(paramKey1, paramVal1)
                .field(paramKey2, paramVal2)
                .endObject();
        Map<String, Object> params = xContentBuilderToMap(xContentBuilder);
        MethodComponentContext methodContext = new MethodComponentContext(name, params);
        assertEquals(paramVal1, methodContext.getParameters().get(paramKey1));
        assertEquals(paramVal2, methodContext.getParameters().get(paramKey2));
    }

    /**
     * Test parse where input is valid
     */
    public void testParse_valid() throws IOException {
        // Empty parameters
        String name = "test-name";
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(NAME, name)
                .endObject();
        Map<String, Object> in = xContentBuilderToMap(xContentBuilder);
        MethodComponentContext methodContext = MethodComponentContext.parse(in);
        assertEquals(name, methodContext.getName());
        assertNull(methodContext.getParameters());

        // Multiple parameters
        String paramKey1 = "p-1";
        String paramVal1 = "v-1";
        String paramKey2 = "p-2";
        Integer paramVal2 = 1;

        xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(NAME, name)
                .startObject(PARAMETERS)
                .field(paramKey1, paramVal1)
                .field(paramKey2, paramVal2)
                .endObject()
                .endObject();
        in = xContentBuilderToMap(xContentBuilder);
        methodContext = MethodComponentContext.parse(in);

        assertEquals(paramVal1, methodContext.getParameters().get(paramKey1));
        assertEquals(paramVal2, methodContext.getParameters().get(paramKey2));
    }

    /**
     * Test  toXContent
     */
    public void testToXContent() throws IOException {
        // Empty parameters
        String name = "test-name";
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(NAME, name)
                .endObject();
        Map<String, Object> in = xContentBuilderToMap(xContentBuilder);
        MethodComponentContext methodContext = MethodComponentContext.parse(in);

        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
        builder = methodContext.toXContent(builder, ToXContent.EMPTY_PARAMS).endObject();

        Map<String, Object> out = xContentBuilderToMap(builder);
        assertEquals(name, out.get(NAME));


        // Multiple parameters
        String paramKey1 = "p-1";
        String paramVal1 = "v-1";
        String paramKey2 = "p-2";
        Integer paramVal2 = 1;
        xContentBuilder = XContentFactory.jsonBuilder().startObject()
                .field(NAME, name)
                .startObject(PARAMETERS)
                .field(paramKey1, paramVal1)
                .field(paramKey2, paramVal2)
                .endObject()
                .endObject();
        in = xContentBuilderToMap(xContentBuilder);
        methodContext = MethodComponentContext.parse(in);

        builder = XContentFactory.jsonBuilder().startObject();
        builder = methodContext.toXContent(builder, ToXContent.EMPTY_PARAMS).endObject();

        out = xContentBuilderToMap(builder);

        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) out.get(PARAMETERS);

        assertEquals(paramVal1, paramMap.get(paramKey1));
        assertEquals(paramVal2, paramMap.get(paramKey2));
    }

    public void testEquals() {
        String name1 = "name1";
        String name2 = "name2";
        Map<String, Object> parameters1 = ImmutableMap.of(
                "param1", "v1",
                "param2", 18
        );

        Map<String, Object> parameters2 = new HashMap<>(parameters1);

        Map<String, Object> parameters3 = ImmutableMap.of(
                "param1", "v1"
        );


        MethodComponentContext methodContext1 = new MethodComponentContext(name1, parameters1);
        MethodComponentContext methodContext2 = new MethodComponentContext(name1, parameters1);
        MethodComponentContext methodContext3 = new MethodComponentContext(name2, parameters2);

        assertEquals(methodContext1, methodContext1);
        assertEquals(methodContext1, methodContext2);
        assertNotEquals(methodContext1, methodContext3);
        assertNotEquals(methodContext1, null);
    }

    public void testHashCode() {
        String name1 = "name1";
        String name2 = "name2";
        Map<String, Object> parameters1 = ImmutableMap.of(
                "param1", "v1",
                "param2", 18
        );

        Map<String, Object> parameters2 = new HashMap<>(parameters1);

        Map<String, Object> parameters3 = ImmutableMap.of(
                "param1", "v1"
        );

        MethodComponentContext methodContext1 = new MethodComponentContext(name1, parameters1);
        MethodComponentContext methodContext2 = new MethodComponentContext(name1, parameters1);
        MethodComponentContext methodContext3 = new MethodComponentContext(name2, parameters2);

        assertEquals(methodContext1.hashCode(), methodContext1.hashCode());
        assertEquals(methodContext1.hashCode(), methodContext2.hashCode());
        assertNotEquals(methodContext1.hashCode(), methodContext3.hashCode());
    }
}
