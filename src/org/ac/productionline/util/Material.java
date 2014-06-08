package org.ac.productionline.util;

import org.ac.productionline.main.ProductionLine;

public class Material {	
	//material type
	private String name;
	//time needed to be used when processing the material
	private int time;
	
	public Material() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		if (time <= 0) {
			try {				
				throw new Exception(ProductionLine.getShowstr()+"Material config Error! process time must be a positive number");				
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
		this.time = time;
	}
	
}
