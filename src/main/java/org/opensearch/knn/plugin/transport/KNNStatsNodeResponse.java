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
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opensearch.knn.plugin.transport;

import org.opensearch.action.support.nodes.BaseNodeResponse;
import org.opensearch.cluster.node.DiscoveryNode;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.xcontent.ToXContentFragment;
import org.opensearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * KNNStatsNodeResponse represents the responses generated by an individual node
 */
public class KNNStatsNodeResponse extends BaseNodeResponse implements ToXContentFragment {

    private Map<String, Object> statsMap;

    /**
     * Constructor
     *
     * @param in  stream
     * @throws IOException in case of I/O errors
     */
    public KNNStatsNodeResponse(StreamInput in) throws IOException {
        super(in);
        this.statsMap    = in.readMap(StreamInput::readString, StreamInput::readGenericValue);
    }

    /**
     * Constructor
     *
     * @param node node
     * @param statsToValues mapping of stat name to value
     */
    public KNNStatsNodeResponse(DiscoveryNode node, Map<String, Object> statsToValues) {
        super(node);
        this.statsMap = statsToValues;
    }

    /**
     * Creates a new KNNStatsNodeResponse object and reads in the stats from an input stream
     *
     * @param in StreamInput to read from
     * @return KNNStatsNodeResponse object corresponding to the input stream
     * @throws IOException throws an IO exception if the StreamInput cannot be read from
     */
    public static KNNStatsNodeResponse readStats(StreamInput in) throws IOException {
        KNNStatsNodeResponse knnStats = new KNNStatsNodeResponse(in);
        return knnStats;
    }

    /**
     * Get the map of stats
     *
     * @return map of stats
     */
    public Map<String, Object> getStatsMap() {
        return statsMap;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeMap(statsMap, StreamOutput::writeString, StreamOutput::writeGenericValue);
    }

    /**
     * Converts statsMap to xContent
     *
     * @param builder XContentBuilder
     * @param params Params
     * @return XContentBuilder
     * @throws IOException thrown by builder for invalid field
     */
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        for (String stat : statsMap.keySet()) {
            builder.field(stat, statsMap.get(stat));
        }

        return builder;
    }
}