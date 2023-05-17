# InteractionTestExample
## The project
This project is meant for demonstration purposes. It shows how InterACt can be used to test the integration of component-based software systems.
It consists of three components:
- BankingComponent
- IbanValidator
- AmountValidator

Each of these components is implemented using Spring-Boot-Rest. The BankingComponent takes transfer requests and uses the other two components to validate the transfer request.
Two different flow were implemented. The v1 API calls the IbanValidator and AmountController one after the other, whereas the v2 API calls the IbanValidator which calls the ValueValidator in return. This can be seen in the sequence diagrams below:


## The tests
For each of the two flows, tests are provided on the component level. The test use mocks to test the functionality of each component in isolation. 
Utilizing InterACt these tests are observed and the observed data is stored such that InterACt can analyze these observations and retrieve interaction expectations from that data.
Tests for positive as well as negative testing are provided.
## Interaction testing with InterACt
To run interaction test you need to follow these steps:

- Install and start InterACt
- Run each Unit-Test suite once
- Change the interact.mode in the junit-properties.file to INTERACTION
- Re-execute the tests until all interaction expectations are validated

## Detecting integration faults
You can manipulate components and their tests such that the expectation of a component do not comply with the provided implementation of the other components.
E.g. change the regular expression in the IbanValidatorController such that it only accepts non formatted IBANs. Or change the amount that is used in the BankingComponents test to some value that is negative and would thus be rejected by the AmountValidator.
