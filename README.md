# Bakuretsu ORM IntelliJ Plugin

This repository contains core components for an IntelliJ plugin designed to enhance Bakuretsu ORM support inside the IDE, providing intelligent code completion, navigation, and validation for ORM relation chains.

---

## Overview

This plugin provides advanced support for **Bakuretsu ORM** relation chains inside IntelliJ:

* **Smart completion** of nested ORM relations in string literals.
* **Clickable references** for ORM relation paths, enabling quick navigation.
* Recursive resolution of complex relation chains.
* User-friendly notifications for plugin events.

---

## Features

### Relation Chain Navigation

Bakuretsu ORM defines relations between models as strings, for example:

```java
query.with("user.posts.comments");
```

This plugin lets you:

* Ctrl+Click any part of the relation chain (`user`, `posts`, `comments`) to navigate to the corresponding model field.
* See visual hints and validation for relation names in string literals.
* Navigate deeply nested relations safely with cycle detection.

### Relation Completion

When editing a relation string, the plugin offers auto-completion suggestions based on:

* Fields annotated as relations in your Bakuretsu model classes.
* Recursive traversal of relation chains, providing suggestions for nested relations.

---

## How It Works

### Key Components

* **OrmRelationReferenceProvider**
  Detects string literals representing relation chains and splits them into parts, creating references for each segment.

* **OrmRelationReference**
  Resolves each part of the relation chain by traversing the related model classes and their annotated fields.

* **Util**
  Utility methods to resolve relation classes and show notifications within IntelliJ.

---

## Installation

Include the plugin in your IntelliJ setup via:

* Build from source using Gradle (requires IntelliJ Platform SDK).
* Install the generated plugin ZIP in IntelliJ Plugins settings.

---

## Usage

Write your Bakuretsu ORM query chains like:

```java
query.with("author.books.reviews");
```

* The plugin underlines the relation strings.
* Press Ctrl+Click on `books` to jump to the `books` field in your `author` model.
* While typing, get auto-completion suggestions for available relations at each chain level.

---

## Development

### Requirements

* Java 17
* IntelliJ Platform SDK
* Gradle build system

### Build & Run

* Import the project into IntelliJ.
* Run the plugin in sandbox mode to test.
* Use JUnit tests for validation.

---

## Contributing

Feel free to contribute fixes, improvements, or features. Open issues or submit pull requests.

---

## License

This project is licensed under the MIT License.
