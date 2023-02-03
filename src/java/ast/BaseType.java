package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public enum BaseType implements Type {
    INT, CHAR, VOID, UNKNOWN, NONE;

    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
