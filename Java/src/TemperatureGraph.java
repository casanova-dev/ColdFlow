package coldflow;

import javax.swing.*;
import java.awt.*;

public class TemperatureGraph extends JPanel {

    private final CircularBuffer buffer;

    public TemperatureGraph(CircularBuffer buffer) {
        this.buffer = buffer;
        setPreferredSize(new Dimension(900, 500));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("ColdFlow - Freezer Temperature", 30, 35);

        if (buffer.size() == 0) {
            g2.drawString("No temperature data available", 30, 70);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int left = 60;
        int right = 30;
        int top = 70;
        int bottom = 50;

        int graphWidth = width - left - right;
        int graphHeight = height - top - bottom;

        // Draw axes
        g2.setColor(Color.GRAY);

        g2.drawLine(
                left,
                top,
                left,
                height - bottom
        );

        g2.drawLine(
                left,
                height - bottom,
                width - right,
                height - bottom
        );

        // Find temperature range
        double minTemp = Double.MAX_VALUE;
        double maxTemp = -Double.MAX_VALUE;

        for (int i = 0; i < buffer.size(); i++) {

            double temp =
                    buffer.get(i).getTemperature();

            minTemp = Math.min(minTemp, temp);
            maxTemp = Math.max(maxTemp, temp);
        }

        // Add some space around the graph
        double range = maxTemp - minTemp;

        if (range < 1) {
            range = 1;
        }

        minTemp -= 1;
        maxTemp += 1;

        // Draw temperature labels
        g2.setFont(new Font("Arial", Font.PLAIN, 12));

        g2.drawString(
                String.format("%.1f°C", maxTemp),
                10,
                top + 5
        );

        g2.drawString(
                String.format("%.1f°C", minTemp),
                10,
                height - bottom
        );

        // Draw graph line
        g2.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(3));

        for (int i = 0; i < buffer.size() - 1; i++) {

            double temp1 =
                    buffer.get(i).getTemperature();

            double temp2 =
                    buffer.get(i + 1).getTemperature();

            int x1 = left +
                    (i * graphWidth / Math.max(1, buffer.size() - 1));

            int x2 = left +
                    ((i + 1) * graphWidth /
                    Math.max(1, buffer.size() - 1));

            int y1 = height - bottom -
                    (int) ((temp1 - minTemp) /
                    (maxTemp - minTemp) * graphHeight);

            int y2 = height - bottom -
                    (int) ((temp2 - minTemp) /
                    (maxTemp - minTemp) * graphHeight);

            g2.drawLine(x1, y1, x2, y2);
        }

        // Draw current temperature
        double currentTemperature =
                buffer.get(buffer.size() - 1)
                      .getTemperature();

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));

        g2.drawString(
                String.format(
                        "Current Temperature: %.2f°C",
                        currentTemperature
                ),
                left,
                height - 15
        );
    }

    // Open the graph window
    public static void showGraph(CircularBuffer buffer) {

        JFrame frame =
                new JFrame("ColdFlow Temperature Monitor");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.add(new TemperatureGraph(buffer));

        frame.pack();

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}