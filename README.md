# adifweb
Spring Boot Web Front End to the ADIF Processor.

```mermaid
flowchart TD
A[Input File] --> |ADIF/CSV| B(ADIF Processor)
B --> |KML| D[Google Earth]
B --> |ADIF| C[Augmented ADIF]
B --> |MD/TXT| E[Contacts List]
B --> |TXT| F[QSL Labels]
G[Form Options] --> B
```

[Main Documentation Page](https://urbancamo.github.io/adif-processor/adif-processor)
