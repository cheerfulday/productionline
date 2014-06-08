package org.ac.productionline.util;

import java.util.*;

public class Product {
	//product name
	private String name;
	//the type mapped with quantity of material in the product
	private Map<Material, Integer> productMaterialCount;
	//the type mapped with process time which mapped with quantity of material in the product
	private Map<Material, Map<Long, Integer>> productMaterialTime;
	
	public Product() {
		productMaterialCount = new HashMap<Material, Integer>();
		productMaterialTime = new HashMap<Material, Map<Long, Integer>>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Material, Integer> getProductMaterialCount() {
		return productMaterialCount;
	}

	public void setProductMaterialCount(Map<Material, Integer> productMaterialCount) {
		this.productMaterialCount = productMaterialCount;
	}

	public Map<Material, Map<Long, Integer>> getProductMaterialTime() {
		return productMaterialTime;
	}

	public void setProductMaterialTime(
			Map<Material, Map<Long, Integer>> productMaterialTime) {
		this.productMaterialTime = productMaterialTime;
	}
	
}
