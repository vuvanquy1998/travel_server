var http = require("http");
var url = require("url");
var getMatrix = require("./getMatrix");
const getTours = require("./getTours");
const hostname = "localhost";
const port = 8080;

const server = http.createServer(async function (req, res) {
  res.writeHead(200, { "Content-Type": "text/html" });

  try {
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
    } else if (pathname == "/matrix") {
      var data = "";
      req.on("data", async (chunk) => {
        console.log(`Data chunk available: ${chunk}`);
        data = await getMatrix(chunk);
        console.log(data);
        data = JSON.stringify(data);
        res.end(data);
      });
    } else {
      res.end();
    }
  } catch (error) {
    res.end();
  }
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});
