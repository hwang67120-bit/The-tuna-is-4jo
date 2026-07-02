package com.example.thetunais4joteamproject.global.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

	@GetMapping({"/cart", "/profile", "/orders", "/admin", "/order", "/products/{productId}"})
	public String forwardView() {
		return "forward:/index.html";
	}
}