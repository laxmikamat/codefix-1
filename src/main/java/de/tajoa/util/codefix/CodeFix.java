package de.tajoa.util.codefix;

import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class CodeFix {

	public static final String S = "s";
	public static final String X = "x";

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Pair<Options, Map<Collection<Option>, Cmd>> optionsAndCommands = getOptionsAndCmds();
		Options allOptions = optionsAndCommands.getKey();
		getSharedOptions().getOptions().forEach(o -> allOptions.addOption(o));
		allOptions.addOption("x", "Trace option");
		try {
			executeAndHandleError(parser.parse(allOptions, args), optionsAndCommands);
		} catch (ParseException e) {
			System.out.println("Input Error: " + e.getMessage() + " Help: ");
			printHelp(optionsAndCommands);
		}
	}

	private static void executeAndHandleError(CommandLine currentCmd,
			Pair<Options, Map<Collection<Option>, Cmd>> optionsAndCommands) throws ParseException {
		try {
			execute(currentCmd, getPossibleCmds(optionsAndCommands, Arrays.asList(currentCmd.getOptions())));
		} catch (RuntimeException e) {
			Throwable root = ExceptionUtils.getRootCause(e);
			root = root == null ? e : root;
			if (root instanceof MalformedInputException) {
				System.out.println("Given file encoding seems to be wrong");
			} else if (root instanceof UnsupportedCharsetException) {
				System.out.println("Error: "+ e.getClass().getSimpleName() +" "+ e.getMessage());
				System.out.println("Available charsets are:");
				Charset.availableCharsets().keySet().forEach(ch -> System.out.println(ch));
			} else {
				System.out.println("Error: "+ e.getClass().getSimpleName() +" "+ e.getMessage());
			}
			if (currentCmd.hasOption(X))
				e.printStackTrace();
		}
	}

	private static void printHelp(Pair<Options, Map<Collection<Option>, Cmd>> optionsAndCommands) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("CodeFix", optionsAndCommands.getKey());
	}

	private static Options getSharedOptions() {
		Options options = new Options();
		options.addOption(S, true, "Suffix of files that should be fixed");
		return options;
	}

	/**
	 * Register all impl Cmds
	 */
	private static Collection<Cmd> getCmds() {
		return Arrays.asList(new ReplaceUsingDictionaryCmd());
	}

	private static void execute(CommandLine currentCmd, List<Entry<Collection<Option>, Cmd>> possibleActions)
			throws ParseException {
		if (possibleActions.size() != 1)
			throw new ParseException("Combination of args does not match any fix action!");
		Entry<Collection<Option>, Cmd> action = possibleActions.get(0);
		action.getValue().apply(currentCmd);

	}

	private static List<Entry<Collection<Option>, Cmd>> getPossibleCmds(
			Pair<Options, Map<Collection<Option>, Cmd>> optionsAndCommands, Collection<Option> options) {
		List<Entry<Collection<Option>, Cmd>> possibleActions = optionsAndCommands.getValue().entrySet().stream()
				.filter(cmdAndOptions -> options.containsAll(cmdAndOptions.getKey())).collect(Collectors.toList());
		return possibleActions;
	}

	private static Pair<Options, Map<Collection<Option>, Cmd>> getOptionsAndCmds() {
		Collection<Cmd> cmds = getCmds();
		Options options = new Options();
		Map<Collection<Option>, Cmd> optionsToCmd = new HashMap<>();
		for (Cmd cmd : cmds) {
			cmd.options().getOptions().stream().forEach(o -> options.addOption(o));
			optionsToCmd.put(cmd.options().getOptions(), cmd);
		}
		return Pair.of(options, optionsToCmd);
	}

}
