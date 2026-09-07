package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import controller.GameController;
import logic.GameHelper;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.PlayerState;
import model.EGameMode;
import model.GameSettings;
import model.StrategyType;

public class SwingView extends JFrame implements IView {

  private GameController controller;

  private GamePanel gamePanel;

  private InfoPanel infoPanel;

  private JPanel settingsPanel = new JPanel();

  JPanel topPanel = new JPanel();

  private JPanel buttonsPanel = new JPanel();

  public SwingView() {

    super("SE1 MVC Game View");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    add(topPanel, BorderLayout.NORTH);

    topPanel.add(buttonsPanel);

    addnewGameButton();

    addSettingsButton();

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

  public void setController(GameController controller) {
    this.controller = controller;
  }

  private void addGameInfo() {

    if (infoPanel != null) {
      topPanel.remove(infoPanel);
    }

    infoPanel = new InfoPanel();

    topPanel.add(infoPanel);

    topPanel.revalidate();
    topPanel.repaint();
  }

  private void addSettingsButton() {

    JButton settingsButton = new JButton("Settings");

    settingsButton.setPreferredSize(new Dimension(180, 50));
    settingsButton.setFont(new Font("SansSerif", Font.BOLD, 20));
    settingsButton.setAlignmentX(CENTER_ALIGNMENT);

    settingsButton.addActionListener(
        e -> openSettingsDialog());

    buttonsPanel.add(settingsButton);
  }

  private void openSettingsDialog() {

    JDialog dialog = new JDialog(this, "Game Settings", true);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

    JPanel settingsPanel = new JPanel(new GridLayout(8, 2, 10, 10));

    JComboBox<EGameMode> gameModeBox = new JComboBox<>(EGameMode.values());

    JComboBox<StrategyType> playerStrategyBox = new JComboBox<>(StrategyType.values());

    JComboBox<StrategyType> enemyStrategyBox = new JComboBox<>(StrategyType.values());

    JTextField serverURLField = new JTextField();

    JTextField gameIdField = new JTextField();

    JTextField studentIdField = new JTextField("Fake1");

    JTextField delayField = new JTextField("100");

    JTextField fixedSeedField = new JTextField();

    settingsPanel.add(new JLabel("Game Mode:"));
    settingsPanel.add(gameModeBox);

    settingsPanel.add(new JLabel("Player Strategy:"));
    settingsPanel.add(playerStrategyBox);

    settingsPanel.add(new JLabel("Enemy Strategy:"));
    settingsPanel.add(enemyStrategyBox);

    settingsPanel.add(new JLabel("Server URL:"));
    settingsPanel.add(serverURLField);

    settingsPanel.add(new JLabel("Game ID:"));
    settingsPanel.add(gameIdField);

    settingsPanel.add(new JLabel("Student ID:"));
    settingsPanel.add(studentIdField);

    settingsPanel.add(new JLabel("Delay:"));
    settingsPanel.add(delayField);

    settingsPanel.add(new JLabel("Fixed Seed:"));
    settingsPanel.add(fixedSeedField);

    JPanel buttonPanel = new JPanel();

    JButton cancelButton = new JButton("Cancel");

    JButton applyButton = new JButton("Apply");

    cancelButton.addActionListener(
        e -> dialog.dispose());

    applyButton.addActionListener(
        e -> {

          int delay = Integer.parseInt(delayField.getText());

          Long fixedSeed = null;

          if (!fixedSeedField.getText().isBlank()) {
            fixedSeed = Long.parseLong(fixedSeedField.getText());
          }

          GameSettings settings = new GameSettings(
              studentIdField.getText(),
              (EGameMode) gameModeBox.getSelectedItem(),
              (StrategyType) playerStrategyBox.getSelectedItem(),
              (StrategyType) enemyStrategyBox.getSelectedItem(),
              serverURLField.getText(),
              gameIdField.getText(),
              delay,
              fixedSeed);

          controller.updateSettings(settings);

          dialog.dispose();
        });

    buttonPanel.add(cancelButton);
    buttonPanel.add(applyButton);

    mainPanel.add(settingsPanel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    dialog.add(mainPanel);

    dialog.setSize(550, 420);
    dialog.setLocationRelativeTo(this);
    dialog.setResizable(false);
    dialog.setVisible(true);
  }

  private void addnewGameButton() {
    JButton newGameButton = new JButton("New Game");
    newGameButton.setPreferredSize(new Dimension(180, 50));
    newGameButton.setFont(new Font("SansSerif", Font.BOLD, 20));
    newGameButton.addActionListener(
        e -> {
          new Thread(
              () -> {
                controller.startNewGame();
              })
              .start();
        });
    newGameButton.setVisible(true);
    newGameButton.setAlignmentX(CENTER_ALIGNMENT);
    buttonsPanel.add(newGameButton);
  }

  @Override
  public void render(GameHelper gameHelper) {

    // SwingUtilities.invokeLater(
    // () -> {

    // /*
    // * Aktuelle Karte an GamePanel übergeben.
    // */
    // addGameInfo();
    // int cols = gameHelper.getMaxX() + 1;
    // int rows = gameHelper.getMaxY() + 1;

    // if (rows == 10 && cols == 10) {
    // gamePanel.setPreferredSize(new Dimension(500, 500));
    // } else if (rows == 5 && cols == 20) {
    // gamePanel.setPreferredSize(new Dimension(1000, 250));
    // }
    // setResizable(false);
    // gamePanel.updateMap(gameHelper);

    // /*
    // * Genau wie im Minesweeper:
    // * repaint() ruft paintComponent() erneut auf.
    // */
    // gamePanel.revalidate();
    // gamePanel.repaint();
    // pack();
    // });
    try {
      SwingUtilities.invokeAndWait(
          () -> {

            addGameInfo();

            int cols = gameHelper.getMaxX() + 1;
            int rows = gameHelper.getMaxY() + 1;

            if (rows == 10 && cols == 10) {
              gamePanel.setPreferredSize(new Dimension(500, 500));
            } else if (rows == 5 && cols == 20) {
              gamePanel.setPreferredSize(new Dimension(1000, 250));
            }

            setResizable(false);

            gamePanel.updateMap(gameHelper);

            gamePanel.revalidate();
            gamePanel.repaint();

            pack();
          });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void printGameResult(boolean won) {

    SwingUtilities.invokeLater(
        () -> {

          if (won) {
            infoPanel.updatePlayerStateWon();
          } else {
            infoPanel.updatePlayerStateLost();
          }
        });
  }
}

/**
 * GamePanel zeichnet die Karte.
 *
 * <p>
 * Gleicher Aufbau wie GamePanel im Minesweeper.
 *
 * <p>
 * Die Zellengröße wird dynamisch anhand der aktuellen Panelgröße berechnet.
 */
class GamePanel extends JPanel {

  private String[][] board;

  private int rows;
  private int cols;

  public GamePanel() {

    setBackground(Color.LIGHT_GRAY);
  }

  /** Aktuellen Spielzustand in ein Board übertragen. */
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
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, fontSize));

        FontMetrics fm = g.getFontMetrics();

        int textWidth = fm.stringWidth(symbol);

        int textHeight = fm.getAscent();

        /*
         * Symbol innerhalb der Zelle zentrieren.
         *
         * Gleiche Berechnung wie im Minesweeper.
         */
        int textX = x + (cellWidth - textWidth) / 2;

        int textY = y + (cellHeight + textHeight) / 2 - 2;

        g.drawString(symbol, textX, textY);
      }
    }
  }

  /** Symbol für ein Feld bestimmen. */
  private String getSymbolForNode(FullMapNode node, GameHelper gameHelper) {

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

class InfoPanel extends JPanel {
  PlayerState state;

  private JLabel statusLabel;

  private JPanel leftPanel;
  private JPanel rightPanel;

  public InfoPanel() {

    setLayout(new BorderLayout());

    JLabel myText = new JLabel("Mein Spieler");
    JLabel enemyText = new JLabel("Gegner");

    myText.setFont(new Font("SansSerif", Font.BOLD, 16));
    enemyText.setFont(new Font("SansSerif", Font.BOLD, 16));

    JLabel myPlayerLabel = new JLabel("🙂");
    JLabel enemyPlayerLabel = new JLabel("😈");

    myPlayerLabel.setFont(
        new Font("Segoe UI Emoji", Font.PLAIN, 40));

    enemyPlayerLabel.setFont(
        new Font("Segoe UI Emoji", Font.PLAIN, 40));

    statusLabel = new JLabel("Game started", SwingConstants.CENTER);

    statusLabel.setFont(
        new Font("SansSerif", Font.BOLD, 20));

    statusLabel.setBorder(
        BorderFactory.createEmptyBorder(0, 20, 0, 0));

    leftPanel = new JPanel();

    leftPanel.add(myText);
    leftPanel.add(myPlayerLabel);

    leftPanel.setPreferredSize(
        new Dimension(170, 60));

    rightPanel = new JPanel();

    rightPanel.add(enemyPlayerLabel);
    rightPanel.add(enemyText);

    rightPanel.setPreferredSize(
        new Dimension(170, 60));

    add(leftPanel, BorderLayout.WEST);
    add(statusLabel, BorderLayout.CENTER);
    add(rightPanel, BorderLayout.EAST);
  }

  public void updatePlayerState(PlayerState state) {
    this.state = state;

    if (state == null) {

      statusLabel.setText("Game started");

      leftPanel.setBorder(null);
      rightPanel.setBorder(null);

      repaint();

      return;

    }
    switch (state.getState()) {

      case Won:
        statusLabel.setText("Won!");

        leftPanel.setBorder(
            BorderFactory.createLineBorder(Color.GREEN, 4));

        rightPanel.setBorder(null);

        break;

      case Lost:
        statusLabel.setText("Lost!");

        rightPanel.setBorder(
            BorderFactory.createLineBorder(Color.GREEN, 4));

        leftPanel.setBorder(null);

        break;

      default:
        statusLabel.setText("Game started");
        break;
    }

    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
  }

  public void updatePlayerStateWon() {

    statusLabel.setText("Won!");

    leftPanel.setBorder(
        BorderFactory.createLineBorder(Color.GREEN, 4));

    rightPanel.setBorder(null);
  }

  public void updatePlayerStateLost() {

    statusLabel.setText("Lost!");

    rightPanel.setBorder(
        BorderFactory.createLineBorder(Color.GREEN, 4));

    leftPanel.setBorder(null);
  }
}
