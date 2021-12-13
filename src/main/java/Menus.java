import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 179p
public class Menus extends JFrame {

  public Menus() {
    final JComponent theUniverse = new ProtoUniverse();
    final MenuContributor theClock = new ProtoClock();

    JMenuBar menuBar = new JMenuBar();
    theClock.addMenus(menuBar);
    ((MenuContributor)theUniverse).addMenus(menuBar);

    JMenuItem exit = new JMenuItem("Exit");
    exit.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            System.exit(0);
          }
        }
    );
    menuBar.add(exit);

    menuBar.setVisible(true);
    setJMenuBar(menuBar);

    getContentPane().add(theUniverse);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setSize(200, 200);
    show();
  }

  public static void main(String[] args) {
    new Menus();
  }

}
