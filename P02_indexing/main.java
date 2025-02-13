import java.util.*;
import java.util.Map.Entry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.*;
import static org.junit.Assert.*;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

@SuppressWarnings("deprecation")


public class main {
	public static void main(String args[]) throws IOException, ParseException 
	{
		int word_freq =0; 
        int position = 0;
        
	    
		StandardAnalyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

	    // Store the index in memory:
		Directory dir = FSDirectory.open(new File("D:\\Masters\\Sem2\\IR\\index-dir").toPath());	
	    // To store an index on disk, use this instead:
	    //Directory directory = FSDirectory.open("/tmp/testindex");
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter iwriter = new IndexWriter(dir, config);
		

	    Document doc = new Document();
	    //String text = "Today is sunny. She is a sunny girl. To be or not to be. She is in Berlin today. Sunny Berlin! Berlin is always exciting!";
	    //doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
	    addDoc(iwriter, "today is sunny", "1");
        addDoc(iwriter, "she is a sunny girl", "2");
        addDoc(iwriter, "to be or not to be", "3");
        addDoc(iwriter, "she is in berlin today", "4");
        addDoc(iwriter, "sunny berlin", "5");
        addDoc(iwriter, "berlin is always exciting", "6");

        iwriter.close();
        IndexReader ireader = DirectoryReader.open(dir);
	    IndexSearcher isearcher = new IndexSearcher(ireader);
	    
	    Map map=new HashMap();  
	    
        String text = "Today is sunny. She is a sunny girl. To be or not to be. She is in Berlin today. Sunny Berlin! Berlin is always exciting!";
        text = text.toLowerCase();
        text = text.replaceAll("\\p{Punct}", "");
        String[] listWord = text.split(" ");
	    for (int i = 0; i < listWord.length; i++) {
	    // Now search the index:
	    String q = listWord[i];
	    // Parse a simple query that searches for "text":
	    QueryParser parser = new QueryParser("text", analyzer);
	    Query query = parser.parse(q);
	    TopDocs topDocs = isearcher.search(query, 1000);
	    ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
        map = get_Postings(map,dir, analyzer,ireader,hits, topDocs,query,q,word_freq,position);
        
        System.out.println();
        
        
	    }
	   
	    TreeMap<String,String> tm=new  TreeMap<String,String> (map);  
        Iterator itr=tm.keySet().iterator();               
        while(itr.hasNext())    
        {    
        String key=(String)itr.next();  
        System.out.println(map.get(key));  
        }    
        
        IntersectionAlgorithm("sunny","to",map,ireader,isearcher,analyzer);
        
	    ireader.close();
	    dir.close();
	}
	private static void IntersectionAlgorithm(String term1,String term2,Map map,IndexReader ireader,IndexSearcher isearcher,StandardAnalyzer analyzer) throws ParseException,IOException {
		
		HashSet<String> term1Map = new HashSet<>();
		HashSet<String> term2Map = new HashSet<>();
		
		HashSet<String> tempTerm1Map = new HashSet<>();
		HashSet<String> tempTerm2Map = new HashSet<>();
		
		String q = term1;
	     QueryParser parser = new QueryParser("text", analyzer);
	    Query query = parser.parse(term1);
	    TopDocs topDocs = isearcher.search(query, 1000);
	    ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
	    
		for (final ScoreDoc scoreDoc : hits) {
            final int docId = scoreDoc.doc;
            final Document d = ireader.document(docId);
            String doc_Id = d.get("doc_id");
            term1Map.add(doc_Id);
	    }
		
		//term2
		
		    QueryParser parser1 = new QueryParser("text", analyzer);
		    Query query1 = parser1.parse(term2);
		    TopDocs topDocs1 = isearcher.search(query1, 1000);
		    ScoreDoc[] hits1 = isearcher.search(query1, 1000).scoreDocs;
		    
			for (final ScoreDoc scoreDoc : hits1) {
				
	            final int docId = scoreDoc.doc;
	            final Document d = ireader.document(docId);
	            String doc_Id1 = d.get("doc_id");
	            term2Map.add(doc_Id1);
		    }
			
		tempTerm1Map = term1Map;
		tempTerm2Map = term2Map;
		
		term1Map.retainAll(tempTerm2Map);
		term2Map.retainAll(tempTerm1Map);
		
		if(term1Map.isEmpty() && term2Map.isEmpty())
				System.out.println("No matching documents for "+term1+" and "+term2);
				
			else if(term1Map.isEmpty() && !term2Map.isEmpty())
				System.out.println(term2Map);
			else
				System.out.println(term1Map);
	
		
		
		
	}
	private static Map get_Postings(Map map,Directory dir, StandardAnalyzer analyzer, IndexReader ireader,ScoreDoc[] hits,TopDocs topDocs, Query query,String q,int word_freq,int position ) throws IOException, ParseException {
		final List<Document> docs = new ArrayList<>();
		String result ="["+ q + ":"+ topDocs.totalHits+":" + topDocs.scoreDocs.length+"]"+" --> ";
		
		    //System.out.print("["+ q + ":"+ topDocs.totalHits+":" + topDocs.scoreDocs.length+"]");
		    //System.out.print(" --> ");
	        
		    for (final ScoreDoc scoreDoc : hits) {
	
	            final int docId = scoreDoc.doc;

	            final Document d = ireader.document(docId);
	            
	            docs.add(d);
	            String content = d.get("text");
	            if(content != null)
	            	word_freq = Word_Freq(content,q);//.add(d);
	            	position = Position(content,q);
	            
	            //System.out.print("[" + d.get("doc_id")+":"+ word_freq + ":"+(position+1)+"]");
	            result +="[" + d.get("doc_id")+":"+ word_freq + ":"+(position+1)+"]";
	            map.put(q,result);
	           
		    }
	    return map;
	}
	private static int Position(String string, String word) {
		int position = 0;
		//position = string.indexOf(word);
		String[] listWord = string.split(" ");
	    for (int i = 0; i < listWord.length; i++) {
	        if (listWord[i].equals(word)) {
	            position = i;
	        }
	    }
		return position;
	}
	private static int Word_Freq(String string,String word) {
		//String string = "Spring is beautiful but so is winter";
		//String word = "is";
		String temp[] = string.split(" ");
		int count = 0;
		for (int i = 0; i < temp.length; i++) {
		if (word.equals(temp[i]))
		count++;
		}
		
		return count;
	}
	private static void addDoc(IndexWriter w, String str, String id) throws IOException {

        Document doc = new Document();
        doc.add(new TextField("text", str, Field.Store.YES));
        doc.add(new StringField("doc_id", id, Field.Store.YES));
        w.addDocument(doc);

       

  }


}
