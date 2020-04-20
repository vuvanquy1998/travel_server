const request = require("request");
request(
  {
    url: "http://localhost:8080/travel",
    qs: {
      location: "107.70183,18.07717",
      time: 5,
    },
  },
  (error, response) => {
    if (error) {
      console.log("error:", error);
    } else {
      var data = JSON.parse(response.body)
      console.log(data);
    }
  }
);
