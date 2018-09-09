# Gizmo

Gizmo is a Web UI that makes it easy to interact with Gremlin-enabled graph databases like AWS Neptune
and Azure CosmosDB.

It uses the [Cypher](https://neo4j.com/developer/cypher-query-language/) as the query language for interacting with the underlying Graph DB. 

I have used [Neo4j](https://neo4j.com) for years and find Cypher to be very natural and expressive.  While the 
[Gremlin query language](https://tinkerpop.apache.org/gremlin.html) is powerful and good for programmatic use, 
it is not natural for interactive use by a human being.  The 
[Gremlin Console](http://tinkerpop.apache.org/docs/current/tutorials/the-gremlin-console/) CLI is a bit
primitive and clunky to use (at least comparison to Neo4j's [cypher-shell](https://neo4j.com/docs/operations-manual/current/tools/cypher-shell/)).

So I wrote Gizmo to ease my Neo4j withdrawl.  It's nowhere near as good as Neo4j's Console, but it is at least
usable.

# Usage

The easiest way to run the console is via docker:

```docker run -it -e GREMLIN_URL=<GREMLIN_URL> rebar/gizmo```

```GREMLIN_URL``` should be of the form: ```//<host>:<port>```
  

## AWS Neptune

If you have a Neptune server running here:

```neptune-1.cavmsnxuuwai.us-west-2.neptune.amazonaws.com:8182```

Then you would launch the TinkerPop Console like this:

```shell
docker run -it \
  -e GREMLIN_URL=//neptune-1.cavmsnxuuwai.us-west-2.neptune.amazonaws.com:8182 \
  -p 8080:8080 \
  rebar/tinkerpop-console
```

Then point your browser to http://localhost:8080 and have fun!


# Limitations

* Gizmo relies on the [Cypher-to-Gremlin translator](https://github.com/opencypher/cypher-for-gremlin), which does not support all Cypher features.
* The UI is very rudimentary and I am not a UI genius.  I just want to be able to type in Cypher and have it execute against AWS Neptune.
* AWS Neptune signed requests are not supported
* I have not yet tried this against any graph db *other than* AWS Neptune

