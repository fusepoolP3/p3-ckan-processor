# CKAN Processor
A RESTful webservice for having the data sets of CKAN RDF graphs transformed and written in LDP.

## Install

    mvn clean install exec:java

## Usage
Currenly the only way to use the application is through its REST endpoint.

    curl -X POST -d "dataSet=http://cot-test.infotn.it/dataset" "http://localhost:7100/endpoint/process/"
    Request was accepted and process started in backgroud.

## Data flow
![Data flow diagram](https://raw.githubusercontent.com/fusepoolP3/p3-ckan-processor/master/process.png)

## Issues
* Currenly it has no user interface, just a blank welcome page.
* No feedback on whether the request successfully finished or failed.
* LDPC names are sometimes differ from the original p3:LDPC label.

## References
This application implements the requirements in [FP-327](https://fusepool.atlassian.net/browse/FP-327).
