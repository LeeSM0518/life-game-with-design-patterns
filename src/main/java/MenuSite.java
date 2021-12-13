import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 190p
public final class MenuSite {

  private static JFrame menuFrame = null;
  private static JMenuBar menuBar = null;

  private static Map requesters = new HashMap();

  private static Properties nameMap;

  private static final Pattern shortcutExtractor =
      Pattern.compile("\\s*([^;]+?)\\s*" + "(;\\s*([^\\s].*?))?\\s*$");

  private static final Pattern submenuExtractor =
      Pattern.compile("(.*?)(?::(.*?))?"
          + "(?::(.*?))?"
          + "(?::(.*?))?"
          + "(?::(.*?))?"
          + "(?::(.*?))?"
          + "(?::(.*?))?");

  private static final LinkedList<Object> menuBarContents = new LinkedList<>();

  private MenuSite() {
  }

  private static boolean valid() {
    assert menuFrame != null : "MenuSite not established";
    assert menuBar != null : "MenuSite not established";
    return true;
  }

  /**
   * 인자로 넘어온 JFrame에 메뉴를 적용시키는 메서드
   *
   * @param container 메인 프레임
   */
  public synchronized static void establish(JFrame container) {
    assert container != null;
    assert menuFrame == null : "Tried to establish more than one MenuSite";

    menuFrame = container;
    menuBar = new JMenuBar();
    menuFrame.setJMenuBar(menuBar);

    assert valid();
  }

  /**
   * 빈 메뉴를 생성한 후 메뉴 바에 추가하는 메서드
   * - 서브메뉴는 main:sub와 같은 문법을 통해 지정하면 된다.
   *
   * @param requester     메뉴를 소유하는 객체
   * @param menuSpecifier 생성할 메뉴
   */
  public static void addMenu(Object requester, String menuSpecifier) {
    createSubmenuByName(requester, menuSpecifier);
  }

  /**
   * 메뉴에 라인 아이템을 추가하는 메서드
   *
   * @param requester  라인 아이템을 추가해 달라고 요청한 객체
   * @param toThisMenu 어디에 라인 아이템을 추가할지를 알려주는 메뉴 지정자
   * @param name       아이템의 이름 (보이지 않는)
   * @param listener   메뉴 아이템이 선택되었을 때 통보할 ActionListener
   */
  public static void addLine(Object requester, String toThisMenu, String name, ActionListener listener) {
    assert requester != null : "null requester";
    assert name != null : "null item";
    assert toThisMenu != null : "null toThisMenu";
    assert valid();

    Component element;

    if (name.equals("-")) {
      element = new JSeparator();
    } else {
      assert listener != null : "null listener";

      JMenuItem lineItem = new JMenuItem(name);
      lineItem.setName(name);
      lineItem.addActionListener(listener);
      setLabelAndShortcut(lineItem);

      element = lineItem;
    }

    JMenu found = createSubmenuByName(requester, toThisMenu);
    if (found == null)
      throw new IllegalArgumentException("addLine() can't find menu (" + toThisMenu + ")");

    Item item = new Item(element, found, toThisMenu);
    menusAddedBy(requester).add(item);
    item.attachYourselfToYourParent();
  }

  /**
   * requester가 추가한 모든 아이템을 삭제하는 메서드
   *
   * @param requester 삭제를 요청한 객체
   */
  public static void removeMyMenus(Object requester) {
    assert requester != null;
    assert valid();

    Collection allItems = (Collection) requesters.remove(requester);

    if (allItems != null) {
      Iterator i = allItems.iterator();
      while (i.hasNext()) {
        Item current = (Item) i.next();
        current.detachYourselfFromYourParent();
      }
    }
  }

  public static void setEnable(Object requester, boolean enable) {
    assert requester != null;
    assert valid();

    Collection allItems = (Collection) requesters.get(requester);

    if (allItems != null) {
      Iterator i = allItems.iterator();
      while (i.hasNext()) {
        Item current = (Item) i.next();
        current.setEnableAttribute(enable);
      }
    }
  }

  public static JMenuItem getMyMenuItem(Object requester, String menuSpecifier, String name) {
    assert requester != null;
    assert menuSpecifier != null;
    assert valid();

    Collection allItems = (Collection) requesters.get(requester);

    if (allItems != null) {
      Iterator i = allItems.iterator();
      while (i.hasNext()) {
        Item current = (Item) i.next();
        if (current.specifiedBy(menuSpecifier)) {
          if (current.item() instanceof JSeparator) {
            continue;
          }
          if (name == null && current.item() instanceof JMenu) {
            return (JMenu) (current.item());
          }
          if (((JMenuItem) current.item()).getName().equals(name)) {
            return (JMenuItem) current.item();
          }
        }
      }
    }
    return null;
  }

  private static JMenu createSubmenuByName(Object requester, String menuSpecifier) {
    assert requester != null;
    assert menuSpecifier != null;
    assert valid();

    Matcher m = submenuExtractor.matcher(menuSpecifier);
    if (!m.matches()) {
      throw new IllegalArgumentException("Malformed menu specifier");
    }

    JMenuItem child = null;
    MenuElement parent = menuBar;
    String childName;

    for (int i = 1; (childName = m.group(i++)) != null; parent = child) {
      child = getSubmenuByName(childName, parent.getSubElements());

      if (child != null) {
        if (!(child instanceof JMenu)) {
          throw new IllegalArgumentException("Specifier identifies line item, not menu.");
        }
      } else {
        child = new JMenu(childName);
        child.setName(childName);
        setLabelAndShortcut(child);

        Item item = new Item(child, parent, menuSpecifier);
        menusAddedBy(requester).add(item);
        item.attachYourselfToYourParent();
      }
    }

    return (JMenu) child;
  }

  private static JMenuItem getSubmenuByName(String name, MenuElement[] contents) {
    JMenuItem found = null;
    for (int i = 0; found == null && i < contents.length; i++) {
      if (contents[i] instanceof JPopupMenu) {
        found = getSubmenuByName(name, contents[i].getSubElements());
      } else if (((JMenuItem) contents[i]).getName().equals(name)) {
        found = (JMenuItem) contents[i];
      }
    }
    return found;
  }

  public static void mapNames(URL table) throws IOException {
    if (nameMap == null) {
      nameMap = new Properties();
    }
    nameMap.load(table.openStream());
  }

  public static void addMapping(String name, String label, String shortcut) {
    if (nameMap == null) {
      nameMap = new Properties();
    }
    nameMap.put(name, label + ";" + shortcut);
  }

  private static void setLabelAndShortcut(JMenuItem item) {
    String name = item.getName();
    if (name == null) return;

    String label;
    if (nameMap != null && (label = (String) (nameMap.get(name))) != null) {
      Matcher m = shortcutExtractor.matcher(label);
      if (!m.matches()) {
        item.setText(name);
        Logger.getLogger("ui").warning(
            "Bad "
                + "name-to-label map entry:"
                + "\n\tinput=[" + name + "=" + label + "]"
                + "\n\tSetting label to " + name
        );
      } else {
        item.setText(m.group(1));

        String shortcut = m.group(3);

        if (shortcut != null) {
          if (shortcut.length() == 1) {
            item.setAccelerator(
                KeyStroke.getKeyStroke(
                    shortcut.toUpperCase().charAt(0),
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
                    false
                )
            );
          }
        } else {
          KeyStroke key = KeyStroke.getKeyStroke(shortcut);

          if (key != null) {
            item.setAccelerator(key);
          } else {
            Logger.getLogger("ui").warning(
                "Malformed shortcut parent specification "
                    + "in MenuSite map file: "
                    + shortcut
            );
          }
        }
      }
    }
  }

  private static Collection menusAddedBy(Object requester) {
    assert requester != null : "Bad argument";
    assert requesters != null : "No requesters";
    assert valid();

    Collection menus = (Collection) (requesters.get(requester));

    if (menus == null) {
      menus = new LinkedList();
      requesters.put(requester, menus);
    }
    return menus;
  }

  private static final class Item {
    private Component item;

    private String parentSpecification;
    private MenuElement parent;
    private boolean isHelpMenu;

    public String toString() {
      StringBuffer b = new StringBuffer(parentSpecification);

      if (item instanceof JMenuItem) {
        JMenuItem i = (JMenuItem) item;
        b.append(":");
        b.append(i.getName());
        b.append(" (");
        b.append(i.getText());
        b.append(")");
      }
      return b.toString();
    }

    private boolean valid() {
      assert item != null : "item is null";
      assert parent != null : "parent is null";
      return true;
    }

    public Item(Component item, MenuElement parent, String parentSpecification) {
      assert parent != null;
      assert parent instanceof JMenu || parent instanceof JMenuBar : "Parent must be JMenu or JMenuBar";

      this.item = item;
      this.parent = parent;
      this.parentSpecification = parentSpecification;
      this.isHelpMenu = (item instanceof JMenuItem) && (item.getName().compareToIgnoreCase("help") == 0);

      assert valid();
    }

    public boolean specifiedBy(String specifier) {
      return parentSpecification.equals(specifier);
    }

    public Component item() {
      return item;
    }

    public final void attachYourselfToYourParent() {
      assert valid();

      if (parent instanceof JMenu) {
        ((JMenu) parent).add(item);
      } else if (menuBarContents.size() <= 0) {
        menuBarContents.add(this);
        ((JMenuBar) parent).add(item);
      } else {
        Item last = (Item) (menuBarContents.getLast());
        if (!last.isHelpMenu) {
          menuBarContents.addLast(this);
          ((JMenuBar) parent).add(item);
        } else {
          menuBarContents.removeLast();
          menuBarContents.add(this);
          menuBarContents.add(last);

          if (parent == menuBar) {
            parent = regenerateMenuBar();
          }
        }
      }
    }

    public void detachYourselfFromYourParent() {
      assert valid();

      if (parent instanceof JMenu) {
        ((JMenu) parent).remove(item);
      } else {
        menuBar.remove(item);
        menuBarContents.remove(this);
        regenerateMenuBar();

        parent = null;
      }
    }

    public void setEnableAttribute(boolean on) {
      if (item instanceof JMenuItem) {
        JMenuItem item = (JMenuItem) this.item;
        item.setEnabled(on);
      }
    }

    private JMenuBar regenerateMenuBar() {
      assert valid();

      menuBar = new JMenuBar();

      ListIterator i = menuBarContents.listIterator(0);
      while (i.hasNext()) {
        menuBar.add(((Item) i.next()).item);
      }
      menuFrame.setJMenuBar(menuBar);
      menuFrame.setVisible(true);
      return menuBar;
    }

    private static class Debug {
      public interface Visitor {
        void visit(JMenu e, int depth);
      }

      private static int traversalDepth = -1;

      public static void visitPostorder(MenuElement me, Visitor v) {
        if (me.getClass() != JMenuItem.class) {
          final MenuElement[] contents = me.getSubElements();
          for (final MenuElement content : contents) {
            if (content.getClass() != JMenuItem.class) {
              ++traversalDepth;
              visitPostorder(content, v);
              if (!(content instanceof JPopupMenu)) {
                v.visit((JMenu) content, traversalDepth);
              }
              --traversalDepth;
            }
          }
        }
      }
    }

    public static class Test extends JFrame {
      static Test instance;
      static boolean isDisabled1 = false;
      static boolean isDisabled2 = false;

      Test() {
        setSize(400, 200);
        addWindowListener(
            new WindowAdapter() {
              @Override
              public void windowClosing(final WindowEvent e) {
                System.exit(1);
              }
            }
        );
        MenuSite.establish(this);
        show();
      }

      static class RemoveListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
          MenuSite.removeMyMenus(instance);
        }
      }

      static public void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        instance = new Test();

        ActionListener reportIt = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            JMenuItem item = (JMenuItem) (e.getSource());
            System.out.println(item.getText());
          }
        };

        ActionListener terminator = new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            System.exit(0);
          }
        };

        Object fileId = new Object();

        MenuSite.addMenu(fileId, "File");
        MenuSite.addLine(fileId, "File", "Quit", terminator);
        MenuSite.addLine(fileId, "File", "Bye", terminator);

        MenuSite.addMenu(instance, "Main");
        MenuSite.addLine(instance, "Main", "Add Line Item to Menu",
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                MenuSite.addLine(instance, "Main", "Remove Main and Help menus",
                    new ActionListener() {
                      @Override
                      public void actionPerformed(final ActionEvent e) {
                        MenuSite.removeMyMenus(instance);
                      }
                    });
              }
            });

        MenuSite.addLine(instance, "Main", "-", null);
        final Object disable1 = new Object();

        MenuSite.addLine(instance, "Main", "Toggle1",
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                isDisabled1 = !isDisabled1;
                MenuSite.setEnable(disable1, !isDisabled1);
                MenuSite.getMyMenuItem(instance, "Main", "Toggle1")
                    .setText(isDisabled1 ? "Enable following Item" : "Disable following Item");
              }
            });
        MenuSite.getMyMenuItem(instance, "Main", "Toggle1")
            .setText("Disable following Item");

        MenuSite.addLine(disable1, "Main", "Disableable", reportIt);

        final Object disable2 = new Object();

        MenuSite.addLine(instance, "Main", "Toggle2",
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                isDisabled2 = !isDisabled2;
                MenuSite.setEnable(disable2, !isDisabled2);
                MenuSite.getMyMenuItem(instance, "Main", "Toggle2")
                    .setText(isDisabled2 ? "Enable following Item" : "Disable following Item");
              }
            });

        MenuSite.getMyMenuItem(instance, "Main", "Toggle2")
            .setText("Disable following Item");

        MenuSite.addLine(disable2, "Main", "Disableable", reportIt);

        final Object id = new Object();

        MenuSite.addLine(id, "Main", "-", null);
        MenuSite.addLine(id, "Main", "Remove this item & separator line",
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent e) {
                MenuSite.removeMyMenus(id);
              }
            });

        MenuSite.addLine(instance, "Main", "-", null);
        MenuSite.addLine(instance, "Main:Submenu1", "Submenu One Item", reportIt);
        MenuSite.addLine(instance, "Main:Submenu2", "Submenu Two Item", reportIt);
        MenuSite.addLine(instance, "Main:Submenu3", "Submenu Three Item", reportIt);

        MenuSite.addLine(instance, "Main:Submenu2:SubSubmenu2",
            "Sub-Submenu Two Item", reportIt);

        MenuSite.addLine(instance, "Main:Submenu3:SubSubmenu3",
            "Sub-Submenu Three Item", reportIt);

        MenuSite.addLine(instance, "Main:Submenu3:SubSubmenu3:SubSubSubmenu3",
            "Sub-Sub-Submenu Three Item", reportIt);

        MenuSite.addLine(instance, "Main", "-", null);

//        MenuSite.mapNames(new );
      }
    }
  }

}
