import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 207p
public class Universe extends JPanel {

  private final Cell outermostCell;
  private static final Universe theInstance = new Universe();

  private static final int DEFAULT_GRID_SIZE = 8;
  private static final int DEFAULT_CELL_SIZE = 8;

  private Universe() {
    outermostCell = new Neighborhood(
        DEFAULT_CELL_SIZE,
        new Neighborhood(
            DEFAULT_GRID_SIZE,
            new Resident()
        )
    );

    final Dimension PREFERRED_SIZE = new Dimension(
        outermostCell.widthInCells() * DEFAULT_CELL_SIZE,
        outermostCell.widthInCells() * DEFAULT_CELL_SIZE
    );

    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(final ComponentEvent e) {
            final Rectangle bounds = getBounds();
            bounds.height /= outermostCell.widthInCells();
            bounds.height *= outermostCell.widthInCells();
            bounds.width = bounds.height;
            setBounds(bounds);
          }
        }
    );

    setBackground(Color.white);
    setPreferredSize(PREFERRED_SIZE);
    setMaximumSize(PREFERRED_SIZE);
    setMinimumSize(PREFERRED_SIZE);
    setOpaque(true);

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(final MouseEvent e) {
            final Rectangle bounds = getBounds();
            bounds.x = 0;
            bounds.y = 0;
            outermostCell.userClicked(e.getPoint(), bounds);
            repaint();
          }
        }
    );

    MenuSite.addLine(this, "Grid", "Clear", new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        repaint();
      }
    });

//    MenuSite.addLine(this, "Grid", "Load", new ActionListener() {
//      @Override
//      public void actionPerformed(final ActionEvent e) {
//        doLoad();
//      }
//    });
//
//    MenuSite.addLine(this, "Grid", "Store", new ActionListener() {
//      @Override
//      public void actionPerformed(final ActionEvent e) {
//        doStore();
//      }
//    });

    MenuSite.addLine(this, "Grid", "Exit", new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        System.exit(0);
      }
    });

    Clock.instance().addClockListener(new Clock.Listener() {
      @Override
      public void tick() {
        if (outermostCell.figureNextState(
            Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
            Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY
        )) {
          if (outermostCell.transition()) {
            refreshNow();
          }
        }
      }
    });
  }

  public static Universe instance() {
    return theInstance;
  }

//  private void doLoad() {
//    try {
//      final FileInputStream in = new FileInputStream(Files.userSelected(".", ".life", "Life File", "Load"));
//
//      Clock.instance().stop();
//      outermostCell.clear();
//
//      Storable memento = outermostCell.createMemento();
//      memento.load(in);
//      outermostCell.transfer(memento, new Point(0, 0), Cell.LOAD);
//      in.close();
//    } catch (IOException e) {
//      JOptionPane.showMessageDialog(null, "Read Failed!",
//          "The Game of Life", JOptionPane.ERROR_MESSAGE);
//      repaint();
//    }
//  }
//
//  private void doStore() {
//    try {
//      final FileOutputStream out = new FileOutputStream(Files.userSelected(".", ".life", "Life File", "Write"));
//
//      Clock.instance().stop();
//
//      Storable memento = outermostCell.createMemento();
//      outermostCell.transfer(memento, new Point(0, 0), Cell.STORE);
//      memento.flush(out);
//
//      out.close();
//    } catch (IOException e) {
//      JOptionPane.showMessageDialog(null, "Write Failed!",
//          "The Game of Life", JOptionPane.ERROR_MESSAGE);
//    }
//  }

  public void paint(Graphics g) {
    final Rectangle panelBounds = getBounds();
    final Rectangle clipBounds = g.getClipBounds();

    panelBounds.x = 0;
    panelBounds.y = 0;
    outermostCell.redraw(g, panelBounds, true);
  }

  private void refreshNow() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final Graphics g = getGraphics();
        if (g == null) return;
        try {
          final Rectangle panelBounds = getBounds();
          panelBounds.x = 0;
          panelBounds.y = 0;
          outermostCell.redraw(g, panelBounds, false);
        } finally {
          g.dispose();
        }
      }
    });
  }

}
