package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public sealed abstract class Node permits Alternative, Alternatives, Bind, Constructor, Expression, LambdaForm, Program, Variable {
    public final Position position;

    public Node(Position position) {
        this.position = position;
    }

    public abstract <R> R accept(Visitor<R> visitor);

    /**
     * Creates a String representation for a list as member variable.
     *
     * @param name The name of the variable used in the representation.
     * @param list The list to create a string representation of.
     */
    protected static String formatMember(String name, List<?> list) {
        if (list.size() == 0) {
            return String.format("%s = []", name);
        } else {
            String listString = list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",\n"));

            return String.format("%s = [\n%s\n]", name, indent(listString));
        }
    }

    /**
     * Creates a String representation for a member variable.
     *
     * @param name  The name of the variable used in the representation.
     * @param value The value of the member variable.
     */
    protected static String formatMember(String name, String value) {
        return String.format("%s = %s", name, value);
    }

    /**
     * Creates a String representation of a {@link Node} node.
     * This method is used in the concrete subclasses of {@link Node}.
     *
     * @param name     The name of the concrete subclass.
     * @param children The children present in a subclass' instance.
     */
    protected static String formatTree(String name, Object... children) {
        if (children.length == 0) {
            return String.format("%s()", name);
        } else {
            String memberString = Arrays.stream(children)
                    .map(String::valueOf)
                    .collect(Collectors.joining(",\n"));

            return String.format("%s(\n%s\n)", name, indent(memberString));
        }
    }

    /**
     * Prepends a TAB-character (\t) in front of every line of the given String.
     */
    private static String indent(String str) {
        return str.lines()
                .map(line -> "\t" + line)
                .collect(Collectors.joining("\n"));
    }
}
