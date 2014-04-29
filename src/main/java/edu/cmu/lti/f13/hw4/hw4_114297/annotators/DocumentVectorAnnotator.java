package edu.cmu.lti.f13.hw4.hw4_114297.annotators;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_114297.VectorSpaceRetrieval;
import edu.cmu.lti.f13.hw4.hw4_114297.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_114297.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_114297.utils.Utils;

import org.tartarus.snowball.ext.englishStemmer; 

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {
	
	public static HashSet<String> stopwords;
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		FSIterator<Annotation> iter = jcas.getAnnotationIndex(Document.type).iterator();
		while (iter.hasNext()) {
			Document doc = (Document) iter.next();
			FSList tokenList = createTermFreqVector(jcas, doc);
			doc.setTokenList(tokenList);
			//for debug printing
			printDocument(doc);
		}
	}
	
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */
	private FSList createTermFreqVector(JCas jcas, Document doc) {
		//construct a vector of tokens and update the tokenList in CAS
		
		//we want to do BoW with some TF-IDF and a 
		// so, we need to preprocess: lower, trimming, stopwords and stemming
		
		//strip whitespaces
		String stripWs = doc.getText().trim().replace("[ ]+", " ");
		//lowercase
		String lower = stripWs.toLowerCase();
		//remove stopWords
		String[] words = lower.split(" ");
		ArrayList<String> wordList = new ArrayList<String>();
		//HashSet<String> stopwords = loadStopwords();
		for(String s: words) {
			if(!stopwords.contains(s))
				wordList.add(s);
		}
		//stemming
		for(int i=0; i<wordList.size(); i++)
			wordList.set(i, stem(wordList.get(i)));
		//count tokens
		HashMap<String, Integer> tokens = new HashMap<String, Integer>();
		for(String s: wordList )
			if(tokens.containsKey(s))
				tokens.put(s, tokens.get(s) + 1);
			else
				tokens.put(s, 1);		
		//finally, we transform the tokenList
		ArrayList<Token> sal = new ArrayList<Token>();
		Token tok = null;
		for(Entry<String, Integer> e: tokens.entrySet()) {
			tok = new Token(jcas);
			tok.setText(e.getKey());
			tok.setFrequency(e.getValue());
			sal.add(tok);
		}
		//aaand, apend it!
		return Utils.fromCollectionToFSList(jcas, sal);
	}
	
	public static HashSet<String> loadStopwords() {
		HashSet<String> res = new HashSet<String>();
		try {
			System.out.println(System.getProperty("user.dir"));
			URL url = VectorSpaceRetrieval.class.getResource("/data/stopwords.txt");	         	 
			
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;
			while((line = br.readLine()) != null)
				if(!line.startsWith("#"))
					res.add(line);
			br.close();
			/*
			 * I dunno why Scanner doesn't work
			System.out.println(url.getPath());
			Scanner jin = new Scanner(new File(url.getPath()));
			String s = null;
			while(jin.hasNextLine()) {
				s = jin.next();
				if(!s.startsWith("#"))
					res.add(s);
			}
			jin.close();
			*/
		} catch(Exception ex) {
			System.err.println("Error reading stopwords.txt: " +ex);
			System.exit(1);
			return null;
		}
		
		return res;
	}
	
	private String stem(String s) {
		englishStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(s);
		stemmer.stem();
		return stemmer.getCurrent();
	}
	
	private void printDocument(Document doc) {
		System.out.println("Text: " +doc.getText());
		StringBuffer sb = new StringBuffer();
		doc.getTokenList().prettyPrint(0, 1, sb, true);
		System.out.println(sb.toString());
	}
}
