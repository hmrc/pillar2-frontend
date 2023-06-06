
# pillar2-frontend

This service provides a means for users to ensure that large multinational businesses pay a minimum
level of corporate income tax (15%) on the profits.

## Using Service Manager

You can use service manager to provide assets to the frontend. the PILLAR2_ALL service is responsible for starting up all services required by the tax credits service project.
This can be start or stop by running:

    sm --start PILLAR2_ALL
    sm --stop PILLAR2_ALL


## Integration and unit tests

To run the unit tests:

    Run 'sbt test' from within the project

To check code coverage:

    sbt scalafmt test:scalafmt it:test::scalafmt coverage test it:test coverageReport


### Eligibility question

Eligibility questions journey start  with this url '/eligibility/group-in-multiple-territories' and there are four different questions to check eligibility.
User does not need to be authenticated for this journey.

Endpoint to start eligibility questions.

    /eligibility/group-in-multiple-territories


if all question asked in this journey answered with 'yes' then this mean you need to pay Global Minimum Tax, User will be redirected to  HMRC online services to register.


To use testonly route locally .

    sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10050'


### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").