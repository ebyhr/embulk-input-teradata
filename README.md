# Teradata input plugin for Embulk

Connect teradata server with jdbc

## Overview

* **Plugin type**: input
* **Resume supported**: no
* **Cleanup supported**: no
* **Guess supported**: no

## Configuration

- **host**: description (string, required)
- **user**: description (string, required)
- **password**: description (string, required)
- **database**: description (string, required)

## Example

```yaml
in:
  type: teradata
  driver_class: com.teradata.jdbc.TeraDriver
  host: 127.0.0.1
  user: dbc
  password: "dbc"
  database: dbc
  query: select * from dbc.tables;
```


## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
