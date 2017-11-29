# mod-vendor

Copyright (C) 2017 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

This RMB module is responsible for the persistence of Vendor data (i.e. CRUD module).

For additional information on the acquisitions-vendor-module, please refer to the [Vendor Module WIKI](https://wiki.folio.org/display/RM/Acquisitions+Vendor+Module).


## Building the Project

To compile this module, head to the root-folder and run the following command in your Terminal:

```
mvn clean install
```

To run the module in standalone mode (i.e. without involving Okapi):
```
java -jar target/mod-vendors-fat.jar -Dhttp.port=8081 embed_postgres=true
```

>Note that the above command launches an embedded Postgres server and is accessible using the default creds found in the *Credentials* section [here](https://github.com/folio-org/raml-module-builder).


Once up, access the module's API docs through the following links: 
* [Vendor APIs](http://localhost:8081/apidocs/index.html?raml=raml/vendor.raml)
* [Contact Category APIs](http://localhost:8081/apidocs/index.html?raml=raml/contact_category.raml)
* [Vendor Category APIs](http://localhost:8081/apidocs/index.html?raml=raml/vendor_category.raml)
