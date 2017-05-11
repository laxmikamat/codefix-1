package de.tajoa.util.codefix;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ReplaceUsingDictionaryCmd implements Cmd {

	private static final String RS = "rs";
	private static final String TBR = "tbr";
	private static final String D = "d";
	private static final String RUD = "rud";

	@Override
	public Options options() {
		Options options = new Options();
		Option rudOption = new Option(RUD, "replaceUsingDictionary", true,
				"Fix files by replacing the string given by the parameter -tbr "
						+ "with on of the relpacements given by the parameter -rs "
						+ "if the word created by the replacement can be found in the dictionary given by the parameter -d."
						+ "Format: <source-path> <target-path> <encoding>. UTF-8 is used as default encoding");
		rudOption.setArgs(3);
		rudOption.setOptionalArg(true);
		options.addOption(rudOption);
		Option dic = new Option(D, "dictionary", true,
				"Dictionary file to be used. The file must separate words "
						+ "by line breaks. Optionally followed by the encoding in the format <filepath> <encoding>."
						+ " UTF-8 is used as default encoding");
		dic.setArgs(2);
		dic.setOptionalArg(true);
		options.addOption(dic);
		options.addOption(new Option(TBR, "toBeReplaced", true, "String that should be replaced"));
		Option reOption = new Option(RS, "replacements", true, "Possible replacements as whitespace separated list");
		reOption.setArgName("replacements");
		reOption.setArgs(100);
		reOption.setOptionalArg(true);
		options.addOption(reOption);
		return options;
	}

	@Override
	public Integer apply(CommandLine cmd) {
		String toBeReplaced = cmd.getOptionValue(TBR);
		String[] replacements = cmd.getOptionValues(RS);
		Path dicFilePath = Paths.get(cmd.getOptionValue(D));
		Dictionary dictionary = new Dictionary();
		Charset charsetOfDic = cmd.getOptionValues(D).length >= 2 ? Charset.forName(cmd.getOptionValues(D)[1])
				: StandardCharsets.UTF_8;
		dictionary.addDictionaryFile(dicFilePath, charsetOfDic);
		Path fixPath = Paths.get(cmd.getOptionValue(RUD));
		Charset charsetOfFilesToFix = cmd.getOptionValues(RUD).length >= 3
				? Charset.forName(cmd.getOptionValues(RUD)[2]) : StandardCharsets.UTF_8;
		ReplaceUsingDictionaryFix fix = new ReplaceUsingDictionaryFix(toBeReplaced, charsetOfFilesToFix, dictionary, replacements);
		Optional<Path> target = cmd.getOptionValues(RUD).length >= 2
				? Optional.of(Paths.get(cmd.getOptionValues(RUD)[1])) : Optional.empty();
		Optional<String> suffix = cmd.hasOption(CodeFix.S) ? Optional.of(cmd.getOptionValue(CodeFix.S))
				: Optional.empty();
		fix.fix(fixPath, target, suffix);
		return 1;
	}

}
