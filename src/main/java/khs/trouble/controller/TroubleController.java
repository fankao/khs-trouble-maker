package khs.trouble.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import khs.trouble.model.Email;
import khs.trouble.model.Event;
import khs.trouble.model.Service;
import khs.trouble.model.ServiceContainer;
import khs.trouble.service.impl.EmailService;
import khs.trouble.service.impl.EventService;
import khs.trouble.service.impl.TroubleService;

@Controller
@RequestMapping(value = "/api")
public class TroubleController {

	@Autowired
	TroubleService service;

	@Autowired
	EventService eventService;
	
	@Autowired
	EmailService emailService;

	@Autowired
	DiscoveryClient discoveryClient;

	@Value("${trouble.token}")
	String token;

	@RequestMapping(value = "/access/token", method = RequestMethod.GET)
	@ResponseBody
	public String accessToken(HttpServletRequest request) {
		return token;
	}

	@RequestMapping(value = "/random/kill", method = RequestMethod.GET)
	@ResponseBody
	public boolean randomKill(HttpServletRequest request) {
		service.randomKill(token);
		return true;
	}

	@RequestMapping(value = "/kill/{service:.+}/{instanceId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public boolean kill(@PathVariable("service") String serviceName, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
		service.kill(serviceName, instanceId, token);
		return true;
	}

	@RequestMapping(value = "/random/load", method = RequestMethod.GET)
	@ResponseBody
	public boolean randomBlock(HttpServletRequest request) {
		service.randomLoad(token);
		return true;
	}

	@RequestMapping(value = "/load/{service:.+}/{instanceId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public boolean block(@PathVariable("service") String serviceName, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
		service.load(serviceName, instanceId, token);
		return true;
	}

	@RequestMapping(value = "/random/exception/{service:.+}", method = RequestMethod.GET)
	@ResponseBody
	public boolean randomException(@PathVariable("service") String serviceName, HttpServletRequest request) {
		service.exception(serviceName, "", token);
		return true;
	}

	@RequestMapping(value = "/exception/{service:.+}/{instanceId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public boolean exception(@PathVariable("service") String serviceName, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
		service.exception(serviceName, instanceId, token);
		return true;
	}

	@RequestMapping(value = "/random/memory/{service:.+}", method = RequestMethod.GET)
	@ResponseBody
	public boolean randomMemory(@PathVariable("service") String serviceName, HttpServletRequest request) {
		service.memory(serviceName, "", token);
		return true;
	}

	@RequestMapping(value = "/memory/{service:.+}/{instanceId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public boolean memory(@PathVariable("service") String serviceName, @PathVariable("instanceId") String instanceId, HttpServletRequest request) {
		service.memory(serviceName, instanceId, token);
		return true;
	}
	
	

	@RequestMapping(value = "/services", method = RequestMethod.GET)
	@ResponseBody
	public ServiceContainer services() {
	//public List<Service> services() {
	//public List<String> services() {
		List<String> list = discoveryClient.getServices();
		
		ServiceContainer serviceContainer = new ServiceContainer();
		List<Service> services = new ArrayList<Service>();
		serviceContainer.setServices(services);
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String serviceId = (String) iterator.next();
			Service application = new Service();
			application.setName(serviceId);
			application.setInstances(discoveryClient.getInstances(serviceId));
			services.add(application);
		}
		if (services.isEmpty()) {
			eventService.eventInfo("No Services discovered, make sure service registry is started and visible");
		}
		return serviceContainer;
		
		//if (list.isEmpty()) {
		//	eventService.eventInfo("No Services discovered, make sure service registry is started and visible");
		//}
		//return list;
	}

	@RequestMapping(value = "/events", method = RequestMethod.GET)
	@ResponseBody
	public Iterable<Event> events() {
		return eventService.events();
	}

	@RequestMapping(value = "/valid", method = RequestMethod.GET)
	@ResponseBody
	public boolean valid(@PathVariable("token") String ltoken) {
		return token.equals(ltoken);
	}
	
	@RequestMapping(value = "/emails", method = RequestMethod.GET)
	@ResponseBody
	public Iterable<Email> retrieveEmails() {
		return emailService.emails();		
	}
	
	@RequestMapping(value = "/emails{fullname}{emailaddress}", method = RequestMethod.POST)
	@ResponseBody
	public Email updateEmail(@PathVariable("fullname") String fullName, @PathVariable("emailaddress") String emailAddress, HttpServletRequest request) {
		Email email = new Email();
		email.setFullName(fullName);
		email.setEmailAddress(emailAddress);
		return emailService.updateEmail(email);		
	}
}
