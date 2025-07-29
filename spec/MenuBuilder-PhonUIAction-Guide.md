# MenuBuilder & PhonUIAction System Guide

This document explains how the `MenuBuilder` and `PhonUIAction` systems work in Phon, providing a comprehensive guide for developers working with the UI action and menu system.

## Overview

Phon uses two key classes to create dynamic, modular user interfaces:

1. **`MenuBuilder`** - A path-based menu construction system
2. **`PhonUIAction`** - A functional action system that extends Swing's `AbstractAction`

These systems work together to create flexible, maintainable menus and UI actions throughout the application.

## MenuBuilder System

### Core Concepts

`MenuBuilder` (`ca.phon.ui.menu.MenuBuilder`) provides a path-based approach to building Swing menus. Instead of manually creating menu hierarchies, you specify where items should be placed using string paths.

### Path Syntax

Menu items are addressed using paths - alphanumeric sequences separated by `/`:

```text
File                    # The File menu
View/Record Data        # Record Data submenu under View
Edit/Undo              # Undo item under Edit menu
```

### Positioning Control

You can control where items are inserted using the `@` token:

- `@^` - Place at beginning
- `@$` - Place at end (default)
- `@<item_name>` - Place after the named item
- `@<number>` - Place at specific index

#### Examples

```java
// Add to beginning of File menu
builder.addItem("File@^", "New Project");

// Add after "Open" item
builder.addItem("File@Open", "Recent Files");

// Add to end of menu (default)
builder.addItem("File", "Exit");
```

### Basic Usage

```java
// Create a MenuBuilder attached to a JMenuBar
MenuBuilder builder = new MenuBuilder(menuBar);

// Add menus
JMenu fileMenu = builder.addMenu(".", "File");
JMenu editMenu = builder.addMenu(".@File", "Edit");

// Add menu items
builder.addItem("File", "Open Project");
builder.addItem("File", "Save Project");
builder.addSeparator("File", "file_separator");
builder.addItem("File", "Exit");

// Add items with Actions
builder.addItem("Edit", new UndoAction());
builder.addItem("Edit", new RedoAction());
```

### Advanced Features

#### Nested Menu Creation

```java
// Creates View menu and Record Data submenu if they don't exist
builder.addItem("View/Record Data", "My Custom Item");
```

#### Appending Subitems

```java
// Copy all items from one menu to another
JMenu sourceMenu = existingMenu;
builder.appendSubItems("Target Menu", sourceMenu);
```

#### Working with Existing Menus

```java
// Get reference to existing menu
JMenu helpMenu = builder.getMenu("Help");
if (helpMenu != null) {
    // Modify existing menu
}
```

### Real-World Example

Here's how the default menu system adds the File menu:

```java
protected void addFileMenu(Window owner, JMenuBar menu) {
    final MenuBuilder builder = new MenuBuilder(menu);
    final JMenu fileMenu = builder.addMenu(".@^", "File");

    fileMenu.add(new OpenFileEP());

    JMenu recentFilesMenu = new JMenu("Recent files");
    recentFilesMenu.addMenuListener(new RecentFilesMenuListener());
    fileMenu.add(recentFilesMenu);

    fileMenu.addSeparator();

    fileMenu.add(new NewProjectCommand());
    fileMenu.add(new OpenProjectCommand());

    final JMenu recentProjectsMenu = new JMenu("Recent Projects");
    recentProjectsMenu.addMenuListener(new RecentProjectsMenuListener());
    fileMenu.add(recentProjectsMenu);
}
```

## PhonUIAction System

### Action Framework

`PhonUIAction` (`ca.phon.ui.action.PhonUIAction`) is a generic action class that supports functional programming patterns. It extends `AbstractAction` and provides several creation patterns.

### Action Types

#### 1. Runnable Actions

For simple actions without parameters:

```java
PhonUIAction<Void> action = PhonUIAction.runnable(() -> {
    System.out.println("Action executed!");
});
```

#### 2. Consumer Actions

For actions that need data:

```java
PhonUIAction<String> action = PhonUIAction.consumer(
    text -> System.out.println("Processing: " + text),
    "Hello World"
);
```

#### 3. Event Consumer Actions

For actions that need access to the ActionEvent:

```java
PhonUIAction<Void> action = PhonUIAction.eventConsumer(this::onClearText);

public void onClearText(PhonActionEvent<Void> pae) {
    queryField.setText("");
    firePropertyChange("text_cleared", false, true);
}
```

#### 4. Event Consumer with Data

For actions that need both event and data:

```java
PhonUIAction<Integer> action = PhonUIAction.eventConsumer(
    this::onSetVolume,
    75  // volume level
);

public void onSetVolume(PhonActionEvent<Integer> pae) {
    Integer volume = pae.getData();
    // Set volume to specified level
}
```

### PhonActionEvent

`PhonActionEvent<T>` wraps standard `ActionEvent` and adds typed data:

```java
public class PhonActionEvent<T> {
    public ActionEvent getActionEvent() { /* ... */ }
    public T getData() { /* ... */ }
    public void setData(T data) { /* ... */ }
}
```

### Background Execution

Actions can be configured to run in background threads:

```java
PhonUIAction<Void> action = PhonUIAction.runnable(this::longRunningTask);
action.setRunInBackground(true);
action.setWorkerThread(customWorker); // Optional custom worker
```

### Action Properties

Like standard Swing actions, you can set properties:

```java
PhonUIAction<Void> action = PhonUIAction.runnable(this::performAction);
action.putValue(PhonUIAction.NAME, "My Action");
action.putValue(PhonUIAction.SHORT_DESCRIPTION, "Does something useful");
action.putValue(PhonUIAction.SMALL_ICON, myIcon);
action.putValue(PhonUIAction.ACCELERATOR_KEY,
    KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
```

## Integration Patterns

### Using MenuBuilder with PhonUIAction

```java
public void setupPopupMenu(JPopupMenu menu) {
    final MenuBuilder menuBuilder = new MenuBuilder(menu);

    // Add custom items via menu handler
    if (menuHandler != null) {
        menuHandler.setupMenu(menuBuilder);
    }

    // Add separator before clear action
    if (menu.getComponentCount() > 0) {
        menuBuilder.addSeparator(".", "clear");
    }

    // Create and add clear action
    PhonUIAction<Void> clearFieldAct = PhonUIAction.eventConsumer(this::onClearText);
    clearFieldAct.putValue(PhonUIAction.NAME, "Clear text");
    JMenuItem clearTextItem = new JMenuItem(clearFieldAct);
    menuBuilder.addItem(".", clearTextItem);
}
```

### Dynamic Menu Building

Here's an example from the Query menu system:

```java
public void menuSelected(MenuEvent e) {
    final JMenu queryMenu = (JMenu)e.getSource();
    queryMenu.removeAll();

    // Get current project
    final Project project = CommonModuleFrame.getCurrentFrame()
        .getExtension(Project.class);

    // Add stock scripts
    final QueryScriptLibrary library = new QueryScriptLibrary();
    final ResourceLoader<QueryScript> stockLoader = library.stockScriptFiles();

    for(QueryScript script : stockLoader) {
        final JMenuItem item = new JMenuItem(new QueryScriptCommand(project, script));
        queryMenu.add(item);
    }

    queryMenu.addSeparator();

    // Add browse action
    final PhonUIAction<Project> browseAct = PhonUIAction.eventConsumer(
        QueryMenuListener::onBrowseForQuery, project);
    browseAct.putValue(PhonUIAction.NAME, "Browse...");
    browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Browse for query...");
    final JMenuItem browseItem = new JMenuItem(browseAct);

    queryMenu.add(browseItem);
}
```

### KeyBinding Integration

PhonUIAction works seamlessly with Swing's input/action maps:

```java
private void setupActions() {
    ActionMap actionMap = component.getActionMap();
    InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);

    // Create action
    PhonUIAction<Void> focusNextAct = PhonUIAction.eventConsumer(this::focusNextPhone);
    actionMap.put("FOCUS_NEXT", focusNextAct);

    // Bind to keystroke
    KeyStroke focusNextKs = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
    inputMap.put(focusNextKs, "FOCUS_NEXT");

    // Action with data
    PhonUIAction<Integer> newSegmentAct = PhonUIAction.eventConsumer(
        this::newSegment, speakerIndex);
    actionMap.put("segment_speaker1", newSegmentAct);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "segment_speaker1");
}
```

## Plugin Integration

### Menu Filters

Plugins can modify menus using `IPluginMenuFilter`:

```java
@PhonPlugin(name="My Plugin")
public class MyMenuFilter implements IPluginMenuFilter {
    @Override
    public void filterWindowMenu(Window owner, JMenuBar menuBar) {
        final MenuBuilder builder = new MenuBuilder(menuBar);

        // Add custom menu
        JMenu myMenu = builder.addMenu(".@Tools", "My Plugin");

        // Add actions
        PhonUIAction<Void> myAction = PhonUIAction.runnable(this::doSomething);
        myAction.putValue(PhonUIAction.NAME, "Do Something");
        builder.addItem("My Plugin", myAction);
    }
}
```

### Custom Menu Handlers

For more complex menu customization:

```java
public interface SearchFieldMenuHandler {
    void setupMenu(MenuBuilder menu);
}

// Usage
searchField.setMenuHandler(new SearchFieldMenuHandler() {
    @Override
    public void setupMenu(MenuBuilder menu) {
        // Add custom items to search field context menu
        PhonUIAction<Void> customAction = PhonUIAction.runnable(() -> {
            // Custom functionality
        });
        customAction.putValue(PhonUIAction.NAME, "Custom Option");
        menu.addItem(".", customAction);
    }
});
```

## Best Practices

### MenuBuilder

1. **Use meaningful paths**: `"Edit/Find/Find Next"` is better than `"item1/item2/item3"`
2. **Use positioning strategically**: Place important items using `@^` or `@<reference>`
3. **Name separators**: Always provide names for separators for later reference
4. **Handle existing menus**: Check if menus exist before modifying them

### PhonUIAction

1. **Choose the right action type**: Use `runnable` for simple actions, `eventConsumer` when you need event data
2. **Set descriptive properties**: Always set `NAME` and `SHORT_DESCRIPTION`
3. **Consider background execution**: Use `setRunInBackground(true)` for long-running operations
4. **Type your data**: Use specific types rather than `Object` for better type safety

### Integration

1. **Separate concerns**: Keep menu structure logic separate from action logic
2. **Use dependency injection**: Pass required dependencies to action constructors
3. **Handle errors gracefully**: Wrap action code in try-catch blocks
4. **Test menu paths**: Verify that menu paths work correctly during development

## Common Patterns

### Modal Dialog with Actions

```java
public class MyDialog extends JDialog {
    private void setupButtons() {
        PhonUIAction<Void> okAction = PhonUIAction.eventConsumer(this::onOk);
        okAction.putValue(PhonUIAction.NAME, "OK");

        PhonUIAction<Void> cancelAction = PhonUIAction.eventConsumer(this::onCancel);
        cancelAction.putValue(PhonUIAction.NAME, "Cancel");

        JButton okButton = new JButton(okAction);
        JButton cancelButton = new JButton(cancelAction);

        // Use ButtonBarBuilder for proper platform layout
        JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
        add(buttonBar, BorderLayout.SOUTH);
    }
}
```

### Context-Sensitive Menus

```java
private JPopupMenu createContextMenu(MyObject context) {
    JPopupMenu menu = new JPopupMenu();
    MenuBuilder builder = new MenuBuilder(menu);

    if (context.canEdit()) {
        PhonUIAction<MyObject> editAction = PhonUIAction.eventConsumer(
            this::onEdit, context);
        editAction.putValue(PhonUIAction.NAME, "Edit");
        builder.addItem(".", editAction);
    }

    if (context.canDelete()) {
        PhonUIAction<MyObject> deleteAction = PhonUIAction.eventConsumer(
            this::onDelete, context);
        deleteAction.putValue(PhonUIAction.NAME, "Delete");
        builder.addItem(".", deleteAction);
    }

    return menu;
}
```

This documentation provides a comprehensive guide to using the MenuBuilder and PhonUIAction systems effectively in Phon development.
