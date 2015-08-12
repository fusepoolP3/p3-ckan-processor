# CKAN Processor
A RESTful webservice for having the data sets of CKAN RDF graphs transformed and written in LDP.

## Install

    mvn clean install exec:java

## Usage
A transformation can be started through the REST endpoint of the application using a POST request.

    $ curl -X POST -d "dataSet=http://cot-test.infotn.it/dataset" "http://localhost:7100/async-processor/start"
    /status/39e09eef-8358-48c8-b905-bf82859094ee

This will give back the location where the status of the previously started transformation can be monitored. The status can be checked using a GET request.

    $ curl -X GET "http://localhost:7100/async-processor/status/39e09eef-8358-48c8-b905-bf82859094ee"
    Status: PROCESSING

## Data flow
![Data flow diagram](https://raw.githubusercontent.com/fusepoolP3/p3-ckan-processor/master/process.png)

## Issues
* Adding the transformered results to an LDPC is really slow.

## References
This application implements the requirements in [FP-327](https://fusepool.atlassian.net/browse/FP-327).
