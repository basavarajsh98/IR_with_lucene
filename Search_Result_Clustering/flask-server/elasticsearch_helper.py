import os
from elasticsearch import Elasticsearch

# Global variable to store the index name
global index_name
index_name = "topics"

# Connect to the Elasticsearch cluster
es = Elasticsearch([{'host': 'localhost', 'port': 9200, "scheme": "http"}])


def create_index():
    """
    This function creates an index if doesn't exist already and indexes the documents
    """
    if not es.indices.exists(index=index_name):
        print("Creating index...")
        print("Configuring the indexer...")
        es.indices.create(index=index_name, body={
            "settings": {
                "analysis": {
                    "analyzer": {
                        "my_analyzer": {
                            "type": "custom",
                            "tokenizer": "standard",
                            "filter": ["lowercase", "stop", "porter_stem"]
                        }
                    },
                    "filter": {
                        "stop": {
                            "type":       "stop",
                            "stopwords":  "_english_"
                        },
                        "porter_stem": {
                            "type": "stemmer",
                            "name": "porter2"
                        }
                    }
                }
            },
            "mappings": {
                "properties": {
                    "content": {
                        "type": "text",
                        "analyzer": "my_analyzer"
                    }
                }
            }
        })
        return False
    else:
        print("Index '{}' already exist!".format(index_name))
        return True


def index():
    indexed = create_index()
    if not indexed:
        print("Indexing the documents...")
        docId = 1
        for filename in os.listdir("crawled_topics"):
            with open(os.path.join("crawled_topics", filename), 'r', encoding='utf8') as f:
                document = {
                    "title": filename.split(".")[0],
                    "content": f.read(),
                    "analyzer": "my_analyzer"
                }
            es.index(index=index_name, id=docId, body=document)
            docId += 1
        print(f"Successfully indexed {docId} documents!")
        es.indices.refresh(index=index_name)


def search(query_str):
    """
    This function performs a search query on the Elasticsearch index for the 'query_str'
    """
    print(f"Searching for '{query_str}'...")
    res = es.search(index=index_name, body={
        "explain": True,
        'size': 25,
        'query': {
            'match': {
                'content':  {
                    "query": query_str,
                    "analyzer": "my_analyzer"
                }
            }
        }
    })
    documents = [(hit['_id'],
                  hit["_score"],
                  hit['_source']['content'],
                  hit['_source']['title']) for hit in res['hits']['hits']]
    print(f"Found {len(documents)} matching documents")
    return documents
