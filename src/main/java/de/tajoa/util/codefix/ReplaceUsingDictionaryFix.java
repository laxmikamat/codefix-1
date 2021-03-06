package de.tajoa.util.codefix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ReplaceUsingDictionaryFix implements Fix {

	private final String toBeReplaced;
	private final String wordBoundRegex;
	private final Charset charset;
	private final Dictionary dictionary;
	private final Collection<String> replacements;

	public ReplaceUsingDictionaryFix(	String toBeReplaced,
										Charset charset,
										Dictionary dictionary,
										String... replacements) {
		this.toBeReplaced = toBeReplaced;
		this.wordBoundRegex = toWordBoundRegex(toBeReplaced);
		this.charset = charset;
		this.dictionary = dictionary;
		this.replacements = Arrays.asList(replacements);
		System.out.println(this);
	}

	/**
	 * Only handles single replacements per word
	 */
	@Override
	public Pair<Path, byte[]> apply(Pair<Path, byte[]> inputFile) {
		String result = new String(inputFile.getValue(), this.charset);
		Pattern patternWord = Pattern.compile(toWordBoundRegex(toBeReplaced));
		Matcher matcherWord = patternWord.matcher(result);
		while (matcherWord.find()) {
			String wordWithMatch = matcherWord.group();
			result = handleMatchedWord(result, wordWithMatch);
		}
		return Pair.of(inputFile.getKey(), result.getBytes(charset));
	}

	private String handleMatchedWord(String input, String matchedWord) {
		List<String> possibleReplacements = this.possibleReplacements(matchedWord);
		if (possibleReplacements.size() == 1) {
			String replacement = possibleReplacements.iterator().next();
			System.out.println("Replacing: " + matchedWord + " -> " + replacement);
			return input.replaceFirst(wordBoundRegex, replacement);
		} else if (possibleReplacements.isEmpty()) {
			System.out.println("No replacement found for: " + matchedWord);
		} else {
			return handleAmbigiusReplacement(input, matchedWord, possibleReplacements);
		}
		return input;
	}

	private String handleAmbigiusReplacement(	String input,
												String matchedWord,
												List<String> possibleReplacements) {
		System.out.println("Multiple possible replacements found for: " + matchedWord + ", "
				+ possibleReplacements);
		System.out.println("Please enter the index of your choice. Type \"0\" to show the surrounding text:");
		try {
			int choice = this.getIntFromSysIn();
			choice = handleSourrundingChoice(input, matchedWord, choice);
			String replacement = possibleReplacements.get(choice - 1);
			System.out.println("Replacing: " + matchedWord + " -> " + replacement);
			return input.replaceFirst(wordBoundRegex, replacement);
		} catch (Exception e) {
			System.out.println("Invalid choice input, please retry!");
		}
		return handleAmbigiusReplacement(input, matchedWord, possibleReplacements);
	}

	private int handleSourrundingChoice(String input,
										String matchedWord,
										int choice) throws IOException {
		if (choice == 0) {
			int indexOfMatch = input.indexOf(matchedWord);
			String sd = input.substring(Math.max(0, indexOfMatch - 50),
										Math.min(input.length(), indexOfMatch + 50));
			System.out.println("Surrounding of Match: \n" + sd);
			System.out.println("Please enter the index of your choice:");
			choice = this.getIntFromSysIn();
		}
		return choice;
	}

	private static String toWordBoundRegex(String toBeReplaced) {
		return "\\w*" + toBeReplaced + "\\w*";
	}

	public List<String> possibleReplacements(String matchedWord) {
		return this.replacements.stream()
								.map(r -> matchedWord.replaceFirst(toBeReplaced, r))
								.filter(ty -> dictionary.contains(ty)
										|| (StringUtils.isAllLowerCase(ty.substring(1))
												&& dictionary.contains(ty.toLowerCase())))
								.collect(Collectors.toList());
	}

	private HashMap<String[], String> memFix;

	public int getIntFromSysIn() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return Integer.parseInt(br.readLine());
	}

	@Override
	public String toString() {
		return "ReplaceUsingDictionaryFix [toBeReplaced=" + toBeReplaced + ", charset=" + charset
				+ ", replacements=" + replacements + "]";
	}

}
