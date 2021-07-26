import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JFrame {

    // Named constants for the game board
    int ROWS = 3;
    int COLS = 3;

    // Named constants for graphics drawing
    int CELL_SIZE = 100; // cell width and height
    int CANVAS_WIDTH = CELL_SIZE * COLS;
    int CANVAS_HEIGHT = CELL_SIZE * ROWS;
    int GRID_WIDTH = 8; // Grid line width
    int GRID_WIDTH_HALF = GRID_WIDTH / 2; // half width

    // constants for drawing symbols with a padded border 
    int CELL_PADDING = CELL_SIZE / 6;
    int SYMBOL_SIZE = CELL_SIZE - CELL_PADDING * 2; // width/height
    int SYMBOL_STROKE_WIDTH = 8; // pen's stroke width 

    // Use enum (inner class) to represent seeds and cell constants
    public enum Seed {
        BLANK, X, O
    }
    private Seed currentPlayer;
    private Seed[][] board;

    // Use enum to track game state
    public enum GameState {
        PLAYING, DRAW, X_WON, O_WON
    }
    private GameState currentState;

    private DrawCanvas canvas; // Drawing panel (JPanel) for the game board
    private JLabel statusBar; // Status bar

    class DrawCanvas extends JPanel {

        public void paintComponent(Graphics g) { // invoke via repaint
            System.out.println("repainted");
            super.paintComponent(g); // fills background
            setBackground(Color.WHITE);

            // Draw the gridlines
            g.setColor(Color.LIGHT_GRAY);
            for (int r = 1; r < ROWS; r++) {
                g.fillRoundRect(0, CELL_SIZE * r - GRID_WIDTH_HALF, 
                    CANVAS_WIDTH - 1, GRID_WIDTH, GRID_WIDTH, GRID_WIDTH);
            }
            for (int c = 1; c < COLS; c++) {
                g.fillRoundRect(CELL_SIZE * c - GRID_WIDTH_HALF, 0, 
                    GRID_WIDTH, CANVAS_HEIGHT - 1, GRID_WIDTH, GRID_WIDTH);
            }

            // Draw the seeds of all of the cells if they aren't empty
            // Use Graphics 2D b/c it lets us set the stroke width
            Graphics2D g2d = (Graphics2D)g;
            g2d.setStroke(new BasicStroke(SYMBOL_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int r = 0; r < ROWS; ++r) {
                for (int c = 0; c < COLS; ++c) {
                    int x1 = c * CELL_SIZE + CELL_PADDING;
                    int y1 = r * CELL_SIZE + CELL_PADDING;
                    if (board[r][c] == Seed.X) {
                        g2d.setColor(Color.RED);
                        int x2 = (c + 1) * CELL_SIZE - CELL_PADDING;
                        int y2 = (r + 1) * CELL_SIZE - CELL_PADDING;
                        g2d.drawLine(x1, y1, x2, y2);
                        g2d.drawLine(x2, y1, x1, y2);
                        System.out.println("drew x");
                    } else if (board[r][c] == Seed.O) {
                        g2d.setColor(Color.BLUE);
                        g2d.drawOval(x1, y1, SYMBOL_SIZE, SYMBOL_SIZE);
                        System.out.println("drew o");
                    }
                }
            }

            // Print status bar message
            if (currentState == GameState.PLAYING) {
                statusBar.setForeground(Color.BLACK);
                if (currentPlayer == Seed.X) {
                    statusBar.setText("X's Turn");
                } else {
                    statusBar.setText("O's Turn");
                }
            } else if (currentState == GameState.DRAW) {
                statusBar.setForeground(Color.RED);
                statusBar.setText("It's a draw! Click to play again.");
            } else if (currentState == GameState.X_WON) {
                statusBar.setForeground(Color.RED);
                statusBar.setText("X won! Click to play again.");
            } else if (currentState == GameState.O_WON) {
                statusBar.setForeground(Color.RED);
                statusBar.setText("O won! Click to play again.");
            }
        }   
    }

    // Constructor to set up the game and the GUI components
    public Game() {
        canvas = new DrawCanvas(); // Construct canvas (JPanel)
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        // set up the status bar to display message
        statusBar = new JLabel("    ");
        statusBar.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 4, 5));
        
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(canvas, BorderLayout.CENTER);
        cp.add(statusBar, BorderLayout.PAGE_END);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack(); // Pack all the components in this JFrame
        setTitle("Tic Tac Toe");
        setVisible(true);

        board = new Seed[ROWS][COLS]; // allocate array
        initGame(); 
        // the canvas (JPanel) fires a MouseEvent whenever mouse clicked
        canvas.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { // click handler
                int mouseX = e.getX();
                int mouseY = e.getY();
                // Get row and col clicked
                int rowClicked = mouseY / CELL_SIZE;
                int colClicked = mouseX / CELL_SIZE;
                System.out.println("Clicked row " + rowClicked + ", col " + colClicked);

                if (currentState == GameState.PLAYING) {
                    if (rowClicked >= 0  && rowClicked < ROWS
                            && colClicked >= 0 && colClicked < COLS
                            && board[rowClicked][colClicked] == Seed.BLANK) {
                        // Make move 
                        board[rowClicked][colClicked] = currentPlayer;

                        updateGame(currentPlayer, rowClicked, colClicked); // update state

                        // Switch player 
                        currentPlayer = (currentPlayer == Seed.X) ? Seed.O : Seed.X;
                    }
                } else { // game over
                    initGame(); // restart game 
                }
                // Refresh the drawing canvas
                repaint(); // Call-back the paint component
            }
        });
    }

    // init game board contents and status
    public void initGame() {
        // set all cells to blank
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = Seed.BLANK;
            }
        }
        currentState = GameState.PLAYING;
        currentPlayer = Seed.X; // x plays first
    }

    public boolean hasWon(Seed seed, int r, int c) {
        return (board[r][0] == seed 
             && board[r][1] == seed
             && board[r][2] == seed)
        || (board[0][c] == seed
        && board[1][c] == seed
        && board[2][c] == seed)
        || (board[0][0] == seed 
        && board[1][1] == seed
        && board[2][2] == seed)
        || (board[0][2] == seed
        && board[1][1] == seed
        && board[2][0] == seed);
    }

    public boolean isDraw() {
        for (int r = 0; r < ROWS; ++r) {
            for (int c = 0; c < COLS; ++c) {
                if (board[r][c] == Seed.BLANK) {
                    return false; // not a draw b/c empty cell found
                }
            }
        }
        return true; // no empty cells
    }

    public void updateGame(Seed seed, int r, int c) {
        if (hasWon(seed, r, c)) {
            if (seed == Seed.X) {
                currentState = GameState.X_WON;
            } else {
                currentState = GameState.O_WON;
            }
        } else if (isDraw()) {
            currentState = GameState.DRAW;
        } else {
            currentState = GameState.PLAYING;
        }
    }

    // public static void main(String[] args) {

    //     new Game();
    // }
}