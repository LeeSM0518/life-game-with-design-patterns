import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProtoClock implements MenuContributor {

  @Override
  public void addMenus(final JMenuBar menuBar) {
    JMenuItem halt = new JMenuItem("Halt");
    JMenuItem slow = new JMenuItem("Slow");
    JMenuItem fast = new JMenuItem("Fast");

    halt.setName("halt");
    slow.setName("slow");
    fast.setName("fast");

    ActionListener handler =
        new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            final String name = ((JMenuItem) e.getSource()).getName();
            switch (name.charAt(0)) {
              case 'h':
                setClockSpeed(0);
                break;
              case 'f':
                setClockSpeed(500);
                break;
              case 's':
                setClockSpeed(250);
                break;
            }
          }
        };

    halt.addActionListener(handler);
    slow.addActionListener(handler);
    fast.addActionListener(handler);

    JMenu go = new JMenu("Go");
    go.add(halt);
    go.add(slow);
    go.add(fast);

    menuBar.add(go);
  }

  private void setClockSpeed(int speed) {
    System.out.println("Changing speed to " + speed);
  }
}
