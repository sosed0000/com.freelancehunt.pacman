import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        GameFrame frame = new GameFrame();
    }
}

class GameFrame extends JFrame {
    public GameFrame() {
        GamePanel gamePanel = new GamePanel();
        this.add(gamePanel);
        this.setTitle("Pac-Man");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}

class GamePanel extends JPanel implements ActionListener {
    GameModel model;
    Timer timer;

    public GamePanel() {
        model = new GameModel();
        timer = new Timer(300, this);
        timer.start();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                model.processUserInput(e);
            }
        });
        this.setFocusable(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(650, 450);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        model.pacMan.draw(g);
        for (Ghost ghost : model.ghosts) {
            ghost.draw(g);
        }
        for (Food f : model.food) {
            f.draw(g);
        }
        for (Wall wall : model.walls) {
            wall.draw(g);
        }
        drawScore(g);
        drawTime(g);
    }


    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + model.score, 10, 20);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        model.updateGameState();
        model.timePassed++;
        repaint();
        if (model.gameOver) {
            timer.stop();

            String message;
            if (model.hasWon()) {
                message = "You Win!";
            } else {
                message = "Game Over";
            }

            Object[] options = {"Restart", "Exit"};
            int result = JOptionPane.showOptionDialog(this, message, "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (result == JOptionPane.YES_OPTION) {
                GameFrame frame = (GameFrame) SwingUtilities.getWindowAncestor(this);
                frame.setContentPane(new GamePanel());
                frame.pack();
            } else if (result == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
    }




    private void drawTime(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Time: " + model.timePassed / 10.0 + "s", 10, 40);
    }

}

class GameModel {
    int[][] map = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    };
    ArrayList<Food> food;
    ArrayList<Wall> walls;
    PacMan pacMan;
    ArrayList<Ghost> ghosts;
    int score;
    int timePassed;
    boolean gameOver;

    public GameModel() {
        pacMan = new PacMan(1, 1);
        ghosts = new ArrayList<>();

        ghosts.add(new Ghost(1, map.length - 2));
        ghosts.add(new Ghost(map[0].length - 2, map.length - 2));


        food = new ArrayList<>();
        walls = new ArrayList<>();
        initializeFoodAndWalls();

        score = 0;
        gameOver = false;
    }

    private void initializeFoodAndWalls() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 1) {
                    walls.add(new Wall(j, i, 40, 40));
                } else if (map[i][j] == 0) {
                    food.add(new Food(j, i));
                }
            }
        }
    }

    public void processUserInput(KeyEvent e) {
        pacMan.processUserInput(e, walls);
    }


    public void updateGameState() {
        pacMan.move(walls);
        Iterator<Food> foodIterator = food.iterator();
        while (foodIterator.hasNext()) {
            Food f = foodIterator.next();
            if (pacMan.collidesWith(f)) {
                foodIterator.remove();
                score++;
            }
        }
        if (food.isEmpty()) {
            gameOver = true;
        }

        for (Ghost ghost : ghosts) {
            if (pacMan.collidesWith(ghost)) {
                gameOver = true;
                break;
            }
            ghost.moveRandomly(map, walls);

        }
        //Костыль, который решил проблему периодического нераспознавания коллизий.
        for (Ghost ghost : ghosts) {
            if (pacMan.collidesWith(ghost)) {
                gameOver = true;
                break;
            }
        }
    }




    private void checkCollisions() {
        for (Ghost ghost : ghosts) {
            if (pacMan.collidesWith(ghost)) {
                score += 10;
                ghost.reset();
            }
        }

        Iterator<Food> foodIterator = food.iterator();
        while (foodIterator.hasNext()) {
            Food f = foodIterator.next();
            if (pacMan.collidesWith(f)) {
                score += 1;
                foodIterator.remove();
            }
        }
    }


    public boolean hasWon() {
        return food.isEmpty();
    }

}

interface GameObject {
    void draw(Graphics g);
}

class PacMan {
    private int x, y;
    private int mapX, mapY;
    private int dx, dy;
    private static final int CELL_SIZE = 40;
    public PacMan(int mapX, int mapY) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.x = mapX * CELL_SIZE;
        this.y = mapY * CELL_SIZE;
    }
    public void processUserInput(KeyEvent e, ArrayList<Wall> walls) {
        int key = e.getKeyCode();
        int tempDx = 0;
        int tempDy = 0;

        if (key == KeyEvent.VK_LEFT) {
            tempDx = -1;
        } else if (key == KeyEvent.VK_RIGHT) {
            tempDx = 1;
        } else if (key == KeyEvent.VK_UP) {
            tempDy = -1;
        } else if (key == KeyEvent.VK_DOWN) {
            tempDy = 1;
        }

        int newX = mapX + tempDx;
        int newY = mapY + tempDy;

        boolean canMove = true;
        for (Wall wall : walls) {
            if (newX == wall.getMapX() && newY == wall.getMapY()) {
                canMove = false;
                break;
            }
        }

        if (canMove) {
            dx = tempDx;
            dy = tempDy;
        }
    }

    public void move(ArrayList<Wall> walls) {
        int newX = mapX + dx;
        int newY = mapY + dy;

        boolean canMove = true;
        for (Wall wall : walls) {
            if (newX == wall.getMapX() && newY == wall.getMapY()) {
                canMove = false;
                break;
            }
        }

        if (canMove) {
            mapX = newX;
            mapY = newY;
            x = mapX * CELL_SIZE;
            y = mapY * CELL_SIZE;
        }
    }

    public boolean collidesWith(Ghost ghost) {
        return mapX == ghost.getMapX() && mapY == ghost.getMapY();
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillArc(x, y, CELL_SIZE, CELL_SIZE, 30, 300);
    }

    public boolean collidesWith(Food food) {
        return mapX == food.getMapX() && mapY == food.getMapY();
    }
}

class Food implements GameObject {
    private int x, y;
    private int mapX, mapY;
    private static final int CELL_SIZE = 40;
    public Food(int mapX, int mapY) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.x = mapX * CELL_SIZE;
        this.y = mapY * CELL_SIZE;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x + CELL_SIZE / 2 - 4, y + CELL_SIZE / 2 - 4, 8, 8);
    }
}

class Ghost implements GameObject {
    private int x, y, dx, dy;
    private int mapX, mapY;
    private static final int CELL_SIZE = 40;
    public Ghost(int mapX, int mapY) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.x = mapX * CELL_SIZE;
        this.y = mapY * CELL_SIZE;
        do {
            int[] directions = {-1, 0, 1};
            dx = directions[(int) (Math.random() * 3)];
            dy = directions[(int) (Math.random() * 3)];
        } while (dx == 0 && dy == 0);
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void reset() {
        mapX = (int) (Math.random() * 5) + 1;
        mapY = (int) (Math.random() * 5) + 1;
        x = mapX * CELL_SIZE;
        y = mapY * CELL_SIZE;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, CELL_SIZE, CELL_SIZE);
    }

    public void moveRandomly(int[][] map, ArrayList<Wall> walls) {
        int[] directionsX = {-1, 0, 1, 0};
        int[] directionsY = {0, -1, 0, 1};

        // Добавим код для определения обратного направления
        int oppositeDirection = -1;
        if (dx != 0 || dy != 0) {
            for (int i = 0; i < 4; i++) {
                if (directionsX[i] == -dx && directionsY[i] == -dy) {
                    oppositeDirection = i;
                    break;
                }
            }
        }

        List<Integer> possibleDirections = new ArrayList<>();

        // Проверяем все четыре направления на возможность движения
        for (int i = 0; i < 4; i++) {
            // Пропускаем обратное направление, если оно определено
            if (i == oppositeDirection) continue;

            int newX = mapX + directionsX[i];
            int newY = mapY + directionsY[i];

            boolean canMove = true;
            for (Wall wall : walls) {
                if (newX == wall.getMapX() && newY == wall.getMapY()) {
                    canMove = false;
                    break;
                }
            }
            if (canMove) {
                possibleDirections.add(i);
            }
        }

        // Выбираем случайное направление из возможных
        if (!possibleDirections.isEmpty()) {
            int direction = possibleDirections.get((int) (Math.random() * possibleDirections.size()));
            dx = directionsX[direction];
            dy = directionsY[direction];
        }

        // Двигаем привидение
        mapX += dx;
        mapY += dy;
        x = mapX * CELL_SIZE;
        y = mapY * CELL_SIZE;
    }


}

class Wall implements GameObject {
    private int x, y;
    private int mapX, mapY;
    private int width, height;
    private static final int CELL_SIZE = 40;

    public Wall(int mapX, int mapY, int width, int height) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.x = mapX * CELL_SIZE;
        this.y = mapY * CELL_SIZE;
        this.width = width;
        this.height = height;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}
