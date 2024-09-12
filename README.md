# DAS Salesforce

## How to use

First you need to build the project:
```bash
$ sbt "project docker" "docker:publishLocal"
```

This will create a docker image with the name `das-salesforce`.

Then you can run the image with the following command:
```bash
$ docker run -p 50051:50051 <image_id>
```
... where `<image_id>` is the id of the image created in the previous step.
This will start the server on port 50051.

You can find the image id by looking at the sbt output or by running:
```bash
$ docker images
```

## Options

| Name                  | Description                       | Default | Required |
|-----------------------|-----------------------------------|---------|----------|
| `api_version`         | Salesforce API version            |         | Yes      |
| `username`            | Salesforce username               |         | Yes      |
| `password`            | Salesforce password               |         | Yes      |
| `security_token`      | Salesforce security token         |         | Yes      |
| `client_id`           | Salesforce client id              |         | Yes      |
| `url`                 | Salesforce URL                    |         | Yes      |
| `add_dynamic_columns` | Add dynamic columns to the schema | `true`  | No       |
