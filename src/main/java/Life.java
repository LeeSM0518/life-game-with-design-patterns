import javax.swing.*;
import java.awt.*;

// 185p
public final class Life extends JFrame {

  private static JComponent universe;

  public static void main(String[] args) {
    new Life();
  }

  private Life() {
    super("The Game of Life. " + "&copy;2003 Allen I. Holub <http://www.holub.com>");

    // MenuSite를 프레임에 장착한다.
    MenuSite.establish(this);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(Universe.instance(), BorderLayout.CENTER);

    pack();
    setVisible(true);
  }

}
