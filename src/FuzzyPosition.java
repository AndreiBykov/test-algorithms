public class FuzzyPosition {
    final private Position position;
    final private double fuzziness;

    public FuzzyPosition(Position position, double fuzziness) {
        this.position = position;
        this.fuzziness = fuzziness;
    }

    public Position getPosition() {
        return position;
    }

    public double getFuzziness() {
        return fuzziness;
    }

    @Override
    public String toString() {
        return position + String.format(" / %.1f", fuzziness);
    }
}
