# Rozdeľovanie komplikovaných elementov na mapách
_Decomposition of complex map elements_

This project analyses orienteering maps in the **.omap** format (ISSprOM-2019, created in **OpenOrienteering Mapper**) and classifies area objects into **simple** and **complex** shapes.  
The tool is a first step towards preparing map data for shortest-path algorithms.

---

## How it works

Main class: `mapanalyzer.MapComplexFilterZima` (in `src/mapanalyzer/MapComplexFilterZima.java`).

The program:

1. Reads a `.omap` file (XML).
2. Builds a symbol table from `<symbol>` elements and detects **area symbols** (`<area_symbol>`).
3. For each map `<object>` that uses an area symbol:
    - parses its `<coords>` into a polygon,
    - decides whether the polygon is **simple** or **complex**.

Current definition:

- **Simple shape** – a polygon that can be decomposed into exactly one triangle  
  (effectively: three unique vertices, optional repeated first/last point).
- **Complex shape** – any other area polygon.

At the end, the program prints a short summary:

- number of analyzed area objects,
- number of simple area objects,
- number of complex area objects.
