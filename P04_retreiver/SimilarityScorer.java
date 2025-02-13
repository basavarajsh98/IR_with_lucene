package IR_P04;
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class SimilarityScorer {

	public static Directory corpus;
	public static IndexReader reader;

	public static void main(String[] args) throws IOException, ParseException {
		List<String> docs = new ArrayList<String>();
		docs.add("Today is sunny");
		docs.add("She is a sunny girl");
		docs.add("To be or not to be");
		docs.add("She is in Berlin today");
		docs.add("Sunny Berlin");
		docs.add("Berlin is always exciting");

		index(docs);
		generateDocVectors();
		findSimilarity(0, 1); // between documents 0 and 1
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
		for (String doci : docs) {
			Document doc = new Document();
			FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
			myFieldType.setStoreTermVectors(true);
			doc.add(new Field("docID", Integer.toString(docCount), myFieldType));
			doc.add(new Field("content", doci, myFieldType));
			writer.addDocument(doc);
			docCount++;
		}
		writer.close();
	}

	public static HashMap<String, Double> tfIdfWeights(int docId) throws IOException {
		int N = reader.numDocs();
		Terms terms = reader.getTermVector(docId, "content");
		TermsEnum termsEnum = terms.iterator();
		BytesRef termRef = null;
		HashMap<String, Double> tfIdfWeights = new HashMap<>();
		while ((termRef = termsEnum.next()) != null) {
			String term = termRef.utf8ToString();
			Term t = new Term("content", term);
			long tf = termsEnum.totalTermFreq();
			long dft = reader.docFreq(t);
			double idf = (Math.log10(N) - Math.log10(dft));
			double weight = tf * idf;
			tfIdfWeights.put(term, weight);
		}
		return tfIdfWeights;
	}

	public static void generateDocVectors() throws IOException {
		System.out.println("Document Vectors: ");
		System.out.println("----------------");
		reader = DirectoryReader.open(corpus);
		for (int docId = 0; docId < reader.numDocs(); docId++) {
			System.out.println("D" + Integer.toString(docId) + ":" + tfIdfWeights(docId));
		}
	}

	public static void euclideanDistance(HashMap<String, Double> vector1, HashMap<String, Double> vector2) {
		double distance = 0;
		for (String term : vector1.keySet()) {
			if (vector2.containsKey(term)) {
				distance += Math.pow((vector1.get(term) - vector2.get(term)), 2);
			} else
				distance += Math.pow(vector1.get(term), 2);
		}
		for (String i : vector2.keySet()) {
			if (!vector1.containsKey(i)) {
				distance += Math.pow(vector2.get(i), 2);
			}
		}
		double simScore = (1 / (1 + Math.sqrt(distance)));
		System.out.println();
		System.out.println("**********************************************");
		System.out.println("Euclidean Distance(D0, D1): " + simScore);
	}

	public static double dotProduct(HashMap<String, Double> vector1, HashMap<String, Double> vector2) {
		double simScore = 0;
		for (String term : vector1.keySet()) {
			if (vector2.containsKey(term)) {
				simScore += (vector1.get(term) * vector2.get(term));
			}
		}
		return simScore;
	}

	public static void cosineSimilarity(HashMap<String, Double> vector1, HashMap<String, Double> vector2) {

		double dotp = dotProduct(vector1, vector2);
		System.out.println("Dot Product(D0, D1): " + dotp);

		double magnitudeV1 = 0;
		double magnitudeV2 = 0;
		for (String term : vector1.keySet()) {
			magnitudeV1 += Math.pow(vector1.get(term), 2);
		}
		for (String term : vector2.keySet()) {
			magnitudeV2 += Math.pow(vector2.get(term), 2);
		}
		double simScore = (dotp / (Math.sqrt(magnitudeV1) * Math.sqrt(magnitudeV2)));
		System.out.println("Cosine(D0, D1): " + simScore);
		System.out.println("**********************************************");
	}

	public static void findSimilarity(int doc1, int doc2) throws IOException {
		HashMap<String, Double> docVector1 = tfIdfWeights(doc1);
		HashMap<String, Double> docVector2 = tfIdfWeights(doc2);
		euclideanDistance(docVector1, docVector2);
		dotProduct(docVector1, docVector2);
		cosineSimilarity(docVector1, docVector2);
	}

}
