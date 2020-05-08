/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.springboot;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Illustrates how you can use jsprit with an already compiled distance and time
 * matrix.
 *
 * @author schroeder
 */
public class CostMatrixExample {

	public static void main(String[] args) {
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()) {
				System.out.println("creating directory ./output");
				boolean result = dir.mkdir();
				if (result)
						System.out.println("./output created");
		}

		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, Integer.MAX_VALUE)
						.setCostPerDistance(1).setCostPerTime(2).build();
		VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance("0"))
						.setType(type).build();

		int sizeMatrixDistance = 5;
		float[][] matrixDistance = new float[][] { { 5, 1, 4, 6, 8 }, { 9, 2, 12, 7, 19 }, { 15, 1, 9, 15, 5 },
						{ 14, 17, 13, 2, 20 }, { 21, 24, 5, 7, 2 } };
		Service[] services = new Service[sizeMatrixDistance];

		for (int i = 0; i < matrixDistance.length; i++) {
				String instance = Integer.toString(i);
				Service s = Service.Builder.newInstance(instance).addSizeDimension(0, 0)
								.setLocation(Location.newInstance(instance)).build();
				services[i] = s;
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
		for (int i = 0; i < services.length; i++) {
				vrpBuilder.addJob(services[i]);
		}
		VehicleRoutingProblem vrp = vrpBuilder.build();
		VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

		/*
		 * get the best
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		SolutionPrinter.print(vrp, bestSolution, SolutionPrinter.Print.VERBOSE);

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
						// System.out.println(act);
						// System.out.println("jobId: " + jobId);
						result = result.concat("," + jobId);
				}
		}
		System.out.println(result);
		// return result;
	}

}
