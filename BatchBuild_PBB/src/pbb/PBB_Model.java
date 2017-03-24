package pbb;

import ilog.concert.*;
import ilog.cplex.*;

public class PBB_Model {
	public static void solveMe() {
		// Input Parameters
		int noPart = 8; // Number of parts	i
		int noConstExpr = 4; // Number of constraint expressions	j
		int noOpDays = 5; // Number of operation days	l
		int noConst = 3;	// Number of constraints	k
		int bigM = 10000; // Big M Number	10000	M
		
		double[][] weight = {{300,200,400,500},{500,400,250,450},{150,200,300,200},{1000,1200,1050,950},{50,70,60,40},{5,10,5,10},{2000,2500,3000,1500},{600,800,550,750}}; // Weekly demand volume of part i with constraint expression j
		double[] totalCapacityL ={4000,4000,4000,4000,4000}; //Total capacity of day l
		double[] dailyCapacity4ConstK ={2500,3500,3000}; // daily capacity for constraint k

		double[][] indexTable ={{0,0,1,1},{0,1,0,1},{0,0,1,0}}; // Indexed table of constraint expressions t
		
		
        try {
        	// define new model
        	IloCplex cplex = new IloCplex();

        	// variables
        	IloNumVar[][][] x = new IloNumVar[i][j][l]; // Volume of part i with constraint expression j on day l 
        	for (int i = 0; i < noPart; i++){
        		for (int j = 0; j < noConstExpr; j++){
        			for (int l = 0; l < noOpDays; l++){
        				
        			}
        		}
        	}
        	IloNumVar[][] y = new IloNumVar[i][l]; // parts and days
        	        	
        	// expressions
        	IloLinearNumExpr objective = cplex.linearNumExpr();
        	objective.addTerms(1, x);
        	objective.addTerms(1, y);
        	

        	// define objective
        	cplex.addMinimize(objective);


        	// constraints
        	

        	
        	// solve model
        	if (cplex.solve()) {
        		System.out.println("obj = "+cplex.getObjValue());
        	}
        	else {
        		System.out.println("problem not solved");
        	}
        	
        	cplex.end();
        }
        catch (IloException exc) {
        	exc.printStackTrace();
        }
	
		
	}

}
