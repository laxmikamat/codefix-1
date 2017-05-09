package de.tajoa.util.codefix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

public interface Fix extends Function<Pair<Path, byte[]>, Pair<Path, byte[]>> {

	default void fix(Path path, Optional<Path> target, String suffix) throws Exception {
		Files.walk(path).filter(p -> p.toFile().isFile() && p.toFile().getName().endsWith(suffix)).parallel()
				.map(Fix::readFile).map(this).forEach(pAb -> {
					Fix.writeFile(Fix.createPath(path, pAb.getKey(), target), pAb.getValue());
				});
	}

	public static Path createPath(Path originalBase, Path file, Optional<Path> targetBase) {
		if (targetBase.isPresent()) {
			Path p = targetBase.get().resolve(originalBase.relativize(file));
			File pathFile = p.toFile();
			if (!pathFile.getParentFile().exists())
				pathFile.getParentFile().mkdirs();
			return p;
		}
		return file;
	}

	public static Pair<Path, byte[]> readFile(Path path) {
		try {
			System.out.println("Reading file: " + path.toFile().getAbsolutePath());
			return Pair.of(path, Files.readAllBytes(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Pair.of(Paths.get(""), new byte[0]);
	}

	public static void writeFile(Path path, byte[] content) {
		try {
			System.out.println("Writing file: " + path.toFile().getAbsolutePath());
			Files.write(path, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
