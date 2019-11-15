ICGC DCC - ID
===

The ID project is used by the [DCC Release](https://github.com/icgc-dcc/dcc-release), [DCC Repository](https://github.com/icgc-dcc/dcc-repository), and [SONG](https://github.com/icgc-dcc/SONG) to assign unique stable identifiers to entities of interest. 



Modules
---

Sub-system modules:

- [Core](dcc-id-core/README.md)
- [Client](dcc-id-client/README.md)
- [Server](dcc-id-server/README.md)

Build
---

From the command line:

```bash
mvn clean package
```

Docker
---

To start the server in secure production mode, with an initialized database and auth system, run the following:
```bash
make docker-start
```


The following are the default:

**username**:   `john.doe@example.com`

**userId**:      `c6608c3e-1181-4957-99c4-094493391096`

**accessToken**: `f69b726d-d40f-4261-b105-1ec7e6bf04d5`

The accessToken has `id.WRITE` scope.

The `dcc-id-server` api is running at http://localhost:8080
To access the swagger ui for EGO (auth), visit http://localhost:9082/swagger-ui.html.

Example curl command: `curl -v --header 'Authorization: Bearer f69b726d-d40f-4261-b105-1ec7e6bf04d5' 'http://localhost:8080/donor/id?submittedDonorId=myDonor1&submittedProjectId=myProj'`

The donor, specimen, sample, object, and analysis endpoints can be tested via the following targets:

- test-donor
- test-specimen
- test-sample
- test-object
- test-analysis-get
- test-analysis-unique
- test-analysis-get

For more targets, refer to the `Makefile`


Copyright and license
---

 - [License](LICENSE.md)



