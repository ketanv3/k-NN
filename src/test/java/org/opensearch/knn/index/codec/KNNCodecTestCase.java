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

package org.opensearch.knn.index.codec;

import org.opensearch.knn.KNNTestCase;
import org.opensearch.knn.common.KNNConstants;
import org.opensearch.knn.index.KNNIndexCache;
import org.opensearch.knn.index.KNNQuery;
import org.opensearch.knn.index.KNNSettings;
import org.opensearch.knn.index.KNNVectorFieldMapper;
import org.opensearch.knn.index.SpaceType;
import org.opensearch.knn.index.VectorField;
import org.opensearch.knn.index.codec.KNN87Codec.KNN87Codec;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FilterLeafReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.opensearch.cluster.service.ClusterService;
import org.opensearch.common.settings.Settings;
import org.opensearch.knn.index.util.KNNEngine;
import org.opensearch.watcher.ResourceWatcherService;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test used for testing Codecs
 */
public class  KNNCodecTestCase extends KNNTestCase {

    private static FieldType sampleFieldType;
    static {
        sampleFieldType = new FieldType(KNNVectorFieldMapper.Defaults.FIELD_TYPE);
        sampleFieldType.putAttribute(KNNConstants.KNN_METHOD, KNNConstants.METHOD_HNSW);
        sampleFieldType.putAttribute(KNNConstants.KNN_ENGINE, KNNEngine.NMSLIB.getName());
        sampleFieldType.putAttribute(KNNConstants.SPACE_TYPE, SpaceType.L2.getValue());
        sampleFieldType.putAttribute(KNNConstants.HNSW_ALGO_M, "32");
        sampleFieldType.putAttribute(KNNConstants.HNSW_ALGO_EF_CONSTRUCTION, "512");
        sampleFieldType.freeze();
    }

    protected void setUpMockClusterService() {
        ClusterService clusterService = mock(ClusterService.class, RETURNS_DEEP_STUBS);
        Settings settings = Settings.Builder.EMPTY_SETTINGS;
        when(clusterService.state().getMetadata().index(Mockito.anyString()).getSettings()).thenReturn(settings);
        KNNSettings.state().setClusterService(clusterService);
    }

    protected ResourceWatcherService createDisabledResourceWatcherService() {
        final Settings settings = Settings.builder()
                .put("resource.reload.enabled", false)
                .build();
        return new ResourceWatcherService(
                settings,
                null
        );
    }

    public void testFooter(Codec codec) throws Exception {
        setUpMockClusterService();
        Directory dir = newFSDirectory(createTempDir());
        IndexWriterConfig iwc = newIndexWriterConfig();
        iwc.setMergeScheduler(new SerialMergeScheduler());
        iwc.setCodec(codec);

        float[] array = {1.0f, 2.0f, 3.0f};
        VectorField vectorField = new VectorField("test_vector", array, sampleFieldType);
        RandomIndexWriter writer = new RandomIndexWriter(random(), dir, iwc);
        Document doc = new Document();
        doc.add(vectorField);
        writer.addDocument(doc);

        KNNIndexCache.setResourceWatcherService(createDisabledResourceWatcherService());
        IndexReader reader = writer.getReader();
        LeafReaderContext lrc = reader.getContext().leaves().iterator().next(); // leaf reader context
        SegmentReader segmentReader = (SegmentReader) FilterLeafReader.unwrap(lrc.reader());
        String hnswFileExtension = segmentReader.getSegmentInfo().info.getUseCompoundFile()
                ? KNNEngine.NMSLIB.getCompoundExtension() : KNNEngine.NMSLIB.getExtension();
        String hnswSuffix = "test_vector" + hnswFileExtension;
        List<String> hnswFiles = segmentReader.getSegmentInfo().files().stream()
                .filter(fileName -> fileName.endsWith(hnswSuffix))
                .collect(Collectors.toList());
        assertTrue(!hnswFiles.isEmpty());
        ChecksumIndexInput indexInput = dir.openChecksumInput(hnswFiles.get(0), IOContext.DEFAULT);
        indexInput.seek(indexInput.length() - CodecUtil.footerLength());
        CodecUtil.checkFooter(indexInput); // If footer is not valid, it would throw exception and test fails
        indexInput.close();

        IndexSearcher searcher = new IndexSearcher(reader);
        assertEquals(1, searcher.count(new KNNQuery("test_vector", new float[] {1.0f, 2.5f}, 1, "myindex")));

        reader.close();
        writer.close();
        dir.close();
    }

    public void testMultiFieldsKnnIndex(Codec codec) throws Exception {
        setUpMockClusterService();
        Directory dir = newFSDirectory(createTempDir());
        IndexWriterConfig iwc = newIndexWriterConfig();
        iwc.setMergeScheduler(new SerialMergeScheduler());
        iwc.setCodec(codec);

        /**
         * Add doc with field "test_vector"
         */
        float[] array = {1.0f, 3.0f, 4.0f};
        VectorField vectorField = new VectorField("test_vector", array, sampleFieldType);
        RandomIndexWriter writer = new RandomIndexWriter(random(), dir, iwc);
        Document doc = new Document();
        doc.add(vectorField);
        writer.addDocument(doc);
        writer.close();

        /**
         * Add doc with field "my_vector"
         */
        IndexWriterConfig iwc1 = newIndexWriterConfig();
        iwc1.setMergeScheduler(new SerialMergeScheduler());
        iwc1.setCodec(new KNN87Codec());
        writer = new RandomIndexWriter(random(), dir, iwc1);
        float[] array1 = {6.0f, 14.0f};
        VectorField vectorField1 = new VectorField("my_vector", array1, sampleFieldType);
        Document doc1 = new Document();
        doc1.add(vectorField1);
        writer.addDocument(doc1);
        IndexReader reader = writer.getReader();
        writer.close();
        KNNIndexCache.setResourceWatcherService(createDisabledResourceWatcherService());
        List<String> hnswfiles = Arrays.stream(dir.listAll()).filter(x -> x.contains("hnsw")).collect(Collectors.toList());

        // there should be 2 hnsw index files created. one for test_vector and one for my_vector
        assertEquals(hnswfiles.size(), 2);
        assertEquals(hnswfiles.stream().filter(x -> x.contains("test_vector")).collect(Collectors.toList()).size(), 1);
        assertEquals(hnswfiles.stream().filter(x -> x.contains("my_vector")).collect(Collectors.toList()).size(), 1);

        // query to verify distance for each of the field
        IndexSearcher searcher = new IndexSearcher(reader);
        float score = searcher.search(new KNNQuery("test_vector", new float[] {1.0f, 0.0f, 0.0f}, 1, "dummy"), 10).scoreDocs[0].score;
        float score1 = searcher.search(new KNNQuery("my_vector", new float[] {1.0f, 2.0f}, 1, "dummy"), 10).scoreDocs[0].score;
        assertEquals(1.0f/(1 + 25), score, 0.01f);
        assertEquals(1.0f/(1 + 169), score1, 0.01f);

        // query to determine the hits
        assertEquals(1, searcher.count(new KNNQuery("test_vector", new float[] {1.0f, 0.0f, 0.0f}, 1, "dummy")));
        assertEquals(1, searcher.count(new KNNQuery("my_vector", new float[] {1.0f, 1.0f}, 1, "dummy")));

        reader.close();
        dir.close();
    }
}

