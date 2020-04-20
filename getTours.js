"use strict";
const request = require("request");

const wemap_key = "vpstPRxkBBTLaZkOaCfAHlqXtCR";
const graphhopper_key = "0d88a10e-a95c-45c7-a1ab-942c807b577e";
/**
 * Get the distance available
 * @param {int} time
 * @param {int} speed
 */
function calculateMaxDistance(time, speed) {
  return (time * speed) / 2;
}
/**
 * draw routing line travel
 * @param {array} coordinates
 */
function drawRouting(coordinates) {
  map.addSource("route_travel", {
    type: "geojson",
    data: {
      type: "Feature",
      properties: {},
      geometry: {
        type: "LineString",
        coordinates: coordinates,
      },
    },
  });
  map.addLayer({
    id: "routingOptimized",
    type: "line",
    source: "route_travel",
    layout: {
      "line-join": "round",
      "line-cap": "round",
    },
    paint: {
      "line-color": "#888",
      "line-width": 8,
    },
  });
}
/**
 * remove routing line travel
 */
function removeRoutingLine() {
  map.removeLayer("routingOptimized");
}
/**
 * get speed of vehicle
 * @param {string} vehicle
 */
function getSpeed(vehicle) {
  var speed;
  switch (vehicle) {
    case "car":
      speed = 100;
      break;
    case "motorbike":
      speed = 80;
      break;
    case "walk":
      speed = 30;
      break;
    case "bus":
      speed = 60;
      break;
    default:
      speed = 100;
      break;
  }
  return speed;
}
/**
 * get all non-duplicate elements of two array
 * @param {array} array1
 * @param {array} array2
 */
function mergeArray(array1, array2) {
  array2.forEach((element) => {
    if (!array1.includes(element)) {
      array1.push(element);
    }
  });
  return array1;
}
/**
 * get places in circle with center = location, radius  = distance, type =v (is limited)
 * @param {array} center
 * @param {int} radius
 * @param {string} k
 * @param {string} v
 */
async function getPlaces(center, radius, k = "shop", v = "fashion") {
  return new Promise(async (resolve, reject) => {
    request(
      {
        url: "https://apis.wemap.asia/we-tools/explore",
        qs: {
          lat: center[0],
          lon: center[1],
          d: radius,
          k: k,
          v: v,
          key: wemap_key,
          limit: 30,
        },
      },
      (error, response) => {
        if (error) {
          console.log("error: ", error);
        } else {
          let places_result = [];
          let places = JSON.parse(response.body);
          console.log("places length: ", places.length);
          resolve(places);
        }
      }
    );
  });
}

/**
 *  get all places can be travelled
 * @param {array} location
 * @param {int} time
 * @param {string} vehicle
 */
async function getAllPlaces(location, time, vehicle) {
  var speed = getSpeed(vehicle);
  var radius = calculateMaxDistance(time, speed);
  console.log("calcute radius: ", radius);
  var places_result = [];
  let places = await getPlaces(location, radius);
  return places;
}

/**
 * get id of Routing Optimized
 * @param {object}} data
 */
async function getJobId(data) {
  return new Promise((resolve, reject) => {
    let dataString = JSON.stringify(data);
    request(
      {
        url: "https://graphhopper.com/api/1/vrp/optimize",
        // url: "https://apis.wemap.asia/route-api/route-api/vrp/optimize",
        qs: {
          key: graphhopper_key,
        },
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: dataString,
      },
      (error, response) => {
        if (error) {
          console.log("get Routing Optimized Error: ", error);
        } else {
          console.log(response.body);
          response = JSON.parse(response.body);
          let job_id = response.job_id;
          resolve(job_id);
        }
      }
    );
  });
}

/**
 * get solution routin optimized
 * @param {object}} data
 */
async function getRoutingOptimized(data) {
  let job_id = await getJobId(data);
  return new Promise((resolve, reject) => {
    request(
      {
        url: "https://graphhopper.com/api/1/vrp/solution/" + job_id,
        qs: { key: graphhopper_key },
      },
      (error, response) => {
        response = JSON.parse(response.body);
        let solution = response.solution;
        // console.log(response)
        resolve(solution);
      }
    );
  });
}

/**
 * convert data wemap to data graphHopper
 * @param {object}} data
 */
function convertDataWeMapToGraphHopper(data) {
  data = [data[4], data[29], data[3], data[25], data[5], data[1]];
  // console.log("places: ", data.length, data);
  var graphHopperData = [];
  data.forEach((element) => {
    let graphHopperElement = {
      id: element.osm_id.toString(),
      name: element.address.address29,
      address: {
        location_id: element.address.address29,
        lat: parseFloat(element.lat),
        lon: parseFloat(element.lon),
      },
    };
    graphHopperData.push(graphHopperElement);
  });
  let start = [
    {
      vehicle_id: "my_vehicle",
      start_address: graphHopperData[0].address,
    },
  ];
  return {
    vehicles: start,
    services: graphHopperData,
  };
}
/**
 * Get directions between the two locations
 * @param {string} url
 */
async function getRoutingTwoPlaces(url) {
  return new Promise((resolve, reject) => {
    request(url, (error, response) => {
      let body = JSON.parse(response.body);
      let instructions = body.paths[0].instructions;
      resolve(instructions);
    });
  });
}
/**
 * get directions through multiple locations
 * @param {array} activities
 * @param {string} vehicle
 */
async function getRouting(activities, vehicle = "car") {
  let url = "https://apis.wemap.asia/route-api/route?";
  let option =
    "type=json&locale=en-US&vehicle=" +
    vehicle +
    "&weighting=fastest&elevation=false&key=" +
    wemap_key;
  let points = [];
  var instructions = [];
  activities.forEach((element) => {
    let lat = element.address.lat.toString();
    let lon = element.address.lon.toString();
    let point = "point=" + lat + "," + lon + "&";
    points.push(point);
  });
  for (let i = 0; i < points.length - 1; i++) {
    let local_url = url + points[i] + points[i + 1] + option;
    let instructionTwoPlaces = await getRoutingTwoPlaces(local_url);
    let last_instruction =
      instructionTwoPlaces[instructionTwoPlaces.length - 1];
    let index = i + 1;
    last_instruction.text =
      last_instruction.text + ". Đã đi đến địa điểm thứ " + index.toString();
    instructionTwoPlaces[instructionTwoPlaces.length - 1] = last_instruction;

    instructions = instructions.concat(instructionTwoPlaces);
  }
  return instructions;
}
function getRoutingOSRM(activities, vehicle) {
  var instructions = [];
  let url = "https://apis.wemap.asia/direction-api/route/v1/";
  // let url = "https://apis.wemap.asia/direction-api/route/v1/";

  let type;
  switch (vehicle) {
    case "car":
      type = "driving";
      break;
    case "motorbike":
      type = "cycling";
      break;
    case "walke":
      type = "walking";
      break;
    case "bus":
      type = "driving";
      break;
    default:
      type = "cycling";
      break;
  }
  url += type;
  let points = [];

  activities.forEach((element) => {
    let lat = element.address.lat.toString();
    let lon = element.address.lon.toString();
    let point = lat + "," + lon;
    points.push(point);
  });
  points.pop();
  let poitsString = points.join(";");
  url = url + "/" + poitsString;
  request(
    {
      url: url,
      qs: {
        key: wemap_key,
        overview: "full",
        steps: true,
        geometries: polyline,
      },
    },
    (error, response) => {
      let data = JSON.parse(response.body);
      let routes = data.routes[0];
      let legs = routes.legs;
    }
  );
}
/**
 * get all possible tours
 * @param {array} location
 * @param {string} time
 */
const getTours = async function (location, time) {
  location = location.split(",");
  time = parseFloat(time);
  var tours = [];
  var places = await getAllPlaces(location, time);
  var graphhopperData = convertDataWeMapToGraphHopper(places);
  // console.log("places: ", places.length, places);
  console.log("graphHoperData: ", graphhopperData.length, graphhopperData);
  let solution = await getRoutingOptimized(graphhopperData);
  let distance = solution.distance;
  let time_travel = solution.time;
  let activities = solution.routes[0].activities;
  console.log("activities: ", activities);
  // let instructions = await getRouting(activities);
  // console.log("instructions: ", instructions);

  return solution;
};
(async () => {
  let result = await getTours("21.0386920, 105.8223580", "10");
  console.log(result);
})();
module.exports = getTours;
