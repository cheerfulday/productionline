package org.ac.productionline.main;  

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

import org.ac.productionline.util.*;
import org.springframework.context.ApplicationContext;  
import org.springframework.context.support.ClassPathXmlApplicationContext;   
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


public class ProductionLine {  
	//production line's name
	private String name;
	//the quantity of products to be produce
	private String productAmount;
	//a synchronization aid that allows a set of threads to all wait for each other to reach a common barrier point
	private CyclicBarrier barrier;	
	private ThreadPoolTaskExecutor taskExecutor;
	//the quantity of the production line's processes
	private int processCount;
	//used for storing product units of the production line
	private List<ProductUnit> productUnits;
	//used for storing the count of products the production line produced
	private List<Product> products;
	//output mark to catch user's eye when an Exception take place
	private static final String showStr = " **!-**-!** ";
	
	public ProductionLine(String name, String productAmount, int processCount, ThreadPoolTaskExecutor taskExecutor) {
		String showString = showStr+"Product Line config ";
		if (Integer.parseInt(productAmount) <= 0) {
			try {
				throw new Exception(showString+"Error! productAmount must be a positive number");
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}else if (processCount <= 0) {
			try {
				throw new Exception(showString+"Error! processCount must be a positive number");
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
		this.name = name;
		this.productAmount = productAmount;
		this.processCount = processCount;
		this.taskExecutor = taskExecutor;
		productUnits = new ArrayList<ProductUnit>();
		products = new ArrayList<Product>();
	}
	
    public static void main(String[] args) {  	    	
    	 @SuppressWarnings("resource")
		ApplicationContext ctx =   
    		        new ClassPathXmlApplicationContext("Spring-Config.xml"); 
        //to get all the Product Processes    
        Collection<ProductProcess> collection = ctx.getBeansOfType(ProductProcess.class).values();
		Iterator<ProductProcess> it = collection.iterator();
		ProductProcess [] processes = new ProductProcess [collection.size()];
		int i = 0;
		while (it.hasNext()) {
			processes[i++] = it.next();
		}
        ProductionLine productLine = (ProductionLine)ctx.getBean("productionline"); 
        productLine.start(processes);         
    }
    
    public void start(ProductProcess...processes) {

    	barrier = new CyclicBarrier(processCount, new Runnable() {
    		Material material;
    		Iterator<Material> Materials;  
    		//used for storing process time mapped with count of materials
    		Map<Long, Integer> productTimes;
    		StringBuffer sb = new StringBuffer();
    		//used for storing the type mapped with tab count of material in the product
    		Map<Material, Integer> materialTabCount;
    		//used for storing max tab count to ensure consistent format when print message afterwards
    		int maxTabCount = 0;
    		int tabCountTemp;
    		StringBuffer sbTemp;
    		
    		//for temporarily storing count of materials from product units
    		Map<Material, Integer> unitMaterialCount;
    		//for temporarily storing process time of materials from product units
    		Map<Material, Long> unitMaterialTime;
    		//for temporarily storing count of materials from products
    		Map<Material, Integer> productMaterialCount;
    		//for temporarily storing process time mapped with count of materials from products
    		Map<Material, Map<Long, Integer>> productMaterialTime;
    		
    		//used for printing the process time of every material
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		Date date;   		
    		//temporarily store time for getting the count of material been added
    		Iterator<Long> timeWithCount;
    		//temporarily variable
			Long time;
			Product productForPrint;
			@Override
			public void run() {								
				Product product = new Product();
				int productCount;
				/*
				 * to standard the name of products
				 * */
				productCount = products.size();
				sb.append(productCount+1);
				while (sb.length() < productAmount.length()) {
					sb.insert(0, 0);
				}
				product.setName(name+"-"+sb.toString());
				sb.setLength(0);
				
				productMaterialCount = product.getProductMaterialCount();
				productMaterialTime = product.getProductMaterialTime();	
				materialTabCount = new HashMap<Material, Integer>();
				if (productUnits.size()==processCount) {
					/*
					 * to merge product units to products 
					 * */
					for (ProductUnit productUnit:productUnits) {
						unitMaterialCount = productUnit.getUnitMaterialCount();
						unitMaterialTime = productUnit.getUnitMaterialTime();						
						Materials = unitMaterialCount.keySet().iterator();
						while(Materials.hasNext()){
							material = Materials.next();							
							if (productMaterialCount.containsKey(material)) {
								productMaterialCount.put(material, productMaterialCount.get(material) + unitMaterialCount.get(material));
								productMaterialTime.get(material).put(unitMaterialTime.get(material), unitMaterialCount.get(material));
							}else {
								productMaterialCount.put(material, unitMaterialCount.get(material));
								productTimes = new HashMap<Long, Integer>();
								productTimes.put(unitMaterialTime.get(material), unitMaterialCount.get(material));
								productMaterialTime.put(material, productTimes);
								//calculate and store the tab count of every material and max tab count
								tabCountTemp = material.getName().length()/8+1;
								materialTabCount.put(material, tabCountTemp);
								if (maxTabCount < tabCountTemp) {
									maxTabCount = tabCountTemp;
								}
							}
						}	
					}
					products.add(product);
					//clear the product units to prepare for the next produce
					productUnits.clear();
					
					/*
					 * to print message of new produced product
					 * with material constitution and time processed 
					 * */					
					productCount = products.size();
					if (productCount == 1) {
						System.out.println(productCount+" product have been producted.");
					}else {
						System.out.println(productCount+" products have been producted.");
					}
					productForPrint = products.get(productCount-1);
					sb.append(productForPrint.getName()+" contains:\n");
					sb.append("material\tcount\tproduceTime\n");
					
					sbTemp = new StringBuffer();
					Materials = productForPrint.getProductMaterialCount().keySet().iterator();					
					while (Materials.hasNext()) {
						material = Materials.next();
						//adjust the print format
						if (maxTabCount == 1) {
							sb.append(material.getName()+"\t\t");
						}else {								
							for(int i=0;i<maxTabCount-materialTabCount.get(material)+1;i++) {
								sbTemp.append("\t");
							}				
							sb.append(material.getName()+sbTemp);							
							sbTemp.setLength(0);														
						}
						
						sb.append(productForPrint.getProductMaterialCount().get(material)+"\t");
						productTimes = productForPrint.getProductMaterialTime().get(material);
						timeWithCount = productTimes.keySet().iterator();
						while (timeWithCount.hasNext()) {
							time = timeWithCount.next();
							date = new Date(time);
							sb.append(formatter.format(date)+" (amount: "+productTimes.get(time)+"),");
						}
						sb.replace(sb.length()-1, sb.length(), "\n");
					}		
					//adjust the print of title if need
					if (maxTabCount > 2) {
						for(int i=0;i<(maxTabCount-2);i++) {
							sbTemp.append("\t");
						}
						sb.insert(26, sbTemp);
						sbTemp.setLength(0);
					}
					materialTabCount.clear();
					
					System.out.println(sb.toString());
					//clear the StringBuffer to prepare for the next print
					sb.setLength(0);					
				}else {
					try {
						throw new Exception("process/unit count error!");
					} catch (Exception e) {
						System.err.println(showStr+e.getMessage());
						System.exit(1);
					}
				}
				
			}			
		});
    	//to start the product processes
    	for(int i = 0; i < processCount; i++) {       
            taskExecutor.execute(processes[i]);     
        }
    }
	
	public CyclicBarrier getBarrier() {
		return barrier;
	}

	public List<ProductUnit> getProductUnits() {
		return productUnits;
	}

	public String getProductAmount() {
		return productAmount;
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public List<Product> getProducts() {
		return products;
	}

	public static String getShowstr() {
		return showStr;
	}      
	
}  