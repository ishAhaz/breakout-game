import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Simple Breakout-style game.
 * Move the paddle with LEFT/RIGHT arrow keys (or A/D).
 * Bounce the ball to break all the bricks in the wall.
 *
 * To run:
 *   javac BreakoutGame.java
 *   java BreakoutGame
 */
public class BreakoutGame extends JPanel implements ActionListener, KeyListener {

    // --- Window size ---
    final int WIDTH = 700;
    final int HEIGHT = 700;

    // --- Paddle ---
    int paddleX = 300;
    final int PADDLE_Y = 640;
    final int PADDLE_WIDTH = 50;
    final int PADDLE_HEIGHT = 12;
    final int PADDLE_SPEED = 8;
    boolean movingLeft = false;
    boolean movingRight = false;

    // --- Ball ---
    double ballX = 290;
    double ballY = 300;
    double ballDX = 3.5;
    double ballDY = -3.5;
    final int BALL_SIZE = 14;

    // --- Bricks ---
    final int ROWS = 6;
    final int COLS = 9;
    final int BRICK_WIDTH = 65;
    final int BRICK_HEIGHT = 20;
    final int BRICK_GAP = 6;
    final int BRICK_TOP = 50;
    ArrayList<Rectangle> bricks = new ArrayList<>();

    // --- Game state ---
    Timer timer;
    boolean gameOver = false;
    boolean youWin = false;
    int score = 0;
    int lives = 2;

    public BreakoutGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        buildBricks();

        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    void buildBricks() {
        bricks.clear();
        int totalWidth = COLS * (BRICK_WIDTH + BRICK_GAP) - BRICK_GAP;
        int startX = (WIDTH - totalWidth) / 2;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = startX + col * (BRICK_WIDTH + BRICK_GAP);
                int y = BRICK_TOP + row * (BRICK_HEIGHT + BRICK_GAP);
                bricks.add(new Rectangle(x, y, BRICK_WIDTH, BRICK_HEIGHT));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Paddle
        g2.setColor(Color.CYAN);
        g2.fillRoundRect(paddleX, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT, 8, 8);

        // Ball
        g2.setColor(Color.WHITE);
        g2.fillOval((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);

        // Bricks
        Color[] rowColors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA};
        for (int i = 0; i < bricks.size(); i++) {
            Rectangle b = bricks.get(i);
            int row = i / COLS;
            g2.setColor(rowColors[row % rowColors.length]);
            g2.fillRect(b.x, b.y, b.width, b.height);
            g2.setColor(Color.BLACK);
            g2.drawRect(b.x, b.y, b.width, b.height);
        }

        // HUD
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Score: " + score, 15, 25);
        g2.drawString("Lives: " + lives, WIDTH - 90, 25);

        if (gameOver) {
            drawCenteredMessage(g2, youWin ? "YOU WIN! Press R to restart" : "GAME OVER - Press R to restart");
        }
    }

    void drawCenteredMessage(Graphics2D g2, String msg) {
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        int msgWidth = fm.stringWidth(msg);
        g2.setColor(Color.WHITE);
        g2.drawString(msg, (WIDTH - msgWidth) / 2, HEIGHT / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            movePaddle();
            moveBall();
            checkCollisions();
        }
        repaint();
    }

    void movePaddle() {
        if (movingLeft) paddleX -= PADDLE_SPEED;
        if (movingRight) paddleX += PADDLE_SPEED;
        if (paddleX < 0) paddleX = 0;
        if (paddleX > WIDTH - PADDLE_WIDTH) paddleX = WIDTH - PADDLE_WIDTH;
    }

    void moveBall() {
        ballX += ballDX;
        ballY += ballDY;

        // Wall bounces
        if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) {
            ballDX = -ballDX;
        }
        if (ballY <= 0) {
            ballDY = -ballDY;
        }

        // Fell below paddle = lose a life
        if (ballY > HEIGHT) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                youWin = false;
            } else {
                resetBall();
            }
        }
    }

    void resetBall() {
        ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
        ballY = 300;
        ballDX = 3.5 * (Math.random() < 0.5 ? 1 : -1);
        ballDY = -3.5;
    }

    void checkCollisions() {
        Rectangle ballRect = new Rectangle((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);

        // Paddle collision
        Rectangle paddleRect = new Rectangle(paddleX, PADDLE_Y, PADDLE_WIDTH, PADDLE_HEIGHT);
        if (ballRect.intersects(paddleRect) && ballDY > 0) {
            ballDY = -ballDY;
            // Add a bit of angle depending on where it hit the paddle
            double hitPos = (ballX + BALL_SIZE / 2.0) - (paddleX + PADDLE_WIDTH / 2.0);
            ballDX = hitPos * 0.15;
        }

        // Brick collisions
        for (int i = bricks.size() - 1; i >= 0; i--) {
            Rectangle b = bricks.get(i);
            if (ballRect.intersects(b)) {
                bricks.remove(i);
                ballDY = -ballDY;
                score += 10;
                break; // only break one brick per frame
            }
        }

        // Win condition
        if (bricks.isEmpty()) {
            gameOver = true;
            youWin = true;
        }
    }

    void restartGame() {
        paddleX = 250;
        resetBall();
        buildBricks();
        score = 0;
        lives = 3;
        gameOver = false;
        youWin = false;
    }

    // --- Keyboard controls ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) movingLeft = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) movingRight = true;
        if (code == KeyEvent.VK_R && gameOver) restartGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) movingLeft = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) movingRight = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // not used
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Breakout - Bounce the Ball!");
        BreakoutGame game = new BreakoutGame();

        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        game.requestFocusInWindow();
    }
}