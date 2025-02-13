package IR_P04;
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class RelevantDocumentFinder {
 
	public static Directory corpus;

	public static void main(String[] args) throws IOException {
		List<String> docs = new ArrayList<String>();
		docs.add("Today is sunny");
		docs.add("She is a sunny girl");
		docs.add("To be or not to be");
		docs.add("She is in Berlin today");
		docs.add("Sunny Berlin");
		docs.add("Berlin is always exciting");

		index(docs);
		search("She is a sunny girl");
	}

	public static void index(List<String> docs) throws IOException {
		// create an index path
		new File(".\\index").mkdir();
		corpus = FSDirectory.open(new File(".\\index").toPath());

		Analyzer analyzer = new SimpleAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(corpus, config);
		writer.deleteAll();

		int docCount = 0;
		for (String doc1 : docs) {
			Document doc = new Document();
			FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
			doc.add(new Field("docID", Integer.toString(docCount), myFieldType));
			doc.add(new Field("content", doc1, myFieldType));
			writer.addDocument(doc);
			docCount++;
		}
		writer.close();
	}

	public static void search(String searchQuery) throws IOException {
		QueryParser qp = new QueryParser("content", new SimpleAnalyzer());
		Query q = null;
		try {
			q = qp.parse(searchQuery);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		IndexReader reader = DirectoryReader.open(corpus);
		IndexSearcher searcher = new IndexSearcher(reader);

//		creating a list to iterate through VSM and BM25 models
		List<Similarity> models = new LinkedList<Similarity>();
		models.add(new ClassicSimilarity());
		models.add(new BM25Similarity());
		for (Similarity simMeasure : models) {
			searcher.setSimilarity(simMeasure);
			TopDocs docs = searcher.search(q, 6);
			ScoreDoc[] scored = docs.scoreDocs;
			System.out.println(simMeasure.toString());
			System.out.println("-------------------");
			for (ScoreDoc aDoc : scored) {
				Document d = searcher.doc(aDoc.doc);
				System.out.println(aDoc.score + " : " + d.get("content"));
			}
			System.out.println();
		}
	}
}