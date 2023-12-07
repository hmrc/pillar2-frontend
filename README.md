# pillar2-frontend

This service provides the users with means to ensure the large multinational businesses pay a minimum
level of corporate income tax (15%) on the profits.

## Running the service locally

#### To compile the project:
The below command ensures the project is compiled without any errors

`sbt clean update compile`

#### To check code coverage:

`sbt scalafmt test:scalafmt it:test::scalafmt coverage test it:test coverageReport`

#### Integration and unit tests

To run the unit tests within the project:

`sbt test`

#### Starting the server in local
`sbt run`

By default, the service runs locally on port **10050**

To use test-only route locally, run the below:

`sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10050'`

### Using Service Manager

You can use service manager to provide necessary assets to the pillar2 backend.
**PILLAR2_ALL** service is responsible for starting up all the services required by the tax credits service project.

This can be started by running the below in a new terminal:

    sm2 --start PILLAR2_ALL

#### Using sbt

For local development, use `sbt run` but if it is already running in sm2, execute below command to stop the
service before running sbt commands.

    sm2 --stop PILLAR_2_FRONTEND

This is an authenticated service, so users first need to be authenticated via GG in order to use the service.

Navigate to http://localhost:9949/auth-login-stub/gg-sign-in which redirects to auth-login-stub page

Make sure to fill in the fields as below:

***Redirect URL: http://localhost:10050/report-pillar2-top-up-taxes***

***Affinity Group: Organisation***

### Testing endpoints

This frontend service provides a few test-only endpoints, exposed via the **GET** and
**POST** HTTP methods in order to be operated by the browser.

---------------------

```POST /stub-grs-journey-data```

Submits the `stub-grs-journey-data` collection

> Response status: 200

---------------------

```GET /stub-grs-journey-data```

Gets the `stub-grs-journey-data` collection posted previously

> Response status: 200

---------------------

```GET /get-all```

Gets all the records from the Stubs

> Response status: 200

---------------------

```GET /clear-all```

Clears all the records from the Stubs

> Response status: 200

---------------------

```GET /clear-all```

Clears all the records data from the Stubs

> Response status: 200

---------------------

```GET /clear-current```

Clears the current records data from the Stubs

> Response status: 200

---------------------

```GET /registration-data```

Gets the registration data from the Stubs

> Response status: 200

---------------------

```GET /eligibility/clear-session```

Clears the Eligibility journey session data from the Stubs

> Response status: 200

---------------------

```GET  /upsertRecord/:id```

Upsert's a record with ID to the Stubs

> Response status: 200

---------------------

```GET /de-enrol```

De-enrols the Pillar2 enrolment from the Stubs

> Response status: 200

---------------------
<br><br>

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").