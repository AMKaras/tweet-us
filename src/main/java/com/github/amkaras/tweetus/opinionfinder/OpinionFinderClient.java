package com.github.amkaras.tweetus.opinionfinder;

import com.github.amkaras.tweetus.opinionfinder.entity.OpinionFinderAnalysis;
import com.github.amkaras.tweetus.twitter.entity.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpinionFinderClient {

    private static final Logger log = LoggerFactory.getLogger(OpinionFinderClient.class);

    private static final String BATCH_FILE = "tweets-batch.doclist";
    private static final String TXT = ".txt";
    private static final String TMP_DIR = "tmp";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String NEW_LINE = "\n";

    private static final String OPINION_FINDER_POLARITY_CLASSIFIER_FILENAME = "exp_polarity.txt";
    private static final Set<String> OPINION_FINDER_SUBJECTIVE_CLUE_FILENAMES = Set.of(
            "subjclueslen1polar"
//            "subjcluesSentenceClassifiersOpinionFinderJune06"
    );
    private static final String OPINION_FINDER_ANALYSIS_FOLDER_SUFFIX = "_auto_anns";

    private static final String OPINION_FINDER_RUN_COMMAND = "java -Xmx1g -classpath " +
            "./opinionfinderv2.0/lib/weka.jar:" +
            "./opinionfinderv2.0/lib/stanford-postagger.jar:" +
            "./opinionfinderv2.0/opinionfinder.jar " +
            "opin.main.RunOpinionFinder " +
            BATCH_FILE + " -d " +
            "-m ./opinionfinderv2.0/models/ " +
            "-l ./opinionfinderv2.0/lexicons/";

    private final OpinionFinderAnalysisParser analysisParser = new OpinionFinderAnalysisParser();

    public OpinionFinderAnalysis analyze(Tweet tweet)
            throws IOException, InterruptedException {

        var filesToBeAnalyzed = createFilesToBeAnalyzed(tweet);
        runOpinionFinderJar();
        var polarityClassifierAnalysisLines = readPolarityClassifierAnalysis(filesToBeAnalyzed);
        var subjectiveCluesAnalysisLines = readSubjectiveCluesAnalysis(filesToBeAnalyzed);
        deleteCreatedFiles();

        var analysis = new OpinionFinderAnalysis();
        var polarityClassifiers = analysisParser.parsePolarityClassifierLines(polarityClassifierAnalysisLines);
        polarityClassifiers.forEach(polarityClassifier -> polarityClassifier.setAnalysis(analysis));
        var subjectiveClues = analysisParser.parseSubjectiveClueLines(subjectiveCluesAnalysisLines);
        subjectiveClues.forEach(subjectiveClue -> subjectiveClue.setAnalysis(analysis));
        analysis.setPolarityClassifiers(polarityClassifiers);
        analysis.setSubjectiveClues(subjectiveClues);
        var dictionaryEntries = analysisParser
                .extractDictionaryEntries(subjectiveCluesAnalysisLines, polarityClassifierAnalysisLines, tweet.getContent());
        dictionaryEntries.forEach(dictionaryEntry -> dictionaryEntry.setAnalysis(analysis));
        analysis.setDictionary(dictionaryEntries);
        return analysis;
    }

    private List<String> createFilesToBeAnalyzed(Tweet tweet) throws IOException {
        new File(TMP_DIR).mkdir();
        final List<String> filenames = new ArrayList<>();
        String filename = TMP_DIR + SLASH + tweet.getId() + TXT;
        BufferedWriter txtBw = new BufferedWriter(new FileWriter(filename));
        txtBw.write(tweet.getContent());
        txtBw.close();
        filenames.add(filename);
        BufferedWriter doclistBw = new BufferedWriter(new FileWriter(BATCH_FILE));
        doclistBw.write(String.join(NEW_LINE, filenames));
        doclistBw.close();
        return filenames;
    }

    private void runOpinionFinderJar() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(OPINION_FINDER_RUN_COMMAND);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line)
                    .append(NEW_LINE);
        }
        log.debug("OpinionFinder run process command line output was: {}{}", NEW_LINE, sb.toString());
        p.waitFor();
        log.debug("OpinionFinder run process exited with code {}", p.exitValue());
        p.destroy();
    }

    private List<String> readPolarityClassifierAnalysis(List<String> filenames) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (String filename : filenames) {
            File file = new File(DOT + SLASH + filename +
                    OPINION_FINDER_ANALYSIS_FOLDER_SUFFIX + SLASH + OPINION_FINDER_POLARITY_CLASSIFIER_FILENAME);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<String> readSubjectiveCluesAnalysis(List<String> filenames) throws IOException {
        final List<String> lines = new ArrayList<>();
        for (String filename : filenames) {
            for (String analysisFilename : OPINION_FINDER_SUBJECTIVE_CLUE_FILENAMES) {
                File file = new File(DOT + SLASH + filename +
                        OPINION_FINDER_ANALYSIS_FOLDER_SUFFIX + SLASH + analysisFilename);
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private void deleteCreatedFiles() {
        File batchFile = new File(BATCH_FILE);
        if (batchFile.delete()) {
            log.debug("Successfully deleted {}", BATCH_FILE);
        } else {
            log.error("Error when deleting {}", BATCH_FILE);
        }
        String tmpDirectory = DOT + SLASH + TMP_DIR + SLASH;
        if (deleteDirectory(new File(tmpDirectory))) {
            log.debug("Successfully deleted {} directory", tmpDirectory);
        } else {
            log.error("Error when deleting {} directory", tmpDirectory);
        }
    }

    private boolean deleteDirectory(File directory) {
        File[] subdirectories = directory.listFiles();
        if (subdirectories != null) {
            for (File file : subdirectories) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }
}
