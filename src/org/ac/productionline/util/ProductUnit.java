package org.ac.productionline.util;

import java.util.*;

public class ProductUnit {
	//product unit's name
	private String name;
	//the type mapped with quantity of material in the product unit
	private Map<Material, Integer> unitMaterialCount;
	//the type mapped with process time of material in the product unit
	private Map<Material, Long> unitMaterialTime;
	
	public ProductUnit() {	
		unitMaterialCount = new HashMap<Material, Integer>();
		unitMaterialTime = new HashMap<Material, Long>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Material, Integer> getUnitMaterialCount() {
		return unitMaterialCount;
	}

	public void setUnitMaterialCount(Map<Material, Integer> unitMaterialCount) {
		this.unitMaterialCount = unitMaterialCount;
	}

	public Map<Material, Long> getUnitMaterialTime() {
		return unitMaterialTime;
	}

	public void setUnitMaterialTime(Map<Material, Long> unitMaterialTime) {
		this.unitMaterialTime = unitMaterialTime;
	}
	
}
