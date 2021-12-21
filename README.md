
# Eskimi Technical Task

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/71502e770eb141e5a2e06c2dfd6e648e)](https://app.codacy.com/gh/AndySakov/eskimi-technical-task?utm_source=github.com&utm_medium=referral&utm_content=AndySakov/eskimi-technical-task&utm_campaign=Badge_Grade_Settings)

A demo Bidding System by Obafemi Teminife

## API Reference

### Get all items

```http
  GET /ping
```

This pings the server to check if it's up

### Get item

```http
  POST /bid
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `string` | **Required**. Unique ID of the bid request, provided by the exchange. |
| `imp`      | `object[]` | **Required**. Array of Imp objects,  representing the impressions offered. At least 1 Imp object is required.|
| `site`      | `object` | **Required**. Details via a Site object about the publisher’s website.|
| `user`      | `object` | **Recommended**. Details via a User object about the human user of the device; the advertising audience.|
| `device`      | `object` | **Recommended**. Details via a Device object about the user’s device to which the impression will be delivered.|

## Object Definitions

Object definitions can be found in the `com.eskimi.api` package object

## Run Locally

Clone the project

```bash
  git clone https://github.com/AndySakov/eskimi-technical-task.git
```

Go to the project directory

```bash
  cd eskimi-technical-task
```

Start the server

```bash
  sbt run
```

## Testing and coverage

Run the tests included in the project

```bash
  sbt test 
```

Run the tests included in the project with coverage  
Coverage reports are stored in the `target/tmp/coverage-report` directory of the project

```bash
  sbt coverage test coverageReport
```
