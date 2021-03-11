package com.github.amkaras.tweetus.stanfordnlp;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class StanfordLemmatizerClient {

    private StanfordCoreNLP stanfordCoreNLP;

    public StanfordLemmatizerClient() {
        Properties properties;
        properties = new Properties();
        properties.put("annotators", "tokenize, ssplit, pos, lemma");
        /*
        Use no initialization when required files are not in the classpath
         */
        this.stanfordCoreNLP = null;
        /*
        Initialize when required files are present.
        Necessary for dictionary building and therefore correct Bayes' algorithm execution
         */
        this.stanfordCoreNLP = new StanfordCoreNLP(properties);
    }

    public List<String> lemmatize(String document) {
        List<String> lemmas = new LinkedList<>();
        Annotation annotation = new Annotation(document);
        stanfordCoreNLP.annotate(annotation);
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                lemmas.add(token.get(LemmaAnnotation.class));
            }
        }
        return lemmas;
    }
}
