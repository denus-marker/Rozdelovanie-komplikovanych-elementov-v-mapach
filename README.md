# Rozdeľovanie komplikovaných elementov na mapách
_Decomposition of complex map elements_

This repository contains the **winter semester** part of a larger project
for analysing orienteering maps in the **.omap** format (ISSprOM-2019,
created in **OpenOrienteering Mapper**).

The current version focuses on:

- loading a `.omap` file,
- finding **area objects**,
- classifying them into **simple** and **complex** shapes,
- printing basic statistics.

In the **summer semester** the project will be extended with algorithms
that actually **split complex elements** and prepare data for
**shortest-path computation**.

---

## How it works (winter semester version)

Main class: `mapanalyzer.MapComplexFilterZima`
(in `src/mapanalyzer/MapComplexFilterZima.java`).

The program:

1. Reads a `.omap` file (XML).
2. Builds a symbol table from `<symbol>` elements and detects
   **area symbols** (`<area_symbol>`).
3. For each map `<object>` that uses an area symbol:
    - parses its `<coords>` into a polygon,
    - decides whether the polygon is **simple** or **complex**.

Current definition:

- **Simple shape** – a polygon represented by three coordinate points (triangle),  
  with an optional repeated first/last point to close the polygon.
- **Complex shape** – any other area polygon.

At the end, the program prints a short summary:

- number of analyzed area objects,
- number of simple area objects,
- number of complex area objects.

---

## Tests

JUnit 5 tests are located in the `tests` folder:

- `tests/mapanalyzer/MapComplexFilterZimaTest.java` – unit tests for
  the shape classification logic.
- `tests/mapanalyzer/MapComplexFilterZimaIntegrationTest.java` – small
  integration test that runs the main program on a generated `.omap` file.

More functionality will be added and tested in the **summer semester** phase.
