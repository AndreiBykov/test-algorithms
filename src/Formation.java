import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Formation implements Iterable<FormationLine> {
    private final List<FormationLine> formationLines = new ArrayList<>();

    private Formation() {
    }

    public int numberOfLines() {
        return formationLines.size();
    }

    public FormationLine getLine(int index) {
        return formationLines.get(index);
    }

    public static Formation getFormation(List<Position> positions, double[][] fuzzyPartitionMatrix, double[] newLines) {
        Formation formation = new Formation();
        for (int i = 0; i < newLines.length; i++) {
            FormationLine line = new FormationLine(newLines[i]);
            for (int j = 0; j < positions.size(); j++) {
                if (Double.compare(fuzzyPartitionMatrix[j][i], 0) > 0) {
                    line.addFuzzyPosition(new FuzzyPosition(positions.get(j), fuzzyPartitionMatrix[j][i]));
                }
            }
            formation.formationLines.add(line);
        }
        return formation;
    }

    @Override
    public Iterator<FormationLine> iterator() {
        return formationLines.iterator();
    }

    @Override
    public String toString() {
        return formationLines.stream()
                .map(FormationLine::toString)
                .collect(Collectors.joining("\n"));
    }
}
