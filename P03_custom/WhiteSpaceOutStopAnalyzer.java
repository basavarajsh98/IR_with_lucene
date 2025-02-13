import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class WhiteSpaceOutStopAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new WhitespaceTokenizer();
        TokenStream ts = source;


        ShingleFilter theFilter = new ShingleFilter(ts);
        theFilter.setOutputUnigrams(false);
        return new TokenStreamComponents(source, theFilter);
  }

    public static void main(String[] args) throws IOException, ParseException {
    	//Biword Tokenizer
        Analyzer wsanalyzer = new WhiteSpaceOutStopAnalyzer();
        String text = "Today is sunny. She is a sunny girl. To be or not to be. She is in Berlin today. Sunny Berlin! Berlin is always exciting!";
		text = text.replaceAll("\\p{Punct}", " ").toLowerCase();
		BiWordTokenizer(wsanalyzer,text);
		wsanalyzer.close();
		
		
     //Assignment 4.3a
	 System.out.println("\nAssignment 4.3a");
	 System.out.println();
     StandardAnalyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
     IndexWriterConfig config = new IndexWriterConfig(analyzer);
     Directory dir = FSDirectory.open(new File("D:\\Masters\\Sem2\\IR\\index-dir").toPath());	
     IndexWriter iwriter = new IndexWriter(dir, config);
     Document doc = new Document();
	 addDoc(iwriter, "today is sunny", "1");
     addDoc(iwriter, "she is a sunny girl", "2");
     addDoc(iwriter, "to be or not to be", "3");
     addDoc(iwriter, "she is in berlin today", "4");
     addDoc(iwriter, "sunny berlin", "5");
     addDoc(iwriter, "berlin is always exciting", "6");
     addDoc(iwriter,"York University,is a public research university in Canada.It is not located in New York","7");
     addDoc(iwriter,"New York University is a private research university in New York City.","8");
     iwriter.close();
     
     String searchquery = "New York University";
     System.out.println("The search query is "+searchquery);
     
     IndexReader ireader = DirectoryReader.open(dir);
     IndexSearcher isearcher = new IndexSearcher(ireader);
     QueryParser qp = new QueryParser("text", analyzer);
     Query textq = qp.parse(searchquery);
     
     TopDocs hits = isearcher.search(textq, 10);
     for (ScoreDoc sd : hits.scoreDocs) 
     {
         Document d = isearcher.doc(sd.doc);
         System.out.print(String.format("Document "+d.get("doc_id")+": "+d.get("text"))+" - ");
         if(d.get("text").toLowerCase().contains(searchquery.toLowerCase()))
        	 System.out.println("True Positive");
         else
        	 System.out.println("False Positive");
        
     }
    }
    private static void BiWordTokenizer(Analyzer wsanalyzer,String text) throws IOException {
    	TokenStream ts = wsanalyzer.tokenStream(null,text);
		CharTermAttribute attr = ts.addAttribute(CharTermAttribute.class);
		ts.reset();
		while(ts.incrementToken()) {
			    String term = attr.toString();
			    System.out.println(term); 
			}
    }
    private static void addDoc(IndexWriter w, String str, String id) throws IOException {

        Document doc = new Document();
        doc.add(new TextField("text", str, Field.Store.YES));
        doc.add(new StringField("doc_id", id, Field.Store.YES));
        w.addDocument(doc);
  }   
}
