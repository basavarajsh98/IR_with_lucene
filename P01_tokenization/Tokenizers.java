package P01;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Tokenizers {

	public static void main(String[] args) throws IOException {
		final String text = "Today is sunny. She is a sunny girl. To be or not to be. She is in Berlin today.\r\n"
				+ "Sunny Berlin! Berlin is always exciting!";
		Tokenizer stream = new StandardTokenizer();
		//Tokenizer stream = new WhitespaceTokenizer();
		stream.setReader(new StringReader(text));
		CharTermAttribute termAttribute = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			System.out.println(termAttribute.toString());
		}
		stream.end();
		stream.close();
	}
}