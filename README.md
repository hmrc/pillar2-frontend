# pillar2-frontend

Front-end microservice for Pillar 2 project. Pillar 2 refers to the Global Minimum Tax being introduced by the
Organisation for Economic Cooperation and Development (OECD).

The Pillar 2 Top-up Taxes will ensure that global Multinational Enterprises (MNEs) with a turnover of >€750m are
subject to a minimum Effective Tax Rate of 15%, i.e. a top-up tax for Medium to Large MNEs.

## Running with Service Manager

Use [Service Manager](https://github.com/hmrc/sm2) to start the `PILLAR2_ALL` profile, that will run all dependent
microservices, with:
```shell
sm2 --start PILLAR2_ALL
```

Head to the [Authority Wizard](http://localhost:9949/auth-login-stub/gg-sign-in) to sign in and create a session for a
user with your choice of enrolments and tax ID.

To stop all services, run:
```shell
sm2 --stop PILLAR2_ALL
```


## Making Changes Locally
Start all the Pillar 2 services from Service Manager as mentioned above:
```shell
sm2 --start PILLAR2_ALL
```

Stop the `PILLAR_2_FRONTEND` service with:
```shell
sm2 --stop PILLAR_2_FRONTEND
```

Confirm that all dependent services but the `PILLAR_2_FRONTEND` are running with:
```shell
sm2 --status
```

Run Pillar 2 Frontend locally with:
```shell
sbt run
```

Head to the [Authority Wizard](http://localhost:9949/auth-login-stub/gg-sign-in) to sign in and create a session for a
user with your choice of enrolments and tax IDs. 


## Authority Wizard (GG Sign In)
When you sign in with the [Authority Wizard](http://localhost:9949/auth-login-stub/gg-sign-in), provide the following details:

- **Redirect URL**: http://localhost:10050/report-pillar2-top-up-taxes
- **Affinity Group**: Organisation


## Testing

Run unit tests with:
```shell
sbt test
```

Check code coverage with:
```shell
sbt clean coverage test it/test coverageReport
```

Run integration tests with:
```shell
sbt it/test
```

To use testonly route locally:
```shell
sbt 'run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes 10050'
```


## Bank Account Reputation (BARS)
This service calls the BARS service within MDTP to verify business bank accounts as part of the pillar 2 repayments
journey. We call [the verify business endpoint](https://github.com/hmrc/bank-account-reputation/blob/main/public/api/conf/1.0/docs/business/verify.md) to verify accounts, because this is an external service in Local,
Development and Staging we call directly call their stub. Information about handling BARS in different environments are
detailed below:
- Local, Development and Staging uses the [bank-account-reputation-stub](https://github.com/hmrc/bank-account-reputation-stub).
    Refer to [the stub README for test data usage](https://github.com/hmrc/bank-account-reputation-stub?tab=readme-ov-file#personal-account-test-data)
- QA environment, BARS is connected to their third parties test system. Any test data you use here will need to be
    aligned with the test data that the third party service holds
- Production calls the MDTP service


## Key Terminologies

### Ultimate Parent Entity (UPE):
An ultimate parent is not a subsidiary of any other company and has a controlling interest in one or more other entities.

### Nominated Filing Member (NFM):
The nominated filing member is responsible for managing the group's tax returns and keeping business records.


## Eligibility Questions

Eligibility questions journey starts at `/pillar-two/eligibility/group-in-multiple-territories` and there are four
different questions to check eligibility. User does not need to be authenticated for this journey.

If all question asked in this journey are answered with "Yes", then this means that you need to pay Global Minimum Tax,
and User will be redirected to HMRC online services to register.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
