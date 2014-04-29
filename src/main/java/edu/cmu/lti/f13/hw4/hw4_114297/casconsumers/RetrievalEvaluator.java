package edu.cmu.lti.f13.hw4.hw4_114297.casconsumers;

import java.io.IOException;

import edu.cmu.lti.f13.hw4.hw4_114297.utils.Utils;
import edu.cmu.lti.f13.hw4.hw4_114297.utils.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f13.hw4.hw4_114297.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_114297.typesystems.Token;


public class RetrievalEvaluator extends CasConsumer_ImplBase {
	/** query id number **/
	public ArrayList<Integer> qIdList;
	
	/** query and text relevant values **/
	public ArrayList<Integer> relList;

	public ArrayList<HashMap<String, Integer>> vectorList; 
	
	public ArrayList<Integer> rankListManhattan;
	public ArrayList<Integer> rankListCosine;
	public ArrayList<Integer> rankListJaccard;
	public ArrayList<Integer> rankListDice;
	
	
	public void initialize() throws ResourceInitializationException {
		qIdList = new ArrayList<Integer>();
		relList = new ArrayList<Integer>();
		vectorList = new ArrayList<HashMap<String, Integer>>();
		rankListManhattan = new ArrayList<Integer>();
	}

	/**
	 * TODO :: 
	 * 1. construct the global word dictionary 
	 * 2. keep the word frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {
		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
		while (it.hasNext()) {
			Document doc = (Document) it.next();
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			//ArrayList<Token>tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);
			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			vectorList.add(this.coerceFSListToken(doc.getTokenList()));
			//Do something useful here
		}

	}

	/**
	 * TODO 
	 * 1. Compute Cosine Similarity and rank the retrieved sentences 
	 * 2. Compute the MRR metric
	 * This method executes at the end of the pipeline
	 * thus, we don't have access to the CASes
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
		
		super.collectionProcessComplete(arg0);
		System.out.println("RetrievalEvaluator.collectionProcessComplete");
		// TODO :: compute the cosine similarity measure		
		// TODO :: compute the rank of retrieved sentences
		assert(qIdList.size()==vectorList.size() && vectorList.size()==relList.size());
		
		int n = qIdList.size();
		HashMap<String, Integer> actualQuery = null;
		double simi;
		ArrayList<Pair<Integer, Double>> 
			simiList = new ArrayList<Pair<Integer, Double>>();
		
		for(int i=0; i<n; i++) {
			if(relList.get(i)==99) {
				if(simiList.size()>0) {
					rankListManhattan.add(this.getRank(simiList, this.relList));
					simiList.clear();
				}
				actualQuery = vectorList.get(i);
			} else {
				//simi = computeCosineSimilarity(actualQuery, vectorList.get(i));
				//simi = computeDiceCoefficient(actualQuery, vectorList.get(i));
				//simi = computeJaccardSimilarity(actualQuery, vectorList.get(i));
				simiList.add(new Pair<Integer, Double>(
						i,
						computeInverseManhattanDistance(actualQuery, vectorList.get(i))));
			}
		}
		if(simiList.size()>0) {
			rankListManhattan.add(this.getRank(simiList, this.relList));
			simiList.clear();
		}
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = Utils.compute_mrr(this.rankListManhattan);
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(
			Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		// TODO :: compute cosine similarity between two sentences
		Map<String, Integer> minMap, otherMap;
		if(queryVector.size() < docVector.size()) {
			minMap = queryVector;
			otherMap = docVector;
		} else {
			minMap = docVector;
			otherMap = queryVector;
		} 
		String s;
		Iterator itMin = minMap.values().iterator(),
				itOther = otherMap.values().iterator();
		double normMin = 0, normOther = 0;
		for(Map.Entry<String, Integer> e: minMap.entrySet()) {
			s = e.getKey();
			if(otherMap.containsKey(s))
				cosine_similarity += e.getValue() * otherMap.get(s);
			normMin += Math.pow((Integer)itMin.next(), 2);
			normOther += Math.pow((Integer)itOther.next(), 2);
		}
		while(itOther.hasNext())
			normOther += Math.pow((Integer)itOther.next(), 2);

		return cosine_similarity/(normMin*normOther);
	}
	
	private double computeJaccardSimilarity(
			Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double res = 0.;
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(queryVector.keySet());
		union.addAll(docVector.keySet());
		
		HashSet<String> intersection = new HashSet<String>();
		Set<String> min, other;
		if(queryVector.size() < docVector.size()) {
			min = queryVector.keySet();
			other = docVector.keySet();
		} else {
			min = docVector.keySet();
			other = queryVector.keySet();
		}
		for(String m: min)
			if(other.contains(m))
				intersection.add(m);
		
		res = (double)intersection.size()/union.size();
		
		return res;
	}
	
	private double computeDiceCoefficient(
			Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		HashSet<String> union = new HashSet<String>();
		union.addAll(queryVector.keySet());
		union.addAll(docVector.keySet());
		
		return (double)union.size()/(queryVector.size() + docVector.size());
	}
	
	private double computeInverseManhattanDistance(
		Map<String, Integer> queryVector,
		Map<String, Integer> docVector) {
		double res = 0., a, b;
		
		Map<String, Integer> minMap, otherMap;
		if(queryVector.size() < docVector.size()) {
			minMap = queryVector;
			otherMap = docVector;
		} else {
			minMap = docVector;
			otherMap = queryVector;
		}
		//common elements and disjoint elements of minMap
		for(Map.Entry<String, Integer> e: minMap.entrySet()) {
			if(otherMap.containsKey(e.getKey())) {
				a = e.getValue();
				b = otherMap.get(e.getKey());
			} else {
				a = e.getValue();
				b = 0;
			}
			res += Math.abs(a-b);
		}
		//disjoint elements of otherMap
		a = b = 0;
		for(Map.Entry<String, Integer> e: minMap.entrySet()) {
			if(!minMap.containsKey(e.getKey())) {
				a = 0;
				b = e.getValue();
			}
			res += Math.abs(a-b);
		}
		
		if(Math.abs(res) < 0.000000001)
			return Double.MAX_VALUE;
		
		return 1/res;
	}
	
	/**
	 * 
	 * @param simiList Similarity List
	 * @param relList Relevance List
	 * @return
	 */
	private int getRank(ArrayList<Pair<Integer, Double>> simiList, ArrayList<Integer> relList)
	{
		//sort by cos score, desc
		Collections.sort(simiList, simiList.get(0).getComparatorT2Inverted());
		//add the rank
		for(int j=0; j<simiList.size(); j++) {
			int idx = simiList.get(j).getT1();
			if(relList.get(idx)==1) {
				rankListManhattan.add(j+1);
				System.out.println(
						"Sim=" +String.format("%.4f", (double)simiList.get(j).getT2())
						+" rel=" +relList.get(idx)
						+" rank=" +(j+1) 
						+" qid=" +qIdList.get(idx));
						
				return j+1;
			}
		}
		
		return 0;
	}
	
	private HashMap<String, Integer> coerceFSListToken(FSList list) {
		ArrayList<Token> tokens = Utils.fromFSListToCollection(list, Token.class);
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for(Token t: tokens)
			ret.put(t.getText(), t.getFrequency());
		return ret;
	}	
}
