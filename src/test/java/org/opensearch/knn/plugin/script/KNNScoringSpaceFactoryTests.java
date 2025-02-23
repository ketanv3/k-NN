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
/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package org.opensearch.knn.plugin.script;

import org.opensearch.knn.KNNTestCase;
import org.opensearch.knn.index.KNNVectorFieldMapper;
import org.opensearch.knn.index.SpaceType;
import org.opensearch.index.mapper.NumberFieldMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class KNNScoringSpaceFactoryTests extends KNNTestCase {
    public void testValidSpaces() {

        KNNVectorFieldMapper.KNNVectorFieldType knnVectorFieldType =
                mock(KNNVectorFieldMapper.KNNVectorFieldType.class);
        NumberFieldMapper.NumberFieldType numberFieldType = new NumberFieldMapper.NumberFieldType("field",
                NumberFieldMapper.NumberType.LONG);
        List<Float> floatQueryObject = new ArrayList<>();
        Long longQueryObject = 0L;

        assertTrue(KNNScoringSpaceFactory.create(SpaceType.L2.getValue(), floatQueryObject, knnVectorFieldType)
                instanceof KNNScoringSpace.L2);
        assertTrue(KNNScoringSpaceFactory.create(SpaceType.COSINESIMIL.getValue(), floatQueryObject, knnVectorFieldType)
                instanceof KNNScoringSpace.CosineSimilarity);
        assertTrue(KNNScoringSpaceFactory.create(SpaceType.INNER_PRODUCT.getValue(), floatQueryObject, knnVectorFieldType)
                instanceof KNNScoringSpace.InnerProd);
        assertTrue(KNNScoringSpaceFactory.create(SpaceType.HAMMING_BIT.getValue(), longQueryObject, numberFieldType)
                instanceof KNNScoringSpace.HammingBit);
    }

    public void testInvalidSpace() {
        expectThrows(IllegalArgumentException.class, () -> KNNScoringSpaceFactory.create(SpaceType.L2.getValue(),
                null, null));
    }
}
