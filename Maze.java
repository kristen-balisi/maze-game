/* Name: Maze Solver
 * Description: This maze program features a 20x20 grid maze where the 
 * objective is for the player to navigate from the starting point to the exit. 
 * It offers two game modes: player-solve and computer-solve. In player mode, 
 * the user can navigate the maze manually using the keyboard arrow keys. In 
 * computer-assisted mode, the program uses an algorithm to search for an exit route 
 * and, if found, displays the path to the user. This program also prompts the user 
 * to enter a file name to indicate which maze map they will use.
 */

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Maze {

    public static void main(String[] args) {

        Boolean playAgain = true;

        while (playAgain) {

            MazeGame mazeGame = new MazeGame();
            mazeGame.setFocusable(true);

            JFrame mazeFrame = new JFrame("Maze Game - Kristen Balisi");
            mazeFrame.setBounds(725, 33, 633, 750);
            mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mazeFrame.setIconImage(new ImageIcon("Wall.png").getImage());
            mazeFrame.setVisible(true);
            mazeFrame.add(mazeGame);

            mazeGame.playGame();

            playAgain = mazeGame.playAgain();
        }
        System.out.println("Thank you for playing! Bye!");
    }

}

class MazeGame extends JPanel implements KeyListener {

    // initialize variables
    BufferedImage mazeGrid;
    BufferedImage playerPic;
    BufferedImage wallPic;
    BufferedImage hallPathPic;
    BufferedImage startPic;
    BufferedImage endPic;
    BufferedImage visitedPathPic;

    final static String mazeGridImgPath = "MazeGrid.png";
    final static String playerImgPath = "Player.png";
    final static String hallImgPath = "Hall.png";
    final static String wallImgPath = "Wall.png";
    final static String startImgPath = "Start.png";
    final static String endImgPath = "End.png";
    final static String visitedImgPath = "Visited.png";

    int[][] mazeMap = new int[20][20];

    int gameMode;
    int direction;
    Boolean isBoardValid;
    int startX, startY, exitX, exitY, playerCurrentRow, playerCurrentCol;

    MazeGame() {

        this.addKeyListener(this);

        // load maze images
        try {
            mazeGrid = ImageIO.read(new File(mazeGridImgPath));
            playerPic = ImageIO.read(new File(playerImgPath));
            wallPic = ImageIO.read(new File(wallImgPath));
            startPic = ImageIO.read(new File(startImgPath));
            endPic = ImageIO.read(new File(endImgPath));
            hallPathPic = ImageIO.read(new File(hallImgPath));
            visitedPathPic = ImageIO.read(new File(visitedImgPath));
        } 

        catch (Exception e) {
            System.out.println("Exception in loading image: " + e.toString());
        }

        isBoardValid = initBoard();
    }

    // methods/functions related to starting game and playing game

    public Boolean initBoard() {
        // set game mode (user vs. computer)
        gameMode = getGameMode();

        // read map file
        try {
            mazeMap = readMazeFile();
        } 

        catch (Exception e) {
            System.out.println("Exception in opening file: " + e.toString());
            return false;
        }

        // set maze starting point to 2
        startX = 0;
        startY = 1;
        mazeMap[startX][startY] = 2;

        // set maze exit point to 3
        exitX = 18;
        exitY = 19;
        mazeMap[exitX][exitY] = 3;

        // set player starting point
        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {
                if (mazeMap[row][col] == 2) {
                    playerCurrentRow = col;
                    playerCurrentCol = row;
                }
            }
        }

        // computer game mode
        if (gameMode == 2) {
            // program checks if maze is solvable
            // if solvable, an exit path will be displayed
            if (isMazeSolved(mazeMap, startX, startY)) {
                System.out.println("This maze is solvable! Follow the blue path!");
            } 

            else {
                System.out.println("This maze is unsolvable!");
                // if unsolvable, initBoard() method returns false
                return false;
            }
        }
        // if solvable, initBoard() function returns true
        return true;
    }

    public void playGame() {
        // if maze board is invalid, break out of playGame() function
        if (!isBoardValid) {
            return;
        }

        animate();

        while (true) {
            if (checkPlayerWin()) {
                System.out.println("\nWoohoo! You have found the exit to the maze!");
                break;
            }

            try {
                Thread.sleep(100);
            } 

            catch (Exception e) {
                System.out.println("Exception in thread sleep: " + e.toString());
            }
        }
    }

    // functions related to initializing game and getting user input

    public static int[][] readMazeFile() throws FileNotFoundException {

        int mazeMap[][] = new int[20][20];
        Scanner scanMap = new Scanner(System.in);
        System.out.println("Enter name of map file: ");
        String fileName = scanMap.nextLine();
        
        File text = new File(fileName);
        Scanner scan = new Scanner(text);

        int row = 0;

        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            String rowVals[] = line.split(",");

            for (int column = 0; column < rowVals.length; column++) {
                mazeMap[row][column] = Integer.parseInt(rowVals[column]);
            }
            row++;
        }

        return mazeMap;
    }

    public static int getGameMode() {
        Scanner userInput = new Scanner(System.in);
        System.out.println("Select game mode ('1' for user or '2' for computer): ");
        int gameMode = userInput.nextInt();
        return gameMode;
    }

    public Boolean playAgain() {
        Scanner stringScanner = new Scanner(System.in);
        System.out.println("\nWould you like to play again (Y/N)? ");
        String playAgain = stringScanner.nextLine();

        return playAgain.toLowerCase().equals("y");
    }

    // methods/functions related to updating game panel images and player movement

    public void animate() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderMaze(g);
    }

    public void renderMaze(Graphics g) {
        for (int row = 0; row < 20; row++) {
            for (int col = 0; col < 20; col++) {

                // Maze key:
                // 0 - Hall
                // 1 - Wall
                // 2 - Start
                // 3 - Exit
                // 4 - Visited path (computer-assisted mode)

                if (mazeMap[row][col] == 0) {
                    g.drawImage(hallPathPic, 33 + col * 28, 33 + row * 28, null);
                }

                if (mazeMap[row][col] == 1) {
                    g.drawImage(wallPic, 33 + col * 28, 33 + row * 28, null);
                }

                if (mazeMap[row][col] == 2) {
                    g.drawImage(startPic, 33 + col * 28, 33 + row * 28, null);
                }

                if (mazeMap[row][col] == 3) {
                    g.drawImage(endPic, 33 + col * 28, 33 + row * 28, null);
                }

                if (mazeMap[row][col] == 4) {
                    g.drawImage(visitedPathPic, 33 + col * 28, 33 + row * 28, null);
                }

                int userRow = 33 + playerCurrentRow * 28;
                int userCol = 33 + playerCurrentCol * 28;
                g.drawImage(playerPic, userRow, userCol, null);
            }
        }
        g.drawImage(mazeGrid, 24, 27, null);
    }

    public void keyPressed(KeyEvent e) {
        renderAffectedPortionOnly(e);
    }

    // only repaint affected portion of screen (update player position)

    public void renderAffectedPortionOnly(KeyEvent e) {
        int oldCol = playerCurrentCol;
        int oldRow = playerCurrentRow;
        direction = e.getKeyCode();

        switch (direction) {
            case KeyEvent.VK_DOWN:
                // check if player is within bounds of maze grid
                if ((playerCurrentCol + 1) >= 0 && (playerCurrentCol + 1) < 20) {
                    // check if path is not a wall
                    if (mazeMap[playerCurrentCol + 1][playerCurrentRow] != 1) {
                        playerCurrentCol = playerCurrentCol + 1;
                    }
                }
                break;
            case KeyEvent.VK_UP:
                if ((playerCurrentCol - 1) >= 0 && (playerCurrentCol - 1) < 20) {
                    if (mazeMap[playerCurrentCol - 1][playerCurrentRow] != 1) {
                        playerCurrentCol = playerCurrentCol - 1;
                    }
                }
                break;
            case KeyEvent.VK_LEFT:
                if ((playerCurrentRow - 1) >= 0 && (playerCurrentRow - 1) < 20) {
                    if (mazeMap[playerCurrentCol][playerCurrentRow - 1] != 1) {
                        playerCurrentRow = playerCurrentRow - 1;
                    }
                }
                break;
            case KeyEvent.VK_RIGHT:
                if ((playerCurrentRow + 1) >= 0 && (playerCurrentRow + 1) < 20) {
                    if (mazeMap[playerCurrentCol][playerCurrentRow + 1] != 1) {
                        playerCurrentRow = playerCurrentRow + 1;
                    }
                }
                break;
        }

        // only update affected section of screen (player position)
        repaint(33 + oldRow * 28, 33 + oldCol * 28, 33, 33);
        repaint(33 + playerCurrentRow * 28, 33 + playerCurrentCol * 28, 33, 33);
    }

    public boolean checkPlayerWin() {
        return (mazeMap[playerCurrentCol][playerCurrentRow] == 3);
    }

    // functions related to computer-assisted maze solving

    public static boolean isValidPosition(int[][] mazeMap, int row, int column) {

        // check if position is within bounds of maze
        if (row >= 0 && row < 20 && column >= 0 && column < 20) {

            // check if position is not a wall and not a visited path
            return (mazeMap[row][column] != 1) && (mazeMap[row][column] != 4);
        }
        return false;
    }

    public static boolean isMazeSolved(int[][] mazeMap, int row, int column) {

        boolean solved = false;

        // check if position is valid
        if (isValidPosition(mazeMap, row, column)) {

            // check if end of maze has been reached
            if (mazeMap[row][column] == 3) {
                solved = true;
            }

            if (mazeMap[row][column] != 2 && mazeMap[row][column] != 3) {
                // if end of maze has not been reached, turn current block into a visited path
                // position is no longer valid (creating a "virtual wall")
                mazeMap[row][column] = 4;
            }

            // check right block
            if (!solved) {
                solved = isMazeSolved(mazeMap, row, column + 1);
            }

            // check up block
            if (!solved) {
                solved = isMazeSolved(mazeMap, row - 1, column);
            }

            // check left block
            if (!solved) {
                solved = isMazeSolved(mazeMap, row, column - 1);
            }

            // check down block
            if (!solved) {
                solved = isMazeSolved(mazeMap, row + 1, column);
            }
        }
        return solved;
    }

    // implement methods defined in KeyListener interface

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}
}
