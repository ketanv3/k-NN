/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_opensearch_knn_index_v2011_KNNIndex */

#ifndef _Included_org_opensearch_knn_index_v2011_KNNIndex
#define _Included_org_opensearch_knn_index_v2011_KNNIndex
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_opensearch_knn_index_v2011_KNNIndex
 * Method:    saveIndex
 * Signature: ([I[[FLjava/lang/String;[Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_opensearch_knn_index_v2011_KNNIndex_saveIndex
  (JNIEnv *, jclass, jintArray, jobjectArray, jstring, jobjectArray, jstring);

/*
 * Class:     org_opensearch_knn_index_v2011_KNNIndex
 * Method:    queryIndex
 * Signature: (J[FI)[Lorg/opensearch/knn/index/KNNQueryResult;
 */
JNIEXPORT jobjectArray JNICALL Java_org_opensearch_knn_index_v2011_KNNIndex_queryIndex
  (JNIEnv *, jclass, jlong, jfloatArray, jint);

/*
 * Class:     org_opensearch_knn_index_v2011_KNNIndex
 * Method:    init
 * Signature: (Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_opensearch_knn_index_v2011_KNNIndex_init
  (JNIEnv *, jclass, jstring, jobjectArray, jstring);

/*
 * Class:     org_opensearch_knn_index_v2011_KNNIndex
 * Method:    gc
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_opensearch_knn_index_v2011_KNNIndex_gc
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_opensearch_knn_index_v2011_KNNIndex
 * Method:    initLibrary
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_opensearch_knn_index_v2011_KNNIndex_initLibrary
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
