import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Single extends JPanel {
    private Dimension d;
    private Alien[] aliens;
    private Player player;
    private Shot shot;

    private int deaths = 0;
    private String message = "Game Over";
    private Timer timer;


    public Single() {
        initBoard();
        gameInit();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        d = new Dimension(Commons.BOARD_WIDTH, Commons.BOARD_HEIGHT);
        timer = new Timer(Commons.DELAY, new GameCycle());
        timer.start();
    }

    private void gameInit() {
        aliens = new Alien[24];
        addaliens();
        player = new Player();
        shot = new Shot();
        shot.setVisible(false);

    }

    private void addaliens() {
        int cnt = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                Alien alien = new Alien(Commons.ALIEN_INIT_X + 100 * j,
                        Commons.ALIEN_INIT_Y + 40 * i);
                aliens[cnt] = alien;
                cnt += 1;
            }
        }
        for (Alien alien : aliens)
            alien.start();
    }

    private void drawAliens(Graphics g) {
        for (Alien alien : aliens) {
            if (alien.isVisible()) {
                g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
            }
        }
    }

    private void drawPlayer(Graphics g) {

        ImageIcon ii = new ImageIcon(player.playerImgs[0]);
        g.drawImage(ii.getImage(), player.getX(), player.getY(), null);


    }

    private void drawShot(Graphics g) {
        if (shot.isVisible()) {
            g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
        }
    }

    private void drawBombing(Graphics g) {
        for (Alien a : aliens) {
            Bomb b = a.getBomb();
            if (!b.isDestroyed()) {
                g.drawImage(b.getImage(), b.getX(), b.getY(), this);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black); // game background
        g.fillRect(0, 0, d.width, d.height);
        g.setColor(Color.green);// ground
        if (player.isIngame()) {
            g.drawLine(0, Commons.GROUND, Commons.BOARD_WIDTH, Commons.GROUND);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g);
        } else {
            if (timer.isRunning()) {
                timer.stop();
            }
            gameOver(g);
        }
        Toolkit.getDefaultToolkit().sync();
    }


    private void gameOver(Graphics g) {
        g.setColor(Color.black); // whole screen background
        g.fillRect(0, 0, Commons.BOARD_WIDTH, Commons.BOARD_HEIGHT);
        g.setColor(Color.black); // message background
        g.fillRect(50, Commons.BOARD_HEIGHT / 2 - 30, Commons.BOARD_WIDTH - 100, 50);
        g.setColor(Color.blue); // outline
        g.drawRect(50, Commons.BOARD_HEIGHT / 2 - 30, Commons.BOARD_WIDTH - 100, 50);
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics fontMetrics = this.getFontMetrics(small);
        g.setColor(Color.white); // message text
        g.setFont(small);
        g.drawString(message, (Commons.BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                Commons.BOARD_HEIGHT / 2);
        gameended();
    }

    private void gameended() {
        new SwingControlDemo(message);
    }

    private void update() {

        // hit alien
        for (Alien alien : aliens) {

            if (alien.isVisible() && shot.isVisible()) {
                // hit alien
                if (shot.getX() >= (alien.getX())
                        && shot.getX() <= (alien.getX() + Commons.ALIEN_WIDTH)
                        && shot.getY() >= (alien.getY())
                        && shot.getY() <= (alien.getY() + Commons.ALIEN_HEIGHT)) {
                    alien.setDying(true);
                    deaths++;
                    shot.setVisible(false);
                }
            }
        }
        // move each alien down depends on direction 1 moving right -1 moving left
        // checks for each alien if he is touching ground
        for (Alien alien : aliens) {
            if (alien.isVisible()) {
                if (alien.getY() > Commons.ALIENGROUND - Commons.ALIEN_HEIGHT) {
                    player.setIngame(false);
                    timer.stop();
                    message = "Invasion!";
                    gameended();
                }
            }
        }
//move down
        for (Alien alien : aliens) {
            int bo = 0;
            if (alien.isBorder()) {
                bo = 1;
                for (Alien al : aliens)
                    al.setY(al.getY() + Commons.GO_DOWN);
            }
            if (bo == 1)
                alien.setBorder(false);
        }
        // bombs
        Random generator = new Random();
        for (Alien alien : aliens) {
            int shot = generator.nextInt(15);
            Bomb bomb = alien.getBomb();
            // make sure they don't shoot at the same time
            if (shot == Commons.CHANCE && alien.isVisible() && bomb.isDestroyed()) {
                bomb.setDestroyed(false);
                bomb.setX(alien.getX());
                bomb.setY(alien.getY());
            }

            // player got hit by alien
            if (player.isVisible() && !bomb.isDestroyed()) {
                if (bomb.getX() >= (player.getX())
                        && bomb.getX() <= (player.getX() + Commons.PLAYER_WIDTH)
                        && bomb.getY() >= (player.getY())
                        && bomb.getY() <= (player.getY() + Commons.PLAYER_HEIGHT)) {
                    player.setDying(true);
                    bomb.setDestroyed(true);
                    message = "lost game";
                }
            }
        }


        if (deaths == Commons.NUMBER_OF_ALIENS_TO_DESTROY) {
            message = "win";
            player.setIngame(false);
            timer.stop();

        }

    }



    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
            repaint();

        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                player.setDx(0);
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                player.setDx(0);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                player.setDx(-2);
            }
            if (key == KeyEvent.VK_RIGHT) {
                player.setDx(2);
            }



            if (key == KeyEvent.VK_SPACE) {
                if (player.isIngame()) {
                    // only 1 shot each time
                    if (!shot.isVisible()) {
                        shot = new Shot(player.getX(), player.getY());
                    }
                }
            }
        }
    }
}