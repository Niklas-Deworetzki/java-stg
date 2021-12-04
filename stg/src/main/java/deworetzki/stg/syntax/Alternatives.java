package deworetzki.stg.syntax;

import deworetzki.parse.Position;
import deworetzki.stg.visitor.Visitor;

import java.util.Iterator;
import java.util.List;

public final class Alternatives extends Node implements Iterable<Alternative> {
    public final List<Alternative> alternatives; // TODO: Enforce homogeneity
    public final DefaultAlternative defaultAlternative;

    public Alternatives(Position position, List<Alternative> alternatives, DefaultAlternative defaultAlternative) {
        super(position);
        this.alternatives = alternatives;
        this.defaultAlternative = defaultAlternative;
    }

    @Override
    public Iterator<Alternative> iterator() {
        return alternatives.iterator();
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
