# trakker

A web based time tracker.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

There are no other external dependencies, so you can use it as a local application.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

Note that it uses a file-based H2 database for storing timelogs, so pay attention to this if you think to deploy this app on Heroku.

## License

Copyright © 2013 Manuel Paccagnella, distributed under the [MPL 2.0](http://www.mozilla.org/MPL/2.0/) license
