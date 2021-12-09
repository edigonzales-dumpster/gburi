# gburi

## Bemerkungen:
- Annahme: Gemeindenummer (BFS-Nummer) entspricht den vier letzten Zeichen des NBIdents. Dies muss nicht zwingend sein. 
- Falls ein Grundstück mehrere Eigentümer hat, wird pro Eigentümer eine Zeile ins CSV geschrieben.
- Stockwerkeigentum o.ä. ist nicht sauber abgehandelt.
- `innerSpan` kann null sein.

## Run

```
sdk i jbang 0.83.1
```

```
jbang gburi.java
```

## Develop

```
jbang edit gburi.java
```