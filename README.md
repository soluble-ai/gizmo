![Gizmo](https://raw.githubusercontent.com/rebar-cloud/gizmo/master/.assets/gizmo.png)
# Gizmo

Gizmo is a Web UI that makes it easy to interact with [TinkerPop](http://tinkerpop.apache.org/)  graph databases such as AWS 
[Neptune](https://aws.amazon.com/neptune) and Azure [CosmosDB](https://azure.microsoft.com/en-us/services/cosmos-db/) with the [Cypher query language](https://neo4j.com/developer/cypher-query-language/) .

It may be  a matter of preference, but I find Cypher to be substantially more natural and expressive than the [Gremlin query language](https://tinkerpop.apache.org/gremlin.html) that is native to TinkerPop graph databases.


# Usage

The easiest way to run the console is via docker:

```docker run -it -p 8080:8080 rebar/gizmo```

Point your browser to http://localhost:8080 to use Gizmo.

If you do not specify a `GREMLIN_URL` docker environment variable, Gizmo will try to locate a database using a discovery procedure descibed below.  


## Database URL


To specify a database server explicitly, simply add `-e GREMLIN_URL=<url>` to your docker options.

For instance:

```docker run -it -p 8080:8080 -e GREMLIN_URL=neptune://myserver:8182 rebar/gizmo```

These URLs can take the form:

|GREMLIN_URL | Explanation |
|----|---|
| neptune://<host>:<port> | Connect to neptune on the given host and port. The Gremlin Cypher driver will automatically be set for TranslatorFlavor.neptune() |
| cosmos://<host>:<port> | Connect to neptune on the given host and port. The Gremlin Cypher driver will automatically be set for TranslatorFlavor.cosmosDb() |
| gremlin://<host>:<port> | Connect to a standard Gremlin server on the given host and port|



If  ```GREMLIN_URL``` is specified as an environmental variable, Gizmo will honor it.  

For instance, if your Neptune endpoint is:

```test.cluster-cztds9npdixn.us-west-2.neptune.amazonaws.com:8182``` 

you would specifiy this as:

```GREMLIN_URL=neptune://test.cluster-cztds9npdixn.us-west-2.neptune.amazonaws.com:8182```

Cosmos or other Gremlin-enbaled TinkerPop databases can be specified by using `cosmos://<host>:<port>` or `gremlin://<host>:<port>` syntax respectively.



## Database Discovery

Additionally, Gizmo will try to auto-discover endpoints according to the procedure in the following table.  This is purely a convenience.

|Precedence | Config | Explanation |
|--|--|--|
|Highest | ```GREMLIN_URL``` specified as an environment variable or `gremlin.url` set as a system property | The specified url will be honored. |
|  | ```neptune://localhost:8182```| Gizmo will try to connect to a neptune endpoint on localhost:8182.  This is useful if you are using SSH port forwarding to connect to Neptune from outside your VPC. |
|  | ```neptune://docker.host.internal:8182```| Gizmo will try to connect to Neptune on your docker host.  This is useful if you are using SSH port forwarding on your laptop to connect to Neptune from outside your VPC. |
| Lowest | Discover Neptune Endpoint | Gizmo will attempt to connect to AWS and enumerate available Neptune instances.  It will choose the first available Neptune cluster. |

# Tips

##  Gizmo w/Neptune Outside VPC

Neptune servers are only available from inside your VPC.  If you do not have direct connectivity to your VPC from your dev environment, you can use the following techniques:

### SSH Port Forwarding

The follwing will forward the local port 8182 to port 8182 on `NEPTUNE_HOST`:

```ssh -L 0.0.0.0:8182:<NEPTUNE_HOST> <user>@<jumpbox>```

WIth this SSH session established, you can use Gizmo with Neptune as if Gizmo was on the same network as your Neptune server.

### SSHuttle

[sshuttle](https://github.com/sshuttle/sshuttle) is a clever tool that provides transpartent VPN-like capability through an SSH tunnel.

If you set up sshuttle, Gizmo can discover and connect to your Neptune servers as if you have direct connectivity.

# Limitations

* Gizmo relies on the [Cypher-to-Gremlin translator](https://github.com/opencypher/cypher-for-gremlin), which does not support all Cypher features.
* The UI is very rudimentary and I am not a UI genius.  I just want to be able to type in Cypher and have it execute against AWS Neptune.
* AWS Neptune signed requests are not supported
* I have not yet tried this against any graph db *other than* AWS Neptune

