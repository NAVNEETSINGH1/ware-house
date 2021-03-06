package com.example.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.example.bean.InputFormBean;
import com.example.bean.InputTxnLevelMappingBean;
import com.example.bean.InventoryLeftInWarehouses;
import com.example.bean.InventoryLoadingChargesForMonth;
import com.example.bean.InventoryStorageDaysForMonth;
import com.example.bean.LEVEL;
import com.example.bean.OutData;
import com.example.bean.SumInventoryLoadingChargesForMonth;
import com.example.bean.SumInventoryStorageAndLoadingChargesForMonth;
import com.example.bean.SumInventoryStorageChargesForMonth;
import com.example.bean.TotalSumInputBean;
import com.example.model.InputTxn;
import com.example.model.OrderRequest;
import com.example.model.Role;
import com.example.model.User;
import com.example.model.Warehouse;
import com.example.repository.PageWrapper;
import com.example.service.IExcelService;
import com.example.service.IInputTxnLevelMappingService;
import com.example.service.IOrderRequestService;
import com.example.service.IUserService;
import com.example.service.IInputTxnService;
import com.example.service.UserServiceImpl;
import com.example.service.WarehouseServiceImpl;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Controller
public class LoginController
{

	@Autowired
	private IUserService userService;
	@Autowired
	private WarehouseServiceImpl warehouseService;
	@Autowired
	private IExcelService excelService;

	@Autowired
	private IOrderRequestService orderRequestService;

	@Autowired
	private IInputTxnService inputTxnService;

	@Autowired
	private IInputTxnLevelMappingService inputTxnLevelMappingService;

	private Gson gson = new Gson();

	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public ModelAndView login()
	{
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		return modelAndView;
	}

	/*
	 * @RequestMapping(value = { "/fileUpload" }, method = RequestMethod.GET)
	 * public ModelAndView excelFileTodb() { ModelAndView modelAndView = new
	 * ModelAndView();
	 * 
	 * modelAndView.addObject("inputFormBean", new InputFormBean());
	 * modelAndView.setViewName("excel-upload"); return modelAndView; }
	 */

	@RequestMapping(value = "/registration", method = RequestMethod.GET)
	public ModelAndView registration()
	{
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.addObject("roleAdd", userService.findAllRoles());
		modelAndView.setViewName("registration");
		return modelAndView;
	}

	@RequestMapping(value = "/registration/1", method = RequestMethod.POST)
	public String createNewUser(User user, BindingResult bindingResult)
	{
		ModelAndView modelAndView = new ModelAndView();
		User userExists = userService.findUserByEmail(user.getEmail());
		if (userExists != null)
		{
			bindingResult.rejectValue("email", "error.user",
					"There is already a user registered with the email provided");
		}
		if (bindingResult.hasErrors())
		{
			modelAndView.setViewName("registration");
		}
		else
		{
			userService.saveUser(user);
			return "redirect:/users/listing";
		}
		return "redirect:/registration";
	}

	@RequestMapping(value = "/users/listing", method = RequestMethod.GET)
	public ModelAndView usersListing()
	{
		ModelAndView modelAndView = new ModelAndView();
		List<User> users = userService.findAllUsers();
		modelAndView.addObject("users", users);
		modelAndView.setViewName("usersListing");
		return modelAndView;
	}

	@RequestMapping(value = "warehouse/view/registration", method = RequestMethod.GET)
	public ModelAndView warehouseRegistationView()
	{
		ModelAndView modelAndView = new ModelAndView();
		Warehouse warehouse = new Warehouse();
		modelAndView.addObject("user", warehouse);
		modelAndView.setViewName("warehouse");
		return modelAndView;
	}

	@RequestMapping(value = "create/view/orderRequest", method = RequestMethod.GET)
	public ModelAndView orderRequest()
	{
		ModelAndView modelAndView = new ModelAndView();
		Warehouse warehouse = new Warehouse();
		modelAndView.addObject("user", warehouse);
		modelAndView.setViewName("warehouse");
		return modelAndView;
	}

	@RequestMapping(value = "create/orderRequest/form", method = RequestMethod.GET)
	public ModelAndView creatInOrderRequestFormView()
	{
		ModelAndView modelAndView = new ModelAndView();
		OrderRequest orderRequest = new OrderRequest();
		orderRequest.setOrderType("in");
		orderRequest.setOrderDate(new Date());
		modelAndView.addObject("orderRequest", orderRequest);
		modelAndView.setViewName("orderRequest");
		return modelAndView;
	}

	@RequestMapping(value = "create/out/orderRequest/form", method = RequestMethod.GET)
	public ModelAndView creatOutOrderRequestFormView()
	{
		ModelAndView modelAndView = new ModelAndView();
		OrderRequest orderRequest = new OrderRequest();
		orderRequest.setDocumentReceived(true);
		orderRequest.setManagerApproval(true);
		orderRequest.setWareConfirmation(true);
		orderRequest.setOrderType("out");
		orderRequest.setOrderDate(new Date());
		modelAndView.addObject("orderRequest", orderRequest);
		modelAndView.setViewName("outOrderRequestForm");
		return modelAndView;
	}

	@RequestMapping(value = "create/data/orderRequest", method = RequestMethod.POST)
	public String createNewWareHouse(OrderRequest orderRequest, BindingResult bindingResult)
	{

		orderRequestService.saveOrderRequest(orderRequest);
		if (orderRequest.getOrderType().equals("in"))
		{
			return "redirect:/search";
		}

		else
		{
			return "redirect:/outRequest";

		}

	}

	@RequestMapping(value = "/out/data/request", method = RequestMethod.GET)
	public ModelAndView createOutRequest(@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "customerId", required = true) String customerId,
			@RequestParam(value = "warehouseID", required = true) String warehouseID,
			@RequestParam(value = "orderID", required = true) String orderID)
	{
		ModelAndView modelAndView = new ModelAndView();

		List<String> levels = new ArrayList<String>();
		levels.add(LEVEL.LEVEL1.name());
		levels.add(LEVEL.LEVEL2.name());
		levels.add(LEVEL.LEVEL3.name());
		modelAndView.addObject("levelOptions", levels);

		OutData outData = new OutData();
		outData.setId(orderID);
		outData.setOrderID(orderID);
		outData.setCustomerId(customerId);
		outData.setWarehouseID(warehouseID);

		modelAndView.addObject("outData", outData);
		modelAndView.setViewName("outData");
		return modelAndView;
	}

	@RequestMapping(value = "warehouse/view/registration", method = RequestMethod.POST)
	public String createNewWareHouse(@Valid Warehouse user, BindingResult bindingResult)
	{
		Warehouse userExists = warehouseService.findWarehouseByEmail(user.getEmail());
		if (userExists != null)
		{
			bindingResult.rejectValue("email", "error.user",
					"There is already a user registered with the email provided");
			return "warehouse";
		}
		if (bindingResult.hasErrors())
		{

			return "warehouse";

		}
		else
		{
			warehouseService.saveWarehouse(user);

		}

		return "redirect:/warehouse/listing";
	}

	@RequestMapping(value = "/admin/home", method = RequestMethod.GET)
	public ModelAndView home()
	{
		ModelAndView modelAndView = new ModelAndView();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.findUserByEmail(auth.getName());
		modelAndView.addObject("userName",
				"Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
		modelAndView.addObject("adminMessage", "Content Available Only for Users with Admin Role");
		modelAndView.setViewName("admin/home");
		return modelAndView;
	}

	@RequestMapping(value = { "/", "/warehouse/listing" }, method = RequestMethod.GET)
	public ModelAndView viewWareHouses()
	{
		ModelAndView modelAndView = new ModelAndView();
		List<Warehouse> wareHouses = warehouseService.findWarehouses();
		modelAndView.addObject("wareHouses", wareHouses);
		modelAndView.setViewName("warehouseListing");
		return modelAndView;
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ModelAndView searchPost(String query, Integer pageSize)
	{
		ModelAndView modelAndView = new ModelAndView();

		if (pageSize == null)
		{
			pageSize = new Integer(10);
		}

		// PageRequest pageable = new PageRequest(0, pageSize);
		// Page<OrderRequest> paginated =
		// orderRequestService.getAllOrderRequestWithPagination(pageable);

		// PageWrapper<OrderRequest> page = new
		// PageWrapper<OrderRequest>(paginated,
		// "/orderRequest/paginated/listing");
		// modelAndView.addObject("products", page.getContent());
		// modelAndView.addObject("page", page);
		// modelAndView.addObject("newWorkerValue", paginated.getContent());
		// modelAndView.addObject("totalPages", page.getTotalPages());
		List<OrderRequest> orderRequests = orderRequestService.findByInOrderType();
		modelAndView.addObject("orderRequests", orderRequests);

		modelAndView.addObject("psize", pageSize);
		modelAndView.setViewName("header");
		return modelAndView;
	}

	@RequestMapping(value = "/outRequest", method = RequestMethod.GET)
	public ModelAndView outRequest(String query)
	{
		ModelAndView modelAndView = new ModelAndView();

		List<OrderRequest> orderRequests = orderRequestService.findByOutOrderType();

		modelAndView.addObject("orderRequests", orderRequests);

		modelAndView.setViewName("outOrderRequest");
		return modelAndView;
	}

	@RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
	public String submit(@ModelAttribute("inputFormBean") InputFormBean inputFormBean, BindingResult result,
			ModelMap model, final @RequestParam("file") MultipartFile file)
	{

		String originalName = file.getOriginalFilename();

		if (!file.isEmpty())
		{
			try
			{
				byte[] bytes = file.getBytes();

				// Creating the directory to store file
				String rootPath = System.getProperty("catalina.home");
				long timestamp = System.currentTimeMillis() / 1000;
				File dir = new File(rootPath + File.separator + "tmpFiles/" + timestamp);
				if (!dir.exists())
					dir.mkdirs();

				// Create the file on server
				File serverFile = new File(dir.getAbsolutePath() + File.separator + originalName);
				String fileAbsolutePath = serverFile.getAbsolutePath();
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();

				List<InputTxn> savedInputTxns = inputTxnService.readFromExcelAndReturnAfterSaveToDb(inputFormBean,
						fileAbsolutePath);
				inputTxnLevelMappingService.readFromExcelAndSaveToDb(inputFormBean, fileAbsolutePath, savedInputTxns);
				// excelService.readFromExcelAndSaveToDb(fileAbsolutePath);
				System.out.println("\n\n\n  DATA SAVED SUCCESSFULLY TO DB \n\n\n ");

			}

			catch (Exception e)
			{
				e.printStackTrace();

			}

		}

		return "redirect:/search";

	}

	@RequestMapping(value = "students", method = RequestMethod.GET)
	public ModelAndView inOrderRequestSearch(@RequestParam(value = "search", required = false) String search,
			Model model)
	{
		Iterable<OrderRequest> orderRequests = orderRequestService.findByCustomerIDAndInOrderType(search);
		model.addAttribute("orderRequests", orderRequests);
		ModelAndView modelv = new ModelAndView();
		modelv.setViewName("OrderRequestListing");
		return modelv;
	}

	@RequestMapping(value = "outOrderRequest", method = RequestMethod.GET)
	public ModelAndView outOrderRequestSearch(@RequestParam(value = "search", required = false) String search,
			Model model)
	{
		// Iterable<OrderRequest> orderRequests =
		// orderRequestService.listByCustomerID(search);findByCustomerIDAndOutOrderType
		Iterable<OrderRequest> orderRequests = orderRequestService.findByCustomerIDAndOutOrderType(search);
		model.addAttribute("orderRequests", orderRequests);
		ModelAndView modelv = new ModelAndView();
		modelv.setViewName("OutOrderRequestListing");
		return modelv;
	}

	@RequestMapping(value = "/searchFragment", method = RequestMethod.GET)
	public ModelAndView fragment()
	{
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("searchFragment");
		return modelAndView;
	}

	@RequestMapping(value = "/inventory/data", method = RequestMethod.GET)
	public ModelAndView inventory()
	{
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("inventory");
		return modelAndView;
	}

	@RequestMapping(value = "/mergeForm", method = RequestMethod.GET)
	public ModelAndView id(@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "customerId", required = false) String customerId,
			@RequestParam(value = "warehouseID", required = false) String warehouseID,
			@RequestParam(value = "orderID", required = false) String orderID)
	{

		ModelAndView modelAndView = new ModelAndView();
		InputFormBean inputFormBean = new InputFormBean();
		inputFormBean.setCustomerID(customerId);
		inputFormBean.setOrderID(orderID);
		inputFormBean.setWarehouseID(warehouseID);
		Date  currentDate=new Date();
		inputFormBean.setInvoiceDate(currentDate);
		inputFormBean.setbLDate(currentDate);
		inputFormBean.setDateOfIssue(currentDate);
		modelAndView.addObject("inputFormBean", inputFormBean);
		modelAndView.setViewName("excel-upload");
		return modelAndView;

	}

	@RequestMapping(value = "/orderRequest/paginated/listing", method = RequestMethod.GET)
	public String list(Model model, int page, int size)
	{
		PageRequest pageable = new PageRequest(page, size);
		Page<OrderRequest> paginated = orderRequestService.getAllOrderRequestWithPagination(pageable);
		model.addAttribute("orderRequests", paginated.getContent());
		return "OrderRequestListing :: resultsList";
	}

	@RequestMapping(value = "/inputTxn/paginated/listing", method = RequestMethod.GET)
	public String inputTxnListing(Model model, int page, int size)
	{
		PageRequest pageable = new PageRequest(page, size);
		Page<InputTxn> paginated = inputTxnService.getAllWithPagination(pageable);
		model.addAttribute("users", paginated.getContent());
		return "inputTransactionListing :: resultsList";

	}

	@RequestMapping(value = "/inputTransactions/listing", method = RequestMethod.GET)
	public ModelAndView inputTransactions(@RequestParam(value = "search", required = false) String search)
	{

		ModelAndView modelAndView = new ModelAndView();
		List<InputTxn> inputTransactions = inputTxnService.findInputTransactions();

		modelAndView.addObject("users", inputTransactions);
		modelAndView.setViewName("inputTransactionListing");
		return modelAndView;
	}

	@RequestMapping(value = "/totalSumView", method = RequestMethod.GET)
	public ModelAndView totalSumView()
	{

		ModelAndView modelAndView = new ModelAndView();
		TotalSumInputBean totalSumBean=new TotalSumInputBean();
		totalSumBean.setOrderDate(new Date());
		modelAndView.addObject("totalSumInputBean",totalSumBean);
		modelAndView.setViewName("totalSumInventory");
		return modelAndView;
	}

	@RequestMapping(value = "/outDataRequest", method = RequestMethod.POST)
	public String addNewPost(@Valid OutData outData, BindingResult bindingResult, Model model)
	{
		if (bindingResult.hasErrors())
		{
			return "outData";
		}

		String levelName = outData.getLevelName();
		String levelValue = outData.getLevelValue();
		String customerID = outData.getCustomerId();
		int levelNo = 0;
		if (outData.getLevelCat().equals(LEVEL.LEVEL1.name()))
		{
			levelNo = 1;
		}
		else if (outData.getLevelCat().equals(LEVEL.LEVEL2.name()))
		{
			levelNo = 2;
		}
		else if (outData.getLevelCat().equals(LEVEL.LEVEL3.name()))
		{
			levelNo = 3;
		}
		// System.out.println(outData.getLevelCat());
		// System.out.println(levelNo);
		List<InputTxnLevelMappingBean> inputTxnLevelMappings = inputTxnLevelMappingService
				.findByLevelNameAndLevelValueAndGetBean(levelNo, levelName, levelValue, customerID);
		// List<InputTxnLevelMappingBean>
		// findByLevelNameAndLevelValueAndGetBean(Integer levelNo, String
		// levelName, String levelValue)
		// System.out.println(inputTxnLevelMappings);

		model.addAttribute("users", inputTxnLevelMappings);
		model.addAttribute("outData", outData);
		return "inputTransactionListing :: resultsList";

	}

	@RequestMapping(value = "/saveAndGetInputTxns", method = RequestMethod.POST)
	public String saveAndGetInputTxns(@RequestParam(value = "levelTxn[]") Integer[] levelTxn,
			@RequestParam(value = "inputTxn[]") Integer[] inputTxn)
	{

		PageRequest pageable = new PageRequest(0, 1000);
		List<InputTxn> paginated = inputTxnService.findInputTransactions();

		System.out.println(inputTxn + "\n" + levelTxn);
		List<Integer> inputTxnIds = Arrays.asList(inputTxn);
		for (Integer id : inputTxnIds)
		{
			System.out.println(id);
		}
		List<Integer> inputTxnLevelMappingIds = Arrays.asList(levelTxn);
		for (Integer id : inputTxnLevelMappingIds)
		{
			System.out.println(id);
		}

		inputTxnLevelMappingService.markCorrespondingInputTxnLevelMappingsAsOutFromIds(inputTxnLevelMappingIds);
		inputTxnLevelMappingService.markCorrespondingInputTxnsAsOutFromIds(inputTxnIds);
		return "inputTransactionListing :: resultsList";

	}

	/**
	 * 
	 * @Navneet
	 * 
	 * 			public List<InputTxnLevelMappingBean>
	 *          findByLevelNameAndLevelValueAndGetBean(Integer levelNo, String
	 *          levelName, String levelValue) is the service for fetching the
	 *          results
	 * 
	 *          And
	 * 
	 *          public int
	 *          markCorrespondingBothInputTxnAndLevelMappingsAsOut(List
	 *          <InputTxnLevelMappingBean> inputTxnLevelMappingBeans) is the
	 *          service to mark those txns as out
	 * 
	 */

	@RequestMapping(value = "/saveAndGetInputTxns/final", method = RequestMethod.POST)
	public String saveAndGetInputTxns(@RequestParam(value = "data[]") String[] inputTxnLevelMappingBeans,
			@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "customerId", required = true) String customerId,
			@RequestParam(value = "warehouseID", required = true) String warehouseID,
			@RequestParam(value = "orderID", required = true) String orderID)
	{

		List<InputTxnLevelMappingBean> beans = new ArrayList<>();
		if (inputTxnLevelMappingBeans != null && inputTxnLevelMappingBeans.length > 1)
		{
			for (int i = 1; i < inputTxnLevelMappingBeans.length; i++)
			{

				String itl = inputTxnLevelMappingBeans[i];
				itl = itl.replace("[", "{");
				itl = itl.replace("]", "}");
				InputTxnLevelMappingBean InputTxnLevelMappingBea = gson.fromJson(itl, InputTxnLevelMappingBean.class);
				beans.add(InputTxnLevelMappingBea);
			}
			OutData outData = new OutData();
			outData.setCustomerId(customerId);
			outData.setOrderID(orderID);
			outData.setWarehouseID(warehouseID);
			int result = inputTxnLevelMappingService.markCorrespondingBothInputTxnAndLevelMappingsAsOut(beans, outData);

		}
		return "redirect: /outRequest";

	}

	@RequestMapping(value = "/inventoryby/customer", method = RequestMethod.GET)
	public ModelAndView findInventoryLeftInWarehousesByCustomerID(String customerID)
	{
		// Authentication auth =
		// SecurityContextHolder.getContext().getAuthentication();
		// String email = auth.getName();
		// auth.getAuthorities()
		ModelAndView modelAndView = new ModelAndView();
		// User user = userService.findUserByEmail(email);
		List<InventoryLeftInWarehouses> inventories = new ArrayList<>();
		// if (user != null)
		// {
		// String customerID = "" + user.getId();
		inventories = inputTxnService.findInventoryLeftInWarehousesByCustomerID(customerID);
		// }

		modelAndView.addObject("inventories", inventories);
		modelAndView.setViewName("WarehouseInventoriesCustomer");
		return modelAndView;
	}

	@RequestMapping(value = "/inventorydtoragedays/customer", method = RequestMethod.GET)
	public ResponseEntity<List<InventoryStorageDaysForMonth>> findInventoryStorageDaysForMonthByCustomerID()
	{
		String customerID = "1";
		List<InventoryStorageDaysForMonth> results = inputTxnService
				.findInventoryStorageDaysForMonthByCustomerID(customerID, new Date());
		ResponseEntity<List<InventoryStorageDaysForMonth>> responseEntity = new ResponseEntity<>(results,
				HttpStatus.OK);
		return responseEntity;
	}

	// storage
	@RequestMapping(value = "/inventorydtoragecharges/customer", method = RequestMethod.POST)
	public ResponseEntity<List<InventoryStorageDaysForMonth>> findInventoryStorageChargesForMonthByCustomerID(
			String customerId, @DateTimeFormat(pattern="yyyy-MM-dd") Date date)
	{
		
		List<InventoryStorageDaysForMonth> results = inputTxnService.findInventoryStorageChargesForMonthByCustomerID(
				customerId, date);
		ResponseEntity<List<InventoryStorageDaysForMonth>> responseEntity = new ResponseEntity<>(results,
				HttpStatus.OK);
		return responseEntity;
	}

	@RequestMapping(value = "/inventorystoragecharges/customer/sum", method = RequestMethod.GET)
	public ResponseEntity<SumInventoryStorageChargesForMonth> findSumInventoryStorageChargesForMonthByCustomerID()
	{
		String customerID = "1";
		SumInventoryStorageChargesForMonth results = inputTxnService
				.findSumInventoryStorageChargesForMonthByCustomerID(customerID, new Date());
		ResponseEntity<SumInventoryStorageChargesForMonth> responseEntity = new ResponseEntity<>(results,
				HttpStatus.OK);
		return responseEntity;
	}

	/************************************************************
	 * LOADING CHARGES
	 ***************************************************/
	@RequestMapping(value = "/inventoryloadingcharges/customer/list", method = RequestMethod.GET)
	public ResponseEntity<List<InventoryLoadingChargesForMonth>> findInventoryLoadingChargesMonthByCustomerID(
			String customerId, @DateTimeFormat(pattern="yyyy-MM-dd") Date date)
	{
		
		
		// String customerID = "1";
		List<InventoryLoadingChargesForMonth> results = orderRequestService
				.findInventoryLoadingChargesMonthByCustomerID(customerId,
						date);
		ResponseEntity<List<InventoryLoadingChargesForMonth>> responseEntity = new ResponseEntity<>(results,
				HttpStatus.OK);
		return responseEntity;
	}

	@RequestMapping(value = "/inventoryloadingcharges/customer/sum", method = RequestMethod.GET)
	public ResponseEntity<SumInventoryLoadingChargesForMonth> findSumInventoryLoadingChargesMonthByCustomerID()
	{
		String customerID = "1";
		SumInventoryLoadingChargesForMonth results = orderRequestService
				.findSumInventoryLoadingChargesMonthByCustomerID(customerID, new Date());
		ResponseEntity<SumInventoryLoadingChargesForMonth> responseEntity = new ResponseEntity<>(results,
				HttpStatus.OK);
		return responseEntity;
	}

	/************************************************************
	 * STORAGE & LOADING CHARGES **************************customerId,months
	 * date
	 ****************/
	@RequestMapping(value = "/inventorystorageandloadingcharges/customer/sum", method = RequestMethod.POST)
	public ModelAndView findSumInventoryStorageAndLoadingChargesForMonthByCustomerID(
			TotalSumInputBean totalSumInputBean)
	{
		Date date = totalSumInputBean.getOrderDate();

		SumInventoryStorageAndLoadingChargesForMonth sumInventoryStorageAndLoadingChargesForMonth = inputTxnService
				.findSumInventoryStorageAndLoadingChargesForMonthByCustomerID(totalSumInputBean.getCustomerId(), date);
		ResponseEntity<SumInventoryStorageAndLoadingChargesForMonth> responseEntity = new ResponseEntity<>(
				sumInventoryStorageAndLoadingChargesForMonth, HttpStatus.OK);

		ModelAndView modelAndView = new ModelAndView();

		modelAndView.addObject("customerId", totalSumInputBean.getCustomerId());
		modelAndView.addObject("date", totalSumInputBean.getOrderDate());

		modelAndView.addObject("totalSumInputBean", totalSumInputBean);
		modelAndView.addObject("sum", sumInventoryStorageAndLoadingChargesForMonth);
		modelAndView.setViewName("SumResult");
		return modelAndView;

	}

}
