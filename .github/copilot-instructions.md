# Phon Development Guide

Phon is a comprehensive phonological analysis software with a multi-tiered architecture built on Java 17+ and Maven. This guide covers essential patterns and workflows for AI coding agents.

## Project Architecture

### Module Structure

- **Multi-module Maven project** with ~24 modules organized by functionality
- **Core modules**: `core`, `session`, `project` (data models and business logic)
- **Processing modules**: `ipa`, `alignment`, `orthography`, `query` (phonological analysis)
- **UI modules**: `app`, `components` (Swing-based desktop application)
- **Integration modules**: `csv`, `xml`, `media` (data import/export)

### Key Architectural Patterns

#### Factory Pattern with SPI

```java
// Always use SessionFactory for creating session objects
SessionFactory factory = SessionFactory.newFactory();
Session session = factory.createSession(corpus, sessionName);
Record record = factory.createRecord();
Participant participant = factory.createParticipant();
```

#### Plugin System

- **Extension Points**: Implement `IPluginExtensionPoint<T>` for modular functionality
- **Annotations**: Use `@PhonPlugin`, `@EditorViewInfo`, `@Rank` for plugin metadata
- **Discovery**: Plugins auto-discovered via `META-INF/extpts/` files or ServiceLoader

#### Session-Record-Tier Hierarchy

```java
// Core data model: Project -> Corpus -> Session -> Record -> Tiers
Session session = project.openSession(corpus, sessionName);
Record record = session.getRecord(index);
Tier<IPATranscript> ipaTarget = record.getIPATargetTier();
```

## Development Workflows

### Building and Testing

```bash
# Build entire project
mvn clean install

# Run tests (JUnit-based, located in src/test/java)
mvn test

# Build specific module
cd session && mvn clean install
```

### Session Management Pattern

```java
// Always use write locks for session modifications
UUID writeLock = project.getSessionWriteLock(session);
try {
    // Modify session
    project.saveSession(session, writeLock);
} finally {
    project.releaseSessionWriteLock(session, writeLock);
}
```

### CSV Import/Export Pattern

- **CSVImporter**: Main entry point with column mapping (`CSVImportSettings`)
- **Tier handling**: Supports system tiers (IPA, Orthography) and user-defined tiers
- **Error handling**: Uses listener pattern for parsing errors and progress tracking

## Critical Conventions

### Participant Management

- Use `Participant.UNKNOWN` for unidentified speakers (never null)
- Participants cached at project level via `ParticipantCache` extension

### Tier System

- **System tiers**: Predefined types (`SystemTierType` enum)
- **User tiers**: Dynamic creation via `TierDescription`
- **Grouping**: Tiers can be grouped (multiple values per record)

### Extension Objects

- Core objects extend `ExtendableObject` for metadata/extensions
- Use `putExtension(Class, Object)` and `getExtension(Class)` pattern

### Plugin Development

```java
@PhonPlugin(name="My Plugin")
@EditorViewInfo(category=EditorViewCategory.UTILITIES, name="My View")
public class MyExtension implements IPluginExtensionPoint<EditorView> {
    @Override
    public Class<?> getExtensionType() { return EditorView.class; }

    @Override
    public IPluginExtensionFactory<EditorView> getFactory() {
        return (args) -> new MyEditorView((SessionEditor)args[0]);
    }
}
```

## Common Gotchas

1. **Thread Safety**: Session objects use `AtomicReference` and synchronized collections
2. **Lazy Loading**: XML sessions use `LazyRecord` implementations for performance
3. **Media Handling**: Sessions link to external media files via `MediaSegment`
4. **Validation**: Tiers can have `UnvalidatedValue` for parsing errors
5. **Serialization**: Sessions serialize to XML format with versioning support

## File Locations

- **Main application**: `app/src/main/java/ca/phon/app/`
- **Session models**: `session/src/main/java/ca/phon/session/`
- **Project management**: `project/src/main/java/ca/phon/project/`
- **Plugin examples**: Look for `*Extension.java` or `*ExtensionPoint.java` files
- **Tests**: Follow Maven convention `src/test/java/`
