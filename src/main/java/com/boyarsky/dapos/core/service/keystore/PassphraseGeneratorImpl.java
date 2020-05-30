package com.boyarsky.dapos.core.service.keystore;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class PassphraseGeneratorImpl implements PassphraseGenerator {
    private static final String DEFAULT_DICTIONARY_PATH = "dictionary.txt";
    private static final int DEFAULT_MIN_NUMBER_OF_WORDS = 10;
    private static final int DEFAULT_MAX_NUMBER_OF_WORDS = 15;

    private int minNumberOfWords;
    private int maxNumberOfWords;
    private final Random random;
    private URL dictionaryURL;
    //nosonar
    private volatile List<String> dictionary;

    public PassphraseGeneratorImpl(int minNumberOfWords, int maxNumberOfWords, List<String> dictionary) {
        this(minNumberOfWords, maxNumberOfWords);
        this.dictionary = dictionary;
    }

    public PassphraseGeneratorImpl(int minNumberOfWords, int maxNumberOfWords, URL dictionaryURL) {
        this(minNumberOfWords, maxNumberOfWords);
        this.dictionaryURL = dictionaryURL;
    }

    public int getMinNumberOfWords() {
        return minNumberOfWords;
    }

    public void setMinNumberOfWords(int minNumberOfWords) {
        this.minNumberOfWords = minNumberOfWords;
    }

    public int getMaxNumberOfWords() {
        return maxNumberOfWords;
    }

    public void setMaxNumberOfWords(int maxNumberOfWords) {
        this.maxNumberOfWords = maxNumberOfWords;
    }

    public List<String> getDictionary() {
        return dictionary;
    }

    public PassphraseGeneratorImpl(int minNumberOfWords, int maxNumberOfWords) {
        if (minNumberOfWords <= 0 || maxNumberOfWords <= 0) {
            throw new IllegalArgumentException("'minNumberOfWords' and 'maxNumberOfWords' should be positive");
        }

        if (minNumberOfWords > maxNumberOfWords) {
            throw new IllegalArgumentException("'minNumberOfWords' should be less or equal to 'maxNumberOfWords'");
        }
        this.minNumberOfWords = minNumberOfWords;
        this.maxNumberOfWords = maxNumberOfWords;
        this.dictionaryURL = getClass().getClassLoader().getResource(DEFAULT_DICTIONARY_PATH);
        this.random = new SecureRandom();
    }

    public PassphraseGeneratorImpl() {
        this(DEFAULT_MIN_NUMBER_OF_WORDS, DEFAULT_MAX_NUMBER_OF_WORDS);
    }

    @Override
    public String generate() {
        try {
            // load dictionary if not loaded yet
            // ensure thread-safe
            if (dictionary == null) {
                synchronized (this) {
                    if (dictionary == null) {
                        dictionary = loadDictionary();
                    }
                }
            }
            if (dictionary.size() < maxNumberOfWords) {
                throw new RuntimeException("Lack of words in dictionary: required - " + maxNumberOfWords + " but present - " + dictionary.size());
            }
            int numberOfWords = random.nextInt(maxNumberOfWords - minNumberOfWords) + minNumberOfWords;
            Set<String> passphraseWords = new LinkedHashSet<>();
            while (passphraseWords.size() != numberOfWords) {
                passphraseWords.add(dictionary.get(random.nextInt(dictionary.size())));
            }
            return String.join(" ", passphraseWords);
        }
        catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    protected List<String> loadDictionary() throws IOException, URISyntaxException {
            if (dictionaryURL == null) {
                throw new RuntimeException("Dictionary " + DEFAULT_DICTIONARY_PATH + " is not exist");
            }
        return Files.readAllLines(new File(dictionaryURL.toURI()).toPath());
    }
}