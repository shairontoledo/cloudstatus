CloudStatus is a simple RESTful API project to track cloud service incidents, events and status. I could be consider as a generic way to represent incidents like:

- [Slack status](https://status.slack.com/)
- [AWS Health Dashboard](http://status.aws.amazon.com/)
- [Twitter status](http://status.twitter.com/)
- and similarities

It's built on top of [Sprint Boot](http://projects.spring.io/spring-boot/) to apply the philosophy

> "Make JAR, not WAR"

The initial release uses [Redis](http://redis.io/) as datastore and can easily be replaced in the future by any preferred technology.

### Install

Get the latest version from [cloud-status-0.5.0.jar](http://d.pr/f/18U6k)

	curl -o cloud-status-0.5.0.jar -L http://d.pr/f/18U6k+

Or clone the project and build it by yourself using [Gradle](http://gradle.org/)

	git clone https://github.com/shairontoledo/cloudstatus.git
	cd cloudstatus
	gradle bootRepackage
	cd build/libs

### Run it
Before run it you need to install and run [Redis Server](http://redis.io/topics/quickstart) then just start CloudStatus like a regular jar project:

	java -jar cloud-status-0.5.0.jar

You might see from logs that the default port is `8081`, you can change it in the properties file.

### Get started faster
For those that want see things right away without read the API use the `--seed` command tool with [cloud-status-seed.json]() file that comes with the project.

	java -jar cloud-status-0.5.0.jar --seed cloud-status-seed.json

It will populate some date to be used

### Demo UI
I've written a basic UI that uses [Backbone.js](http://backbonejs.org/) to list services, events and their statuses in a shot. After you get the server running at port `8081`, you can just open [demo-index.html]() in your browser to see the demo.

![ui-demo](http://d.pr/i/1cL6u+)
Where
![ui-demo-detaild](http://d.pr/i/3kL9+)

### Protected Resources

By default all API `GET` resources are public, `POST`, `PUT` and `DELETE` are protected by token. To generate a token use token manager tool from command line.

	java -jar cloud-status-0.5.0.jar --create-token identifier-1

That will print on stdout the following:

	*** New token '11111530265e45b49a182e33853dbc7009a8c33c91c14b6382a8093eff81b81f' has been created for 'identifier-1'

The argument `identifier-1` is just an identifier for future reference.
The token should be used protected resources either via `Authorization` header

	Authorization: Token <token here>

Or URI parameter `?token=<token here>`. You can have many tokens you want. They can be listed by

	java -jar cloud-status-0.5.0.jar --list-tokens

And revoked either by identifier or by token itself:

	java -jar cloud-status-0.5.0.jar --revoke-token <identifier or token here>


### Application properties

The default properties can be overwritten if a file `application.properties` is either in the in the start up directory or under `config/` directory from the start up directory.

Most common properties:

Redis configuration

    redis.hostname=localhost
    redis.port=6379

Server configuration

    server.port=8081
    spring.main.banner_mode=off

Bind `/health` endpoint

    endpoints.health.id=health
    endpoints.health.sensitive=true
    endpoints.health.enabled=true

Bind `/info` endpoint

    endpoints.info.id=info
    endpoints.info.sensitive=false
    endpoints.info.enabled=true

Bind `/metrics` endpoint

    endpoints.metrics.id=metrics
    endpoints.metrics.sensitive=true
    endpoints.metrics.enabled=false


## API Reference

### List all endpoints

```
curl http://localhost:8081/api_endpoints | jq .
```

Response

```
[
  "GET /api_endpoints",
  "GET /events/{id}",
  "PUT /events/{id}",
  "DELETE /events/{id}",
  "POST /events",
  "GET /events",
  "GET /services/{id}",
  "POST /services/{id}/events",
  "POST /services",
  "GET /services/{serviceId}/events/{id}",
  "PUT /services/{id}",
  "DELETE /services/{id}",
  "GET /services/{id}/events",
  "PUT /services/{serviceId}/events/{id}",
  "DELETE /services/{serviceId}/events/{id}",
  "GET /services",
  "GET /statuses/{id}",
  "GET /services/{serviceId}/events/{eventId}/statuses/{id}",
  "GET /services/{serviceId}/events/{eventId}/statuses",
  "POST /services/{serviceId}/events/{eventId}/statuses",
  "DELETE /services/{serviceId}/events/{eventId}/statuses/{id}",
  "GET /statuses"
]
```

### POST /services

Create a service. Require `name` field.

```
curl -XPOST -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services"  -d '
{
  "name": "Nice and Cool Service"
}'
```

*Response*

Code `200` - OK

```
{
  "id": "service:af00d3ac-5821-4121-a305-e54356620c1b",
  "name": "Nice and Cool Service"
}
```

Code `401` - Unauthorized

```
{
  "message": "Access to the resource is not authorized"
}
```

Code `400` - Bad Request
```
{
  "message": "Field 'name' may not be null"
}
```

> Note: `401` and `400` status codes can happen in most of all endpoints, they will be omitted in the documentation for brevity.


### GET /services/{id}

Get a service by id.

```
curl -XGET -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b"
```

*Response*

Code `200` - OK

```
{
  "id": "service:af00d3ac-5821-4121-a305-e54356620c1b",
  "name": "Nice and Cool Service"
}
```

### PUT /services/{id}

Update a service by id.

```
curl -XPUT -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b" -d '
{
  "name": "Updated name"
}
'
```

*Response*

Code `200` - OK

```
{
  "id": "service:af00d3ac-5821-4121-a305-e54356620c1b",
  "name": "Updated name"
}
```


### GET /services

Get all services.

```
curl -XGET -H "Content-type: application/json" "http://localhost:8081/services"
```

*Response*

Code `200` - OK

```
[
  {
    "id": "service:41d3299a-6bcc-4325-a558-d78534b319a8",
    "name": "Authentication and Authorization"
  },
  {
    "id": "service:86e1d7a8-69bc-43f5-ba0d-dd19cd0a5e7f",
    "name": "Facebook Integration"
  }
]
```

### DELETE /services/{id}

Delete a service.

```
curl -XDELETE -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b"
```

*Response*

Code `200` - OK

```
{
  "id": "service:af00d3ac-5821-4121-a305-e54356620c1b",
  "name": "Nice and Cool Service"
}
```


### POST /services/{id}/events

Create a service event. Require fields:

- `name` - Event name.
- `when` - When the event happened.
- `severity` - The severity of the event.


```
curl -XPOST -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events"  -d '
{
  "name": "Some users are experiencing issues to access our service",
  "when": "2016-01-10T15:06:07Z",
  "severity": "Partial Service Disruption"
}'
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Partial Service Disruption",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}
```
### PUT /services/{serviceId}/events/{id}

Update a service event


```
curl -XPOST -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819"  -d '
{
  "name": "Correct name here",
}'
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:30:07Z",
  "name": "Correct name here",
  "severity": "Partial Service Disruption",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}
```

### GET /services/{id}/events

Get all service events

```
curl -XGET -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events"
```

*Response*

Code `200` - OK

```
[
  {
    "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
    "when": "2016-01-10T15:06:07Z",
    "name": "Some users are experiencing issues to access our service",
    "severity": "Partial Service Disruption",
    "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
  }
]
```

### GET /services/{serviceId}/events/{id}

Get a service event by id.

```
curl -XGET -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819"
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Partial Service Disruption",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}

```

### DELETE /services/{serviceId}/events/{id}

Delete a service event by id.

```
curl -XDELETE -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819"
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Partial Service Disruption",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}

```

### POST /services/{serviceId}/events/{id}/statuses

Create a status for an event. Require fields:

- `message` - It's an update status message
- `when` - The status date/time.
- `type` - The status type, can be `green`, `yellow` or `red`, that will help know the latest status of the event.


```
curl -XPOST -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819/statuses"  -d '
{
  "when": "2016-01-10T15:09:07Z",
  "message": "This issue has now been resolved. We are sorry for any inconvenience.",
  "type": "green"
}'
```

*Response*

Code `200` - OK

```
{
  "id": "status:d0d6ec93-9e77-43c2-b8e3-cfdabac8f2cc",
  "type": "green",
  "message": "This issue has now been resolved. We are sorry for any inconvenience.",
  "when": "2016-01-10T15:09:07Z",
  "event_id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}
```

### GET /services/{serviceId}/events/{id}/statuses

List all event statuses.

```
curl -XGET -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819/statuses"
```

*Response*

Code `200` - OK

```
[
  {
    "id": "status:d0d6ec93-9e77-43c2-b8e3-cfdabac8f2cc",
    "type": "green",
    "message": "This issue has now been resolved. We are sorry for any inconvenience.",
    "when": "2016-01-10T15:09:07Z",
    "event_id": "event:8f21b895-6405-4aca-ade5-79499b591819",
    "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
  }
]
```

### GET /services/{serviceId}/events/{eventId}/statuses/{id}

Get an event status.

```
curl -XGET -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819/statuses/status:d0d6ec93-9e77-43c2-b8e3-cfdabac8f2cc"
```

*Response*

Code `200` - OK

```
{
  "id": "status:d0d6ec93-9e77-43c2-b8e3-cfdabac8f2cc",
  "type": "green",
  "message": "This issue has now been resolved. We are sorry for any inconvenience.",
  "when": "2016-01-10T15:09:07Z",
  "event_id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}
```

### DELETE /services/{serviceId}/events/{eventId}/statuses/{id}

Delete an event status.

```
curl -XDELETE -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/services/service:af00d3ac-5821-4121-a305-e54356620c1b/events/event:8f21b895-6405-4aca-ade5-79499b591819/statuses/status:d0d6ec93-9e77-43c2-b8e3-cfdabac8f2cc"
```

*Response*

Code `200` - OK

```
{
  "id": "status:d0d6ec93-9e77-43c2-b8e3-cfdabac8f2cc",
  "type": "green",
  "message": "This issue has now been resolved. We are sorry for any inconvenience.",
  "when": "2016-01-10T15:09:07Z",
  "event_id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}
```


### GET /events

Get all events regardless their services.

```
curl -XGET -H "Content-type: application/json" "http://localhost:8081/events"
```

*Response*

Code `200` - OK

```
[
  {
    "id": "event:2e4c8a8b-eeae-47c5-8b76-1022bcae1525",
    "when": "2016-01-20T15:06:07Z",
    "name": "Messages cannot be dispatched among the internal services",
    "severity": "Service Outage",
    "service_id": "service:3bee1635-aa58-4432-9e44-d716766e0027"
  },
  {
    "id": "event:7954085c-9253-42ad-944f-e57fd1a518d3",
    "when": "2016-01-18T17:06:07Z",
    "name": "Unusual API traffic",
    "severity": "Critical",
    "service_id": "service:c68a8e26-4203-4a11-800a-d11031c8f16b"
  }
]
```

### GET /events/{id}

Get an event by id.

```
curl -XGET -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/events/event:8f21b895-6405-4aca-ade5-79499b591819"
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Partial Service Disruption",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}

```

### DELETE /events/{id}

Delete an event by id.

```
curl -XDELETE -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/events/event:8f21b895-6405-4aca-ade5-79499b591819"
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Partial Service Disruption",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}

```

### PUT /events/{id}

Update an event by id.

```
curl -XPUT -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/events/event:8f21b895-6405-4aca-ade5-79499b591819" -d '
{
  "severity": "Normal",
  "when": "2016-01-10T15:06:07Z",
}
'
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Normal",
  "service_id": "service:af00d3ac-5821-4121-a305-e54356620c1b"
}

```

### POST /services/{id}/events

Create a service event. Require fields:

- `name` - Event name.
- `when` - When the event happened.
- `severity` - The severity of the event.
- `service_id` - Service id.


```
curl -XPOST -H "Authorization: Token 265e45b49a18" -H "Content-type: application/json" "http://localhost:8081/events"  -d '
{
  "name": "Some users are experiencing issues to access our service",
  "when": "2016-01-10T15:06:07Z",
  "severity": "Partial Service Disruption",
  "service_id": "service:41d3299a-6bcc-4325-a558-d78534b319a8"
}'
```

*Response*

Code `200` - OK

```
{
  "id": "event:8f21b895-6405-4aca-ade5-79499b591819",
  "when": "2016-01-10T15:06:07Z",
  "name": "Some users are experiencing issues to access our service",
  "severity": "Partial Service Disruption",
  "service_id": "service:41d3299a-6bcc-4325-a558-d78534b319a8"
}
```

### GET /info
Get application info.

```
curl http://localhost:8081/info
```

*Response*

Code `200` - OK

```
{
  "app": {
    "description": "API to store Cloud service status",
    "version": "0.5",
    "name": "Cloud Status API"
  }
}
```

### GET /health

Health checker endpoint.

```
curl http://localhost:8081/health
```

*Response*

Code `200` - OK

```
{
  "app": {
    "description": "API to store Cloud service status",
    "version": "0.5",
    "name": "Cloud Status API"
  }
}
```
### GET /metrics

Get metrics.

```
curl http://localhost:8081/metrics
```

*Response*

Code `200` - OK

```
{
  "mem": 205824,
  "mem.free": 99424,
  "processors": 8,
  "instance.uptime": 31005,
  "uptime": 34211,
  "systemload.average": 2.92822265625,
  "heap.committed": 205824,
  "heap.init": 131072,
  "heap.used": 106399,
  "heap": 1864192,
  "threads.peak": 14,
  "threads.daemon": 12,
  "threads.totalStarted": 19,
  "threads": 14,
  "classes": 6081,
  "classes.loaded": 6081,
  "classes.unloaded": 0,
  "gc.ps_scavenge.count": 5,
  "gc.ps_scavenge.time": 48,
  "gc.ps_marksweep.count": 1,
  "gc.ps_marksweep.time": 32,
  "httpsessions.max": -1,
  "httpsessions.active": 0,
  "gauge.response.metrics": 92,
  "counter.status.200.metrics": 1
}
```

## TODO

- Implement Redis namespace
- Publish–subscribe for new incidents
- API to list items using timeline
- API to build the calendar
- Better Demo UI
