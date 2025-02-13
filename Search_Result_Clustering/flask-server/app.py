from util import *
from flask import Flask, request
from elasticsearch_helper import *

# Initialize Flask app
app = Flask(__name__)

# index the crawled wiki topics
index()


@ app.route("/query", methods=['POST'])
def query():
    query_str = request.get_json()
    documents = search(query_str['id'])
    clusters = cluster_analysis(documents)
    return clusters
