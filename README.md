
# pillar2-frontend
Front-end microservice for Pillar 2  project. Pillar 2 refers to the Global Minimum Tax being introduced by the Organisation for Economic Cooperation and Development (OECD).

The Pillar 2 Top-up Taxes will ensure that global Multinational Enterprises (MNEs) with a turnover of >€750m are subject to a minimum Effective Tax Rate of 15%, i.e. a top-up tax for Medium to Large MNEs.

## Running the service

You can use service manage to run all dependent microservices using the command below

    sm2 --start PILLAR2_ALL
    sm2 --stop PILLAR2_ALL
Or you could run this microservice locally using

    sbt run
Test-only route:

    sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10050'
To run locally:

Navigate to http://localhost:9949/auth-login-stub/gg-sign-in which redirects to auth-login-stub page.

#### Bank Account Reputation (BARS)
This service calls the BARS service within MDTP to verify business bank accounts as part of the pillar 2 repayments journey.
We call [the verify business endpoint](https://github.com/hmrc/bank-account-reputation/blob/main/public/api/conf/1.0/docs/business/verify.md) to verify accounts, 
because this is an external service in Local, Development and Staging we call directly call their stub. Information about handling BARS in different environments are detailed below
- Local, Development and Staging uses the [bank-account-reputation-stub](https://github.com/hmrc/bank-account-reputation-stub). Refer to [the stub README for test data usage](https://github.com/hmrc/bank-account-reputation-stub?tab=readme-ov-file#personal-account-test-data)
- QA environment, BARS is connected to their third parties test system. Any test data you use here will need to be aligned with the test data that the third party service holds
- Producation calls the MDTP service

***Redirect URL: http://localhost:10050/report-pillar2-top-up-taxes***

***Affinity Group: Organisation***
## Key Terminologies

### Ultimate Parent Entity (UPE):
An ultimate parent is not a subsidiary of any other company and has a controlling interest in one or more other entities.
### Nominated Filing Member (NFM):
The nominated filing member is responsible for managing the group's tax returns and keeping business records.
## Integration and unit tests

To run the unit tests:

    Run 'sbt test' from directory the project is stored in 

To check code coverage:

    sbt clean scalafmt test:scalafmt it/test coverage test it/test coverageReport   
To run Integration tests:

    sbt it/test

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
