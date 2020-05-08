const request = require("request");
async function getDistance(origin, destination) {
  let url =
    "http://localhost:8989/route?" +
    "type=json&locale=en-US&vehicle=car&weighting=fastest&elevation=false&key=vpstPRxkBBTLaZkOaCfAHlqXtCR&" +
    "point=" +
    origin +
    "&point=" +
    destination;
  return new Promise((resolve, reject) => {
    if (origin == destination) {
      resolve(0);
    }
    request(url, (error, response) => {
      if (error) {
        console.log("error getDistance: ", error);
        resolve(0);
      } else {
        let data = JSON.parse(response.body);
        let distance = data.paths[0].distance;
        let time = data.paths[0].time;
        resolve(distance);
      }
    });
  });
}
async function getRow(index, places) {
  var row = [];
  var promises = [];
  for (let i = 0; i < places.length; i++) {
    promises.push(getDistance(places[index], places[i]));
  }

  await Promise.all(promises)
    .then(async () => {
      for (let i = 0; i < promises.length; i++) {
        row.push(await promises[i]);
      }
    })
    .catch((err) => {
      console.log(
        "error: ",
        err,
        "Promise All index: ",
        index,
        ":  ",
        places[index]
      );
    });
  return row;
}
const getMatrix = async function (places) {
  var matrix = [];
  for (let i = 0; i < places.length; i++) {
    let row = await getRow(i, places);
    matrix.push(row);
  }
  return matrix;
};

module.exports = getMatrix;

// (async () => {
//   let data = [
//     [21.032356, 105.845461],
//     [21.024985, 105.844774],
//     [21.028911, 105.856318],
//     [21.019297, 105.856705],
//   ];
//   let result = await getMatrix(data);
//   console.log(result);
// })();

// getDistance("21.052403,105.78362", "20.982317,105.863335");
