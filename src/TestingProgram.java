import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class TestingProgram {

    private FootballBoard footballBoard;
    private JTextArea txt;
    private int oldX;
    private int oldY;
    private List<Position> positions = new ArrayList<>();

    private void displayGUI() {
        JFrame frame = new JFrame("Testing program");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(650, 470));

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        footballBoard = new FootballBoard();
        footballBoard.setBackground(new Color(0, 150, 0));
        footballBoard.setLocation(0, 0);

        txt = new JTextArea();
        txt.setPreferredSize(new Dimension(100, 380));

        footballBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Position newPosition;
                if (e.getX() == oldX && e.getY() == oldY) {
                    footballBoard.drawPoint(e.getX(), e.getY());
                    newPosition =
                        new Point(
                            (double) (e.getX() - FootballBoard.TOTAL_SHIFT_X)
                            / FootballBoard.K,
                            (double) (e.getY() - FootballBoard.TOTAL_SHIFT_Y)
                            / FootballBoard.K);
                } else {
                    footballBoard.drawArea(e.getX(), e.getY(), oldX, oldY);
                    newPosition =
                        new ReachabilityArea(
                            (double) (oldX - FootballBoard.TOTAL_SHIFT_X)
                            / FootballBoard.K,
                            (double) (oldY - FootballBoard.TOTAL_SHIFT_Y)
                            / FootballBoard.K,
                            Math.sqrt(Math.pow(e.getX() - oldX, 2) + Math
                                .pow(e.getY() - oldY, 2)) / FootballBoard.K,
                            Math.atan2(e.getY() - oldY, e.getX() - oldX)
                            / Math.PI * 180);
                }
                positions.add(newPosition);
                txt.append(newPosition + "\n");
            }
        });

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> {
            positions.clear();
            footballBoard.clear();
            txt.setText("");
        });

        JButton btnCalc = new JButton("Calc");
        btnCalc.addActionListener(e -> {
            Formation formation = CmeansFormation.calculateFormation(positions);

            System.out.println(formation);
            System.out.println();

            double[]
                centers =
                StreamSupport.stream(formation.spliterator(), false)
                    .mapToDouble(FormationLine::getCenter)
                    .toArray();
            footballBoard.drawVerticalLines(centers);
        });
        contentPane.add(footballBoard);
        contentPane.add(txt);
        contentPane.add(btnCalc);
        contentPane.add(btnClear);

        frame.setContentPane(contentPane);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Runnable runnable = () -> new TestingProgram().displayGUI();
        EventQueue.invokeLater(runnable);
    }
}

class FootballBoard extends JPanel {

    private static final int START_X = 50;
    private static final int START_Y = 50;

    public static final int K = 4;
    private static final int FIELD_WIDTH = 105 * K;
    private static final int FIELD_HEIGHT = 68 * K;
    private static final int CENTER_X = FIELD_WIDTH / 2;
    private static final int CENTER_Y = FIELD_HEIGHT / 2;
    private static final int CIRCLE_RADIUS = 9 * K;
    private static final int BOX_WIDTH = (int) (16.5 * K);
    private static final int BOX_HEIGHT = 40 * K;

    private static final int WIDTH = FIELD_WIDTH + 2 * START_X;
    private static final int HEIGHT = FIELD_HEIGHT + 2 * START_Y;

    public static final int TOTAL_SHIFT_X = START_X + CENTER_X;
    public static final int TOTAL_SHIFT_Y = START_Y + CENTER_Y;

    private ArrayList<Position> positions = new ArrayList<>();
    private ArrayList<Integer> verticalLines = new ArrayList<>();

    public FootballBoard() {
        setOpaque(true);
    }

    void drawPoint(int x, int y) {
        positions.add(new Point(x - START_X, y - START_Y));
        repaint();
    }

    void drawArea(int x, int y, int oldX, int oldY) {
        positions.add(new ReachabilityArea(oldX - START_X, oldY - START_Y,
                                           Math.sqrt(
                                               Math.pow(x - oldX, 2) + Math
                                                   .pow(y - oldY, 2)),
                                           Math.atan2(y - oldY, x - oldX)
                                           / Math.PI * 180));
        repaint();
    }

    void drawVerticalLines(double[] lines) {
        verticalLines.clear();
        for (double line : lines) {
            verticalLines.add((int) (line * K) + CENTER_X);
        }
        repaint();
    }

    void clear() {
        positions.clear();
        verticalLines.clear();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.translate(START_X, START_Y);
        g.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        g.drawLine(CENTER_X, 0, CENTER_X, FIELD_HEIGHT);
        g.drawRect(0, CENTER_Y - BOX_HEIGHT / 2, BOX_WIDTH, BOX_HEIGHT);
        g.drawRect(FIELD_WIDTH - BOX_WIDTH, CENTER_Y - BOX_HEIGHT / 2,
                   BOX_WIDTH, BOX_HEIGHT);
        g.drawOval(CENTER_X - CIRCLE_RADIUS, CENTER_Y - CIRCLE_RADIUS,
                   2 * CIRCLE_RADIUS, 2 * CIRCLE_RADIUS);
        g.drawLine(0, 2 * CENTER_Y + 20, 2 * CENTER_X, 2 * CENTER_Y + 20);
        g.setColor(Color.RED);
        for (Position position : positions) {
            if (position instanceof ReachabilityArea) {
                ReachabilityArea area = (ReachabilityArea) position;
                g.drawOval((int) (area.getXCenter() - area.getRadius()),
                           (int) (area.getYCenter() - area.getRadius()),
                           (int) area.getRadius() * 2,
                           (int) area.getRadius() * 2);
                g.fillOval((int) area.getXCenter() - 2,
                           (int) area.getYCenter() - 2, 4, 4);
                g.drawLine((int) position.getX(), (int) position.getY(),
                           (int) area.getXCenter(), (int) area.getYCenter());
            }
            g.fillOval((int) position.getX() - 4, (int) position.getY() - 4, 8,
                       8);
            g.fillOval((int) position.getX() - 4, 2 * CENTER_Y + 16, 8, 8);
        }

        verticalLines.forEach(x -> g.fillRect(x, 0, 2, 2 * CENTER_Y));
        g.setColor(Color.BLUE);
    }
}