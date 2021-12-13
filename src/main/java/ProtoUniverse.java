import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProtoUniverse extends JPanel implements Cell, MenuContributor {

  public void addMenus(JMenuBar menuBar) {
    JMenuItem clear = new JMenuItem("Clear");
    JMenuItem load = new JMenuItem("Load");
    JMenuItem store = new JMenuItem("Store");

    clear.setName("clear");
    load.setName("load");
    store.setName("store");

    ActionListener handler =
        new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            final String name = ((JMenuItem) e.getSource()).getName();
            switch (name.charAt(0)) {
              case 'c':
                clearGrid();
                break;
              case 'l':
                loadGrid();
                break;
              case 's':
                storeGrid();
                break;
            }
          }
        };

    clear.addActionListener(handler);
    load.addActionListener(handler);
    store.addActionListener(handler);

    JMenu grid = new JMenu("Grid");
    grid.add(clear);
    grid.add(load);
    grid.add(store);

    menuBar.add(grid);
  }

  private void clearGrid() {
    System.out.println("clear");
  }

  private void loadGrid() {
    System.out.println("load");
  }

  private void storeGrid() {
    System.out.println("store");
  }

  @Override
  public boolean figureNextState(final Cell north, final Cell south, final Cell east, final Cell west, final Cell northeast, final Cell northwest, final Cell southeast, final Cell southwest) {
    return false;
  }

  @Override
  public Cell edge(final int row, final int column) {
    return null;
  }

  @Override
  public boolean transition() {
    return false;
  }

  @Override
  public void redraw(final Graphics g, final Rectangle here, final boolean drawAll) {

  }

  @Override
  public void userClicked(final Point here, final Rectangle surface) {

  }

  @Override
  public boolean isAlive() {
    return false;
  }

  @Override
  public int widthInCells() {
    return 0;
  }

  @Override
  public Cell create() {
    return null;
  }

  @Override
  public Direction isDisruptiveTo() {
    return null;
  }

  @Override
  public void clear() {

  }

  @Override
  public boolean transfer(final Storable memento, final Point upperLeftCorner, final boolean doLoad) {
    return false;
  }

  @Override
  public Storable createMemento() {
    return null;
  }
}
