package de.tajoa.util.codefix;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class Dictionary {

	private final Set<String> dictionary;

	public Dictionary() {
		this.dictionary = new HashSet<>();
	}

	public void addDictionaryFile(Path dicFile, Charset charset, String... filter) {
		try (Stream<String> stream = Files.lines(dicFile, charset)) {
			boolean filterEmtpy = filter.length != 0;
			stream.filter(w -> filterEmtpy || StringUtils.containsAny(w, filter)).forEach(w -> this.dictionary.add(w));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int size() {
		return this.dictionary.size();
	}

	public boolean contains(String tryS) {
		return this.dictionary.contains(tryS);
	}
}
