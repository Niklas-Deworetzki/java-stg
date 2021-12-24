package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class grouping all {@link Alternative Alternatives} defined for a {@link CaseExpression}.
 * <p>
 * This class manages the given {@link AlgebraicAlternative} or {@link PrimitiveAlternative}
 * instances, as well as a selected {@link DefaultAlternative}.
 */
public final class Alternatives extends Node implements Iterable<Alternative> {
    public final List<Alternative> alternatives;
    public final DefaultAlternative defaultAlternative;

    public Alternatives(Position position, List<Alternative> alternatives, DefaultAlternative defaultAlternative) {
        super(position);
        this.alternatives = alternatives;
        this.defaultAlternative = defaultAlternative;
    }

    @Override
    public Iterator<Alternative> iterator() {
        List<Alternative> elements = new ArrayList<>(alternatives);
        elements.add(defaultAlternative);
        return elements.iterator();
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return formatTree("Alternatives",
                formatMember("alternatives", alternatives),
                formatMember("default", defaultAlternative.toString()));
    }
}
