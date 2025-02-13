import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;

public class main {
	public static void main(String[] args) {
		
		String text = "Today is sunny. She is a sunny girl. To be or not to be. She is in Berlin today. Sunny Berlin! Berlin is always exciting!";
		
		StandardTokenizer t = new StandardTokenizer();
		WhitespaceTokenizer wt = new WhitespaceTokenizer();
		StandardTokenizer spt = new StandardTokenizer();
		StandardTokenizer ca = new StandardTokenizer();
		
		t.setReader(new StringReader(text));
		wt.setReader(new StringReader(text));
		spt.setReader(new StringReader(text));
		ca.setReader(new StringReader(text));
		
		TokenStream lowerCase = new LowerCaseFilter(ca);
		
		CharArraySet stopWords = new CharArraySet(5, true);
		stopWords.add("was");
		stopWords.add("is");
		stopWords.add("in");
		stopWords.add("to");
		stopWords.add("be");
		
		TokenStream stopFilter = new StopFilter(spt, stopWords);
		TokenStream stopFilterAnalyzer = new StopFilter(lowerCase, stopWords);
		TokenStream porterStem = new PorterStemFilter(stopFilterAnalyzer);
		
		try {
			t.reset();
			wt.reset();
			stopFilter.reset();
			porterStem.reset();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		CharTermAttribute attr = t.addAttribute(CharTermAttribute.class);
		CharTermAttribute wtattr = wt.addAttribute(CharTermAttribute.class);
		CharTermAttribute sptattr = spt.addAttribute(CharTermAttribute.class);
		CharTermAttribute caattr = porterStem.addAttribute(CharTermAttribute.class);
		
		try {
			System.out.println("Standard Tokenizer");
			while(t.incrementToken()) {
				System.out.print(attr.toString() + " ");
			}
			System.out.println("\n");
			
			System.out.println("WhiteSpace Tokenizer");
			while(wt.incrementToken()) {
				System.out.print(wtattr.toString() + " ");
			}
			System.out.println("\n");
			
			System.out.println("StopWord Removal tokenizer");
			while (stopFilter.incrementToken()) {
				System.out.print(sptattr.toString() + " ");
			}
			System.out.println("\n");
			
			System.out.println("Custom Analyzer");
			while (porterStem.incrementToken()) {
				System.out.print(caattr.toString() + " ");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
