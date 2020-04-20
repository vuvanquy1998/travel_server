var http = require("http");
var url = require("url");
const getTours = require("./getTours");
const hostname = "localhost";
const port = 8080;

const server = http.createServer(async function (req, res) {
  res.writeHead(200, { "Content-Type": "text/html" });
  var q = url.parse(req.url, true);
  var params = q.query;
  var pathname = q.pathname;
  if (pathname == "/travel") {
    var result = "url format error";
    var location = params.location;
    var time_travel = params.time;
    if (location && time_travel) {
      result = await getTours(location, time_travel);
      result = JSON.stringify(result);
    }

    res.end(result);
  } else {
    res.end();
  }
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});
