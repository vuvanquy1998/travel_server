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

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer.Label;
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
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;

import com.graphhopper.jsprit.core.problem.job.Shipment;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import java.io.PrintWriter;

import java.util.Collections;
import java.util.List;

public class SimpleExample {

	public static void main(String[] args) {

		/*
		 * some preparation - create output folder
		 */
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()) {
			System.out.println("creating directory ./output");
			boolean result = dir.mkdir();
			if (result)
				System.out.println("./output created");
		}

		/*
		 * get a vehicle type-builder and build a type with the typeId "vehicleType" and
		 * one capacity dimension, i.e. weight, and capacity dimension value of 2
		 */
		final int WEIGHT_INDEX = 0;
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType")
				.addCapacityDimension(WEIGHT_INDEX, Integer.MAX_VALUE);
		VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
		 * get a vehicle-builder and build a vehicle located at (10,10) with type
		 * "vehicleType"
		 */
		Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
		vehicleBuilder.setType(vehicleType);
		VehicleImpl vehicle = vehicleBuilder.build();

		/*
		 * build services at the required locations, each with a capacity-demand of 1.
		 */
		Service service1 = Service.Builder.newInstance("1").addSizeDimension(WEIGHT_INDEX, 0)
				.setLocation(Location.newInstance(5, 7)).build();
		Service service2 = Service.Builder.newInstance("2").addSizeDimension(WEIGHT_INDEX, 0)
				.setLocation(Location.newInstance(5, 13)).build();

		Service service3 = Service.Builder.newInstance("3").addSizeDimension(WEIGHT_INDEX, 0)
				.setLocation(Location.newInstance(15, 7)).build();
		Service service4 = Service.Builder.newInstance("4").addSizeDimension(WEIGHT_INDEX, 0)
				.setLocation(Location.newInstance(15, 13)).build();

		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(vehicle);
		vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);

		VehicleRoutingProblem problem = vrpBuilder.build();

		/*
		 * get the algorithm out-of-the-box.
		 */
		VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

		/*
		 * and search a solution
		 */
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

		/*
		 * get the best
		 */
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

		new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");

		SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

		/*
		 * plot
		 */
		new Plotter(problem, bestSolution).plot("output/plot.png", "simple example");

		/*
		 * render problem and solution with GraphStream
		 */
		new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();

		List<VehicleRoute> list = new ArrayList<VehicleRoute>(bestSolution.getRoutes());
		Collections.sort(list, new com.graphhopper.jsprit.core.util.VehicleIndexComparator());
		for (VehicleRoute route : list) {
			for (TourActivity act : route.getActivities()) {
				String jobId;
				if (act instanceof TourActivity.JobActivity) {
					jobId = ((TourActivity.JobActivity) act).getJob().getId();
				} else {
					jobId = "-";
				}
				System.out.println(act);
				System.out.println("jobId: " + jobId);
			}
		}

	}

}
