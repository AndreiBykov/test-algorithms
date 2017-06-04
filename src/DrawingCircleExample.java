import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class DrawingCircleExample {

    private DrawingBoard drawingBoard;
    private JTextArea txt;

    private static final int GAP = 5;

    private void displayGUI() {
        JFrame frame = new JFrame("Testing program");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(650, 500));

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        drawingBoard = new DrawingBoard();
        drawingBoard.setBackground(new Color(0, 150, 0));
        drawingBoard.setLocation(0, 0);

        txt = new JTextArea();
        txt.setPreferredSize(new Dimension(100, 380));

        drawingBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drawingBoard.setOldX(e.getX());
                drawingBoard.setOldY(e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawingBoard.setCurX(e.getX());
                drawingBoard.setCurY(e.getY());
                drawingBoard.drawPoint();
                txt.setText(drawingBoard.getPoints().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("\n")));
            }
        });

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> {
            drawingBoard.clearAll();
            txt.setText("");
        });

        JButton btnCalc = new JButton("Calc");
        btnCalc.addActionListener(e -> {

            List<Position> list = drawingBoard.getPoints();
            Formation formation = CmeansFinal.calculateFormation(list);

            System.out.println(formation);
            System.out.println();

            double[] centers = StreamSupport.stream(formation.spliterator(), false)
                    .mapToDouble(FormationLine::getxCenter)
                    .toArray();
            drawingBoard.drawVerticalLines(centers);
        });
        contentPane.add(drawingBoard);
        contentPane.add(txt);
        contentPane.add(btnCalc);
        contentPane.add(btnClear);

        frame.setContentPane(contentPane);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Runnable runnable = () -> new DrawingCircleExample().displayGUI();
        EventQueue.invokeLater(runnable);
    }
}

class DrawingBoard extends JPanel {

    private int oldX;
    private int oldY;
    private int curX;
    private int curY;

    private static final int START_X = 50;
    private static final int START_Y = 50;

    private static final int K = 4;
    private static final int FIELD_WIDTH = 105 * K;
    private static final int FIELD_HEIGHT = 68 * K;
    private static final int CENTER_X = FIELD_WIDTH / 2;
    private static final int CENTER_Y = FIELD_HEIGHT / 2;
    private static final int CIRCLE_RADIUS = 9 * K;
    private static final int BOX_WIDTH = (int) (16.5 * K);
    private static final int BOX_HEIGHT = 40 * K;

    private static final int WIDTH = FIELD_WIDTH + 2 * START_X;
    private static final int HEIGHT = FIELD_HEIGHT + 2 * START_Y;

    private ArrayList<Position> positions = new ArrayList<>();
    private ArrayList<Integer> verticalLines = new ArrayList<>();

    public DrawingBoard() {
        setOpaque(true);
    }

    void drawPoint() {

        if (oldX == curX && oldY == curY) {
            positions.add(new Point((curX - START_X - CENTER_X) / (double) K, (curY - START_Y - CENTER_Y) / (double) K));
        } else {
            positions.add(new ReachabilityArea((oldX - START_X - CENTER_X) / (double) K,
                    (oldY - START_Y - CENTER_Y) / (double) K,
                    Math.sqrt(Math.pow((curX - oldX) / (double) K, 2) + Math.pow((curY - oldY) / (double) K, 2)),
                    Math.atan2(curY - oldY, curX - oldX) / Math.PI * 180));
        }
        repaint();
    }

    void drawVerticalLines(double[] x) {
        verticalLines.clear();
        for (double v : x) {
            verticalLines.add((int) v);
        }
        repaint();
    }

    void clearAll() {
        positions.clear();
        verticalLines.clear();
        repaint();
    }

    public List<Position> getPoints() {
        ArrayList<Position> result = new ArrayList<>();
        result.addAll(positions);
        return result;
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
        g.drawRect(FIELD_WIDTH - BOX_WIDTH, CENTER_Y - BOX_HEIGHT / 2, BOX_WIDTH, BOX_HEIGHT);
        g.drawOval(CENTER_X - CIRCLE_RADIUS, CENTER_Y - CIRCLE_RADIUS, 2 * CIRCLE_RADIUS, 2 * CIRCLE_RADIUS);
        g.translate(CENTER_X, CENTER_Y);
        g.drawLine(-CENTER_X, CENTER_Y + 20, CENTER_X, CENTER_Y + 20);
        g.setColor(Color.RED);
        for (Position position : positions) {
            if (position instanceof ReachabilityArea) {
                ReachabilityArea area = (ReachabilityArea) position;
                g.drawOval((int) Math.round((area.getxCenter() - area.getRadius()) * K),
                        (int) Math.round((area.getyCenter() - area.getRadius()) * K),
                        (int) Math.round(area.getRadius() * 2 * K), (int) Math.round(area.getRadius() * 2 * K));
                g.fillOval((int) Math.round(area.getxCenter() * K) - 2, (int) Math.round(area.getyCenter() * K) - 2, 4, 4);
                g.drawLine((int) Math.round(position.getX() * K), (int) Math.round(position.getY() * K),
                        (int) Math.round(area.getxCenter() * K), (int) Math.round(area.getyCenter() * K));
            }
            g.fillOval((int) Math.round(position.getX() * K) - 4, (int) (Math.round(position.getY() * K) - 4), 8, 8);
            g.fillOval((int) Math.round(position.getX() * K) - 4, CENTER_Y + 16, 8, 8);
        }

        verticalLines.forEach(x -> g.fillRect(x * K, -CENTER_Y, 2, 2 * CENTER_Y));
        g.setColor(Color.BLUE);
    }

    public void setOldX(int oldX) {
        this.oldX = oldX;
    }

    public void setOldY(int oldY) {
        this.oldY = oldY;
    }

    public void setCurX(int curX) {
        this.curX = curX;
    }

    public void setCurY(int curY) {
        this.curY = curY;
    }
}