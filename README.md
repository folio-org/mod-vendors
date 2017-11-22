# mod-vendor

Copyright (C) 2017 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

This module is responsible for the persistence of Vendor data (i.e. CRUD module).

This module leverages a relational DB and imports the following dependencies:

* RAML-Module Builder (RMB)
* JOOQ (a lightweight ORM)
* Flyway (to facilitate schema migration)


For additional information on the acquisitions-vendor-module, please refer to the [Vendor Module WIKI](https://wiki.folio.org/display/RM/Acquisitions+Vendor+Module).


For API documentation, run this project locally and then go to [http://localhost:8081/apidocs/index.html?raml=raml/vendor.raml](http://localhost:8081/apidocs/index.html?raml=raml/vendor.raml)


## Building the Project

This module uses a relational DB and takes a database-first approach.

_NOTE: JOOQ was configured to read the DB's schema to synthesize the corresponding POJOs. We then bridge this persistence model with the ones synthesized by RAML module builder (which models the API input/response)._


The database connection parameters needs to be configured in 2 places _(this will refactored so that we deal with only 1 file)_.

```
src/main/resources/db.properties
src/main/resources/postgres-conf.json
```

Once the parameters are set, the database can be initialized by running
```
mvn initialize flyway:migrate
```

The above command will create a hardcoded schema `mod-vendor` and the appropriate tables.

At this point, the project can be compiled. Run:
```
mvn clean install
```

## Schema Update Considerations

Since this module uses a relational DB, any schema migration will require the running of a SQL script. To facilitate this, the module uses Flyway.

All SQL migration scripts have to be stored in ```src/main/resources/db/migration``` and should follow the naming convention as per the [Flyway documentation](https://flywaydb.org/documentation/migration/versioned).

To run a migration:
```mvn initialize flyway:migrate```

