package org.acme.gen;

import java.nio.file.Path;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.utils.SourceRoot;

@Deprecated(forRemoval = true)
public class StatusGen {
	private final Path path;
    private final Name name;
    
    public StatusGen(Path path, Name name) {
		super();
		this.path = path;
		this.name = name;
	}

	public void create() {
		var cu = new CompilationUnit(name.getQualifier().map(Name::toString).orElse(""));
        cu.addClass(name.getIdentifier(), Keyword.PUBLIC);
        cu.setStorage(path.resolve(String.format("%s/%s.java",
				name.getQualifier().map(Name::toString).map(n -> n.replace(".", "/")).orElse(""),
				name.getIdentifier())));
        SourceRoot dest = new SourceRoot(path);
        dest.add(cu);
        dest.saveAll();
    }
}
