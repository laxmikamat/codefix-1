package de.tajoa.util.codefix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

public interface Fix extends Function<Pair<Path, byte[]>, Pair<Path, byte[]>> {

	default void fix(Path path, Optional<Path> target, Optional<String> suffix) {
		try {
			Files.walk(path)
					.filter(p -> p.toFile().isFile()
							&& (!suffix.isPresent() || p.toFile().getName().endsWith(suffix.get())))
					.parallel().map(Fix::readFile).map(this).forEach(pAb -> {
						Fix.writeFile(Fix.createPath(path, pAb.getKey(), target), pAb.getValue());
					});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
			throw new RuntimeException(e);
		}
	}

	public static void writeFile(Path path, byte[] content) {
		try {
			System.out.println("Writing file: " + path.toFile().getAbsolutePath());
			Files.write(path, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
