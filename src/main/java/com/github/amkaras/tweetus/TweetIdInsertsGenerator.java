package com.github.amkaras.tweetus;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class TweetIdInsertsGenerator {

    private static final String ABSOLUTE_PATH = "/Users/aleksandrakaras/tweetus/src/main/resources/collections/";
    private static final String TXT = ".txt";
    private static final String README = "README";

    private static final String POSTGRES_CONN_URL = "jdbc:postgresql://" +
            "tweet-us.cpmks09kcfcz.us-east-1.rds.amazonaws.com:5432/tweetus" +
            "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASS = "Lecazdrzewajakdawniejkasztany";

    private static final int MAX_PATCH_SIZE = 100_000;
    private static final Map.Entry<String, Integer> DEFAULT_SAMPLING = Map.entry("DEFAULT", 100);
    private static final Map<String, Integer> SAMPLING_PER_COLLECTION = Map.of(
            "election-filter", 100,
            "election-day", 25,
            "debate", 25,
            "convention-filter", 10,
            "timelines", 1
    );

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Class.forName("org.postgresql.Driver");

        var sw = Stopwatch.createStarted();

        try (Stream<Path> paths = Files.walk(Paths.get(ABSOLUTE_PATH))) {

            var collectionNames = paths
                    .filter(path -> Files.isRegularFile(path) && !path.toString().contains(README))
                    .map(path -> path.toString()
                            .replace(ABSOLUTE_PATH, "")
                            .replace(TXT, ""))
                    .collect(toSet());

            for (String collection : collectionNames) {

                var sampling = SAMPLING_PER_COLLECTION.entrySet()
                        .stream()
                        .filter(entry -> collection.contains(entry.getKey()))
                        .findFirst()
                        .orElse(DEFAULT_SAMPLING)
                        .getValue();

                var values = Files.lines(
                        new File(ABSOLUTE_PATH + collection + TXT).toPath())
                        .filter(__ -> ThreadLocalRandom.current().nextInt(sampling) == 0)
                        .map(id -> format("('" + id + "', 'PENDING', '%s')", collection))
                        .collect(toSet());

                var patched = Lists.partition(new ArrayList<>(values), MAX_PATCH_SIZE)
                        .stream()
                        .map(patchAsList -> String.join(",", patchAsList))
                        .collect(toList());

                try (Connection connection = DriverManager.getConnection(POSTGRES_CONN_URL, POSTGRES_USER, POSTGRES_PASS)) {
                    var statement = connection.createStatement();
                    for (String patch : patched) {
                        statement.executeUpdate(format("INSERT INTO tweets VALUES %s ON CONFLICT (id) DO NOTHING;", patch));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Tweets inserted into DB in " + sw.stop());
        }
    }
}
