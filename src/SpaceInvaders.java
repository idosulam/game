import javax.swing.*;
import java.awt.*;

public class SpaceInvaders extends JFrame {

    public SpaceInvaders() {
        initUI();
    }

    private void initUI() {
        add(new Board());
        setTitle("Space Invaders");
        setSize(Commons.BOARD_WIDTH, Commons.BOARD_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceInvaders().setVisible(true);

            }
        });
    }
}