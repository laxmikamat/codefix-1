package de.tajoa.util.codefix;

import java.util.HashMap;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface Cmd extends Function<CommandLine, Integer> {
	
	public Options options();

	
	public default HashMap<Options, Cmd> toCmdMap(){
		HashMap<Options, Cmd> map = new HashMap<>();
		map.put(this.options(), this);
		return map;
	}
}
