# poom-crons
Poor man's crontab

# Task Specs

* at `8:30`
```json
{
  "url": "http://an.url",
  "payload": {
    "a": "property"
  },
  "scheduled": {
    "at": {
      "minutes-of-hours": 30,
      "hour-of-day": 8
    }
  }
}
```

* every `2` minutes
```json
{
  "url": "http://an.url",
  "payload": {
    "a": "property"
  },
  "scheduled": {
    "every": {
      "minutes": 2,
      "starting-at": "2019-02-15T00:00:00.000Z"
    }
  }
}
```

* on `thuesdays` at `14:35`
```json
{
  "url": "http://an.url",
  "payload": {
    "a": "property"
  },
  "scheduled": {
    "at": {
      "minutes-of-hours": 35,
      "hour-of-day": 14,
      "day-of-week": 2
    }
  }
}
```

* every `3` hours
```json
{
  "url": "http://an.url",
  "payload": {
    "a": "property"
  },
  "scheduled": {
    "every": {
      "hours": 3,
      "starting-at": "2019-02-25T00:00:00.000Z"
    }
  }
}
```

* on the `5`th of each month at `07:32`
```json
{
  "url": "http://an.url",
  "payload": {
    "a": "property"
  },
  "scheduled": {
    "at": {
      "minutes-of-hours": 32,
      "hour-of-day": 7,
      "day-of-month": 2
    }
  }
}
```

* on the `3`rd month of each year on day `5` at `6:41`
 ```json
 {
  "url": "http://an.url",
  "payload": {
    "a": "property"
  },
   "scheduled": {
     "at": {
       "minutes-of-hours": 41,
       "hour-of-day": 6,
       "day-of-month": 5,
       "motnth-of-year": 3
     }
   }
 }
 ```