package com.example.springboot;

import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.lang.Math;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class HelloController {
    public float[][] convertData(String stringBody) {
        String dataString = stringBody.replace(" ", "");
        dataString = dataString.replace("{", "");
        dataString = dataString.replace("}", "");
        dataString = dataString.replace("[", "");
        dataString = dataString.replace("]", "");
        String[] resultString = dataString.split(",");
        System.out.println(resultString);
        int size = resultString.length;
        System.out.println(size);
        float[] arr = new float[size];
        for (int i = 0; i < size; i++) {
            arr[i] = Float.parseFloat(resultString[i]);
        }
        // float [] resultFloat =
        int sizeResult = (int) Math.sqrt(size);
        System.out.println(sizeResult);
        float[][] result = new float[sizeResult][sizeResult];
        for (int i = 0; i < sizeResult; i++) {
            for (int j = 0; j < sizeResult; j++) {
                result[i][j] = arr[i * sizeResult + j];
            }
        }
        return result;
    }

    public void createFolderouput() {
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result)
                System.out.println("./output created");
        }
    }

    public VehicleRoutingProblemSolution getBestSolution(float[][] matrixDistance) {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, Integer.MAX_VALUE)
                .setCostPerDistance(1).setCostPerTime(2).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance("0"))
                .setType(type).build();

        int sizeMatrixDistance = 5;
        Service[] services = new Service[sizeMatrixDistance];

        for (int i = 0; i < matrixDistance.length; i++) {
            String instance = Integer.toString(i);
            Service service = Service.Builder.newInstance(instance).addSizeDimension(0, 0)
                    .setLocation(Location.newInstance(instance)).build();
            services[i] = service;
        }
        // define a matrix-builder building a symmetric matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder
                .newInstance(false);
        for (int i = 0; i < matrixDistance.length; i++) {
            for (int j = 0; j < matrixDistance.length; j++) {
                String fromInstance = Integer.toString(i);
                String toInstance = Integer.toString(j);
                if (i == j) {
                    matrixDistance[i][j] = 0;
                }
                costMatrixBuilder.addTransportDistance(fromInstance, toInstance, matrixDistance[i][j]);
            }
        }

        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
                .setFleetSize(FleetSize.INFINITE).setRoutingCost(costMatrix).addVehicle(vehicle);
        for (int i = 1; i < services.length; i++) {
            vrpBuilder.addJob(services[i]);
        }
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        return bestSolution;
    }

    public String getRouting(VehicleRoutingProblemSolution bestSolution) {
        List<VehicleRoute> list = new ArrayList<VehicleRoute>(bestSolution.getRoutes());
        Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
        String result = "0";
        for (VehicleRoute route : list) {
            for (TourActivity act : route.getActivities()) {
                String jobId;
                if (act instanceof TourActivity.JobActivity) {
                    jobId = ((TourActivity.JobActivity) act).getJob().getId();
                } else {
                    jobId = "-";
                }
                result = result.concat("," + jobId);
            }
        }
        return result;
    }

    @RequestMapping("/optimizedRouting")
    // @RequestBody String bodyString
    public String index(@RequestBody String stringBody) {
        float[][] matrixDistance = convertData(stringBody);
        System.out.println(matrixDistance);
        createFolderouput();
        VehicleRoutingProblemSolution bestSolution = getBestSolution(matrixDistance);
        String result = getRouting(bestSolution);
        return result;
    }

}

// public class UserStats{
// private String firstName;
// private String lastName;
// // + getters, setters
// }
