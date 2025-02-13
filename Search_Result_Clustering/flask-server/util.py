import warnings
import numpy as np
from flask import jsonify
import matplotlib.pyplot as plt
from elasticsearch_helper import *
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.feature_extraction.text import TfidfVectorizer

warnings.filterwarnings("ignore")


def get_optimal_clusters(vectors, sample_size):
    """
    This function uses the silhouette score method to determine the optimal number of clusters
    """
    print("Finding optimal number of clusters...")
    silhouette_scores = []
    for n_clusters in range(2, sample_size-1):
        kmeans = KMeans(n_clusters=n_clusters)
        kmeans.fit(vectors)
        cluster_labels = kmeans.labels_
        silhouette_scores.append(silhouette_score(vectors, cluster_labels))
    optimal_n_clusters = np.argmax(silhouette_scores) + 2
    return optimal_n_clusters


def prepare_for_visualization(kmeans, documents):
    """
    This function takes the output of the K-means algorithm and formats the data for visualization.
    """
    cluster_dict, lists = {}, {}
    for i, cluster in enumerate(kmeans.labels_.astype(float)):
        if cluster not in cluster_dict.keys():
            lists[f'list_{int(cluster)}'] = []
        doc_dict = {'document_ID': documents[i][0],
                    'title': documents[i][3],
                    'content': documents[i][2],
                    'score': documents[i][1]}
        lists[f'list_{int(cluster)}'].append(doc_dict)
        cluster_dict[cluster] = lists[f'list_{int(cluster)}']
    return cluster_dict


def cluster_histogram(clusters):
    """
    This function plots a bar chart showing the number of documents in each cluster
    """
    counts = [len(v) for v in clusters.values()]
    clusters_keys = list(clusters.keys())
    plt.figure("Cluster Histogram")
    plt.bar(clusters_keys, counts)
    plt.xlabel('Cluster')
    plt.ylabel('Number of Documents')
    plt.show()


def cluster_analysis(documents):
    """
    This function performs tf-idf vectorization and k-means clustering on the documents
    """
    if len(documents) != 0:
        vectorizer = TfidfVectorizer()
        vectors = vectorizer.fit_transform(
            [content for _, _, content, _ in documents])
        print("Search Result Clustering in process...")
        optimal_n_clusters = get_optimal_clusters(vectors, len(documents))
        kmeans = KMeans(n_clusters=optimal_n_clusters)
        kmeans.fit(vectors)
        print("Search Result Clustering completed")
        cluster_dict = prepare_for_visualization(kmeans, documents)
        # cluster_histogram(cluster_dict)
        return jsonify(cluster_dict)
    else:
        return {}
