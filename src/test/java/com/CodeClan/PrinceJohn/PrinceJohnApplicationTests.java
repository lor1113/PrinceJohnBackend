package com.CodeClan.PrinceJohn;

import com.CodeClan.PrinceJohn.components.UserService;
import com.CodeClan.PrinceJohn.models.Stock;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
class PrinceJohnApplicationTests {
	@Autowired
	StockRepository stockRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserService userService;

	@Test
	void contextLoads() {
	}

	@Test
	void canSaveStock() {
		Stock testStock = new Stock("YYY",10F);
		stockRepository.save(testStock);
	}

	@Test
	void canSaveUser() {
		User testUser = new User("Lorenzo","Lorenzocurcio2@gmail.com", LocalDate.of(2001, 4, 7));
		userRepository.save(testUser);
	}

	@Test
	void canSavePortfolio() {
		User testUser = new User("Jonas","jabba@gmail.com", LocalDate.of(1999, 1, 1));
		userRepository.save(testUser);
		Stock testStock1 = new Stock("ABC",200F);
		Stock testStock2 = new Stock("BCD",5F);
		Stock testStock3 = new Stock("CBE",20F);
		stockRepository.save(testStock1);
		stockRepository.save(testStock2);
		stockRepository.save(testStock3);
		Map<String, Integer> testPortfolio = new HashMap<>();
		testPortfolio.put(testStock1.ticker,3);
		testPortfolio.put(testStock2.ticker,10);
		testPortfolio.put(testStock3.ticker,8);
		testUser.setPortfolio(testPortfolio);
		userRepository.save(testUser);
	}

	@Test
	void canCalculatePortfolioValue() {
		User testUser = new User("Jesse","jabba@gmail.com", LocalDate.of(1999, 1, 1));
		Map<String, Integer> testPortfolio = new HashMap<>();
		Stock testStock1 = new Stock("LOL",200F);
		Stock testStock2 = new Stock("LEL",5F);
		Stock testStock3 = new Stock("XD",20F);
		stockRepository.save(testStock1);
		stockRepository.save(testStock2);
		stockRepository.save(testStock3);
		testPortfolio.put(testStock1.ticker,10);
		testPortfolio.put(testStock2.ticker,10);
		testPortfolio.put(testStock3.ticker,10);
		testUser.setPortfolio(testPortfolio);
		userRepository.save(testUser);
		userService.setUser(testUser);
		Assert.isTrue(userService.portfolioValue() == 2250,"Portfolio value must be 2250");
	}

	@Test
	void userCanAddBalance() {
		User testUser = new User("Jesse","jabba@gmail.com", LocalDate.of(1999, 1, 1));
		testUser.addBalance(100);
		userRepository.save(testUser);
		Assert.isTrue(testUser.balance == 100,"Balance must be 100");
	}

	@Test
	void userCanBuyNewStock() {
		User testUser = new User("Jesse","jabba@gmail.com", LocalDate.of(1999, 1, 1));
		testUser.addBalance(200);
		Stock testStock2 = new Stock("XXX",5F);
		stockRepository.save(testStock2);
		testUser.buyStock(testStock2, 10);
		userRepository.save(testUser);
		Assert.isTrue(testUser.balance == 150,"Balance must be 150");
	}

	@Test
	void userCanBuyOldStock() {
		User testUser = new User("Jesse","jabba@gmail.com", LocalDate.of(1999, 1, 1));
		testUser.addBalance(200);
		Stock testStock2 = new Stock("DDD",5F);
		stockRepository.save(testStock2);
		testUser.buyStock(testStock2, 10);
		userRepository.save(testUser);
		Assert.isTrue(testUser.balance == 150,"Balance must be 150");
		testUser.buyStock(testStock2, 10);
		userRepository.save(testUser);
		Assert.isTrue(testUser.balance == 100,"Balance must be 100");
	}

	@Test
	void userCanUpdatePortfolioHistory () {
		User testUser = new User("Jesse","jabba@gmail.com", LocalDate.of(1999, 1, 1));
		testUser.portfolio.put("AMZN",10);
		testUser.portfolio.put("AMD",10);
		testUser.portfolio.put("AAPL",10);
		testUser.portfolioHistoryUpdated = LocalDate.now().minusDays(250);
		userRepository.save(testUser);
		userService.setUser(testUser);
		userService.updatePortfolioHistory();
		User testUserUpdated = userService.getUser();
		userRepository.save(testUserUpdated);
	}

}
