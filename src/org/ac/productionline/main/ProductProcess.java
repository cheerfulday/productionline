package org.ac.productionline.main;  

import java.util.*;
import java.util.concurrent.BrokenBarrierException;

import org.ac.productionline.util.*;

public class ProductProcess extends Thread{  
	//the product line this process belongs
	private ProductionLine productionLine;
	//the type mapped with quantity of material this process needed to process a product unit
	private Map<Material, Integer> needMaterials;
	//the type mapped with quantity of material this process has in the current time
	private Map<Material, Integer> existMaterials;
	//according to scale,the max quantity of every material this process can store
	private Map<Material, Integer> maxStoreMaterials;
	//the sum time this process need to process a product unit
	private int processtime; 
	//variable for the class
	private Material material;	
	
	public ProductProcess(String name, ProductionLine productionLine, Map<Material, Integer> needMaterials,
			Map<Material, Integer> existMaterials, Integer sumMaxStoreMaterial) {
		super.setName("produce "+name);
		this.productionLine = productionLine;
		this.needMaterials = needMaterials;
		this.existMaterials = existMaterials;
		
		String showString = ProductionLine.getShowstr()+"Product Process config ";
		maxStoreMaterials = new HashMap<Material, Integer>();
		//used for storing remainder mapped with material when do calculate afterwards  
		Map<String, Material> remainderOfMaterials = new IdentityHashMap<String, Material>();
		//the max quantity of all the need materials this process need to process a product unit
		int sumMaxNeedMaterial = 0;
		//the max quantity of all the need materials this process need to process a product unit
		int sumMaxExistMaterial = 0;
		//used for storing the result after dividing
		int divideTemp;
		//used for storing the remainder after dividing
		int remainderTemp;
		//'sumMaxStoreMaterial':the max quantity of all the materials this process can store
		int remainder = sumMaxStoreMaterial;
		
		Iterator<Material> itForSum = needMaterials.keySet().iterator();	
		while(itForSum.hasNext()){
			material = itForSum.next();
			if (needMaterials.get(material) <= 0 || existMaterials.get(material) <= 0) {
				try {				
					throw new Exception(showString+" Error! quantity of material must be a positive number");				
				} catch (Exception e) {
					System.err.println(e.getMessage());
					System.exit(1);
				}
			}
			
			//calculate the sum time
			processtime+=needMaterials.get(material)*material.getTime();
			//calculate the sum count of needed materials
			sumMaxNeedMaterial+=needMaterials.get(material);
			//calculate the sum count of the first time add materials/product process exist materials
			sumMaxExistMaterial+=existMaterials.get(material);
		}	
		if (sumMaxNeedMaterial > sumMaxStoreMaterial) {
			try {				
				throw new Exception(showString+"Error! the product process can't store so many amount of materials that need to produce a product unit");				
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}else if (sumMaxExistMaterial > sumMaxStoreMaterial) {
			try {				
				throw new Exception(showString+"Error! the sum count of exist materials is more than the product process's storage space");				
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}else if (sumMaxNeedMaterial*2 > sumMaxStoreMaterial) {
			try {				
				throw new Exception(showString+"Warning! the product process's storage space is too small,maybe will affect the produce efficiency");				
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		
		Iterator<Material> itForDiRe = needMaterials.keySet().iterator();
		while(itForDiRe.hasNext()){
			material = itForDiRe.next();
			divideTemp = needMaterials.get(material)*sumMaxStoreMaterial/sumMaxNeedMaterial;
			//to get remainder from 'sumMaxStoreMaterial'
			remainder-=divideTemp;
			maxStoreMaterials.put(material, divideTemp);
			remainderTemp = needMaterials.get(material)*sumMaxStoreMaterial%sumMaxNeedMaterial;
			remainderOfMaterials.put(String.valueOf(remainderTemp), material);
		}	
 
		String[] sortedKeyOfRemainder = new String[remainderOfMaterials.size()];	
		//to get keys of IdentityHashMap 'remainderOfMaterials' 
		remainderOfMaterials.keySet().toArray(sortedKeyOfRemainder);	
		//to call the method sort() to sort 'sortedKeyOfRemainder' in a descending order
		sortedKeyOfRemainder = sort(sortedKeyOfRemainder);
		
		//to assign the remainder by scale 
		for(int i = 0;i<remainder;i++) {
			material = remainderOfMaterials.get(sortedKeyOfRemainder[i]);
			maxStoreMaterials.put(material, maxStoreMaterials.get(material)+1);			
		}		
	}
	
    @Override  
    public void run() { 
		while (productionLine.getProducts().size() < Integer.parseInt(productionLine.getProductAmount())) {
			ProductUnit productUnit;
				try {   					
					Thread.sleep(processtime);
				} catch (InterruptedException e1) {
					System.err.println(ProductionLine.getShowstr()+this.getName()+" Error! "+e1.getMessage());
					System.exit(1);
				}
				productUnit = new ProductUnit();
				productUnit.setName(this.getName().substring(8));
				Iterator<Material> it = needMaterials.keySet().iterator();
				while(it.hasNext()){
					material = it.next();
					//to add material randomly when not enough
					while (needMaterials.get(material) > existMaterials.get(material)) {					
						addMaterial(material);
					}
					//to store the count of the material when process the current product unit
					productUnit.getUnitMaterialCount().put(material, needMaterials.get(material));
					//to store the current time when process the current product unit
					productUnit.getUnitMaterialTime().put(material, System.currentTimeMillis());				
					//process the material
					existMaterials.put(material,existMaterials.get(material) - needMaterials.get(material));
				}	
				productionLine.getProductUnits().add(productUnit);
            try {
            	productionLine.getBarrier().await();
			} catch (InterruptedException | BrokenBarrierException e) {
				System.err.println(ProductionLine.getShowstr()+this.getName()+" Error! "+e.getMessage());
				System.exit(1);
			}
		}
		productionLine.getTaskExecutor().shutdown();
    }  
    
    public void addMaterial(Material material){	   
    	Integer maxStoreMaterial = maxStoreMaterials.get(material);
		//to get the max count that the process can contain:maxAddMaterialCount
		int maxAddMaterialCount = maxStoreMaterial - existMaterials.get(material);
		//to get the max count that can be add:givenMaterialCount
		int givenMaterialCount = new Random().nextInt(maxAddMaterialCount);		
		if (givenMaterialCount < maxAddMaterialCount) {	
			existMaterials.put(material, givenMaterialCount + existMaterials.get(material));	
		}else {				
			existMaterials.put(material, maxStoreMaterial);
		}
	}

	public static String[] sort(String...count){
		String temp;		
        for (int i = count.length - 1; i > 0; --i) {
            for (int j = 0; j < i; ++j) {
                if (Integer.parseInt(count[j]) < Integer.parseInt(count[j + 1])) {
                    temp = count[j];
                    count[j] = count[j + 1];
                    count[j + 1] = temp;
                }
            }
        }
        return count;
	}
    
	public Map<Material, Integer> getNeedMaterials() {
		return needMaterials;
	}

	public Map<Material, Integer> getExistMaterials() {
		return existMaterials;
	}
          
}  