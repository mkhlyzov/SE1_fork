package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import engine.GameSimulator;
import logic.GameHelper;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMapNode;

public class SwingView extends JFrame {

    private GamePanel gamePanel;

    private JLabel statusLabel;

    public SwingView() {

        super("SE1 MVC Game View");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        add(topPanel, BorderLayout.NORTH);

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> {
            dispose();
            new Thread(() -> {
                GameSimulator.multiPlayer(new String[0]);
            }).start();
        });
        newGameButton.setVisible(true);
        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        topPanel.add(newGameButton);

        statusLabel = new JLabel(
                "Game started",
                SwingConstants.CENTER);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        topPanel.add(statusLabel);

        /*
         * Genau wie im Minesweeper:
         * Ein GamePanel wird erstellt und
         * in BorderLayout.CENTER eingefügt.
         */
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        /*
         * Fenster ist veränderbar.
         * Dadurch kann GamePanel seine Zellen skalieren.
         */
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void render(GameHelper gameHelper) {

        SwingUtilities.invokeLater(() -> {

            /*
             * Aktuelle Karte an GamePanel übergeben.
             */
            int cols = gameHelper.getMaxX() + 1;
            int rows = gameHelper.getMaxY() + 1;

            if (rows == 10 && cols == 10) {
                gamePanel.setPreferredSize(new Dimension(500, 500));
            } else if (rows == 5 && cols == 20) {
                gamePanel.setPreferredSize(new Dimension(1000, 250));
            }
            pack();
            setResizable(false);
            gamePanel.updateMap(gameHelper);

            /*
             * Genau wie im Minesweeper:
             * repaint() ruft paintComponent() erneut auf.
             */
            gamePanel.repaint();
        });
    }

    public void printGameResult(boolean won) {

        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(
                    won
                            ? "🏆 Du hast gewonnen!"
                            : "💀 Du hast verloren.");
        });
    }
}

/**
 * GamePanel zeichnet die Karte.
 *
 * Gleicher Aufbau wie GamePanel im Minesweeper.
 *
 * Die Zellengröße wird dynamisch anhand
 * der aktuellen Panelgröße berechnet.
 */
class GamePanel extends JPanel {

    private String[][] board;

    private int rows;
    private int cols;

    public GamePanel() {

        setBackground(Color.LIGHT_GRAY);
    }

    /**
     * Aktuellen Spielzustand in ein Board übertragen.
     */
    public void updateMap(GameHelper gameHelper) {

        int maxX = gameHelper.getMaxX();
        int maxY = gameHelper.getMaxY();

        rows = maxY + 1;
        cols = maxX + 1;

        board = new String[rows][cols];

        for (FullMapNode node : gameHelper.getMap().getMapNodes()) {

            int x = node.getX();
            int y = node.getY();

            board[y][x] = getSymbolForNode(node, gameHelper);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        /*
         * Solange noch keine Karte vorhanden ist,
         * gibt es nichts zu zeichnen.
         */
        if (board == null) {
            return;
        }

        /*
         * Genau wie im Minesweeper.
         */
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;
        int fontSize = (int) (Math.min(cellWidth, cellHeight));
        /*
         * Jede Zelle zeichnen.
         */
        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                int x = col * cellWidth;
                int y = row * cellHeight;

                String symbol = board[row][col];

                if (symbol == null) {
                    continue;
                }

                /*
                 * Genau wie beim Text im Minesweeper:
                 *
                 * Schriftgröße basiert auf
                 * cellWidth und cellHeight.
                 */
                g.setFont(
                        new Font(
                                "Segoe UI Emoji",
                                Font.PLAIN,
                                fontSize));

                FontMetrics fm = g.getFontMetrics();

                int textWidth = fm.stringWidth(symbol);

                int textHeight = fm.getAscent();

                /*
                 * Symbol innerhalb der Zelle zentrieren.
                 *
                 * Gleiche Berechnung wie im Minesweeper.
                 */
                int textX = x + (cellWidth - textWidth) / 2;

                int textY = y
                        + (cellHeight + textHeight) / 2
                        - 2;

                g.drawString(
                        symbol,
                        textX,
                        textY);
            }
        }
    }

    /**
     * Symbol für ein Feld bestimmen.
     */
    private String getSymbolForNode(
            FullMapNode node,
            GameHelper gameHelper) {

        /*
         * Spielerposition.
         */
        EPlayerPositionState position = node.getPlayerPositionState();

        switch (position) {

            case MyPlayerPosition:
                return "🙂";

            case EnemyPlayerPosition:
                return "😈";

            case BothPlayerPosition:
                return "⚔️";

            default:
                break;
        }

        /*
         * Burg.
         */
        EFortState fortState = node.getFortState();

        switch (fortState) {

            case MyFortPresent:
                return "🏰";

            case EnemyFortPresent:
                return "🏯";

            default:
                break;
        }

        /*
         * Schatz.
         */
        if (gameHelper.goldWasHere(node)) {

            ETreasureState treasureState = node.getTreasureState();

            switch (treasureState) {

                case MyTreasureIsPresent:
                    return "💰";

                case NoOrUnknownTreasureState:
                    return "🟡";

                default:
                    break;
            }
        }

        /*
         * Terrain.
         */
        ETerrain terrain = node.getTerrain();

        if (gameHelper.isObserved(node)) {

            return switch (terrain) {

                case Grass -> "🟢";

                case Water -> "🟦";

                case Mountain -> "🟤";
            };
        }

        return switch (terrain) {

            case Grass -> "🟩";

            case Water -> "🟦";

            case Mountain -> "🟫";
        };
    }
}