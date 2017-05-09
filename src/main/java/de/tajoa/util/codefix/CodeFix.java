package de.tajoa.util.codefix;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

public class CodeFix {

	public static void main(String[] args) throws Exception {
		Dictionary dictionary = new Dictionary();
		dictionary.addDictionaryFile(Paths.get("german.dic"), StandardCharsets.ISO_8859_1, "ä", "ü", "ö", "Ü");
		ReplaceWordFix replaceFix = new ReplaceWordFix("�", StandardCharsets.UTF_8, dictionary, "ä", "ü", "ö", "Ü");
		replaceFix.fix(Paths.get("./test"), Optional.of(Paths.get("./target/test")), "java");
	}

}
