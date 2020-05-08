var request = require("request");
var matrixDistance =
  "[ [ 0, 2043.043, 2493.887, 505.837 ],  [ 2302.871, 0, 450.817, 1797.033 ],  [ 2753.715, 450.817, 0, 2247.877 ], [ 505.837, 1537.205, 1988.049, 0 ] ]";
request(
  {
    url: "http://localhost:8080/optimizedRouting",
    headers: {
      "Content-Type": "application/json",
    },
    body: matrixDistance,
  },
  (error, response) => {
    if (error) {
      resolve("error: ", error);
    } else {
      let data = response.body;
      console.log("get Routing Optimized:", data);
    }
  }
);
