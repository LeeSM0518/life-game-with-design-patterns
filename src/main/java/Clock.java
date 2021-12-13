import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

// 152p
public class Clock {

  private static Clock instance;

  private Timer clock = new Timer();
  private TimerTask tick = null;
  private Publisher publisher = new Publisher();

  private Clock() {
    createMenus();
  }

  public synchronized static Clock instance() {
    if (instance == null) instance = new Clock();
    return instance;
  }

  public void addClockListener(Listener observer) {
    publisher.subscribe(observer);
  }

  public void tick() {
    publisher.publish(
        new Publisher.Distributor() {
          public void deliverTo(Object subscriber) {
            ((Listener) subscriber).tick();
          }
        }
    );
  }

  public void stop() {
    startTicking(0);
  }

  private void startTicking(final int millisecondsBetweenTicks) {
    if (tick != null) {
      tick.cancel();
      tick = null;
    }
    if (millisecondsBetweenTicks > 0) {
      tick = new TimerTask() {
        @Override
        public void run() {
          tick();
        }
      };
      clock.scheduleAtFixedRate(tick, 0, millisecondsBetweenTicks);
    }
  }

  private void createMenus() {
    ActionListener modifier =
        new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            final String name = ((JMenuItem) e.getSource()).getName();
            final char toDo = name.charAt(0);

            if (toDo == 'T') tick();
            else startTicking(
                toDo == 'A' ? 500 :                // 매우 느림
                    toDo == 'S' ? 150 :            // 느림
                        toDo == 'M' ? 70 :         // 중간
                            toDo == 'F' ? 30 : 0   // 빠름
            );
          }
        };

    MenuSite.addLine(this, "Go", "Halt", modifier);
    MenuSite.addLine(this, "Go", "Tick (Single Step)", modifier);
    MenuSite.addLine(this, "Go", "Agonizing", modifier);
    MenuSite.addLine(this, "Go", "Slow", modifier);
    MenuSite.addLine(this, "Go", "Medium", modifier);
    MenuSite.addLine(this, "Go", "Fast", modifier);
  }

  public interface Listener {
    void tick();
  }

}
