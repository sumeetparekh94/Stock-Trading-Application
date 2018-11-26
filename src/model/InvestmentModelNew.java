package model;

import java.text.ParseException;
import java.util.HashMap;

import service.IStockMarketSimulation;
import service.StockMarketSimulation;
import transferable.PortfolioTransferable;

public class InvestmentModelNew extends InvestmentModel implements InvestModelInterfaceNew {

  private HashMap<String, PortfolioWallet> wallet;
  private HashMap<String, WeightsOfPortfolio> listOfWeights;
  private HashMap<String, InvestmentStrategyInterface> strategyTracker;


  public InvestmentModelNew() {
    wallet = new HashMap<String, PortfolioWallet>();
    listOfWeights = new HashMap<String, WeightsOfPortfolio>();
    strategyTracker = new HashMap<String, InvestmentStrategyInterface>();

  }


  @Override
  public void buyStocks(String ticker, String timeStamp, Integer noOfShares, String portfolioName)
          throws IllegalArgumentException, ParseException {


    //lots of checks if the stock already has a strategy.

    if(strategyTracker.containsKey(portfolioName))
    {
      throw new IllegalArgumentException("portfolio locked strategy");
    }
    super.buyStocks(ticker, timeStamp, noOfShares, portfolioName);


  }

  @Override
  public PortfolioTransferable evaluatePortfolio(String portfolioName, String timestamp){
    if(strategyTracker.containsKey(portfolioName))
    {
      System.out.println("already has a strategy\n");
      invest(strategyTracker.get(portfolioName),portfolioName, timestamp);
    }
    return super.evaluatePortfolio(portfolioName,timestamp);
  }

  @Override
  public void investStocks(String portfolioName, Double fixedAmount, HashMap<String, Double> weights, String timeStamp) throws ParseException {
    IPortfolio investingPortfolio = this.listOfPortfolio.get(portfolioName);
    this.listOfWeights.put(portfolioName, new WeightsOfPortfolio(weights));
    for (String key : investingPortfolio.getStockNamesInPortfolio()) {
      Double moneyForEachStock = (weights.get(key) / 100) * fixedAmount;
      investStockhelper(moneyForEachStock, key, portfolioName, timeStamp);
    }


  }

  @Override
  public void investStocks(String portfolioName, Double fixedAmount, String timeStamp) throws ParseException {

    IPortfolio investingPortfolio = this.listOfPortfolio.get(portfolioName);

    Double moneyForEachStock = fixedAmount / investingPortfolio.getStockNamesInPortfolio().size();

    for (String key : investingPortfolio.getStockNamesInPortfolio()) {

      investStockhelper(moneyForEachStock, key, portfolioName, timeStamp);
    }

  }

  @Override
  public void registerStrategy(InvestmentStrategyInterface strategy, String portfolioName) {
    if(!strategyTracker.containsKey(portfolioName))
    {
      strategyTracker.put(portfolioName,strategy);
    }
    else {
      throw  new IllegalArgumentException("sorry this portfolio already has a strategy");
    }
  }

  @Override
  public HashMap<String, Double> viewWeights(String portfolioName) {
    HashMap<String, Double> weightsOfAPortfolio = this.listOfWeights.get(portfolioName).getWeight();
    return new HashMap<String, Double>(weightsOfAPortfolio);
  }

  private void invest(InvestmentStrategyInterface strategy, String portfolioName, String timestamp) {
    try {
      strategy.exceuteStrategyOnPortfolio(this.listOfPortfolio.get(portfolioName), this, timestamp);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Date format is Invalid");
    }
  }


  private void investStockhelper(Double moneyForEachStock, String key, String portfolioName, String timeStamp) throws ParseException {
    IStockMarketSimulation stockMarket = StockMarketSimulation.getInstance();
    Double currentStockPrice = stockMarket.priceOfAStockAtACertainDate(key, timeStamp);


    Double partialNumberOfShares = (moneyForEachStock / currentStockPrice);
    Integer wholeShares = partialNumberOfShares.intValue();


    buyStocks(key, timeStamp, wholeShares, portfolioName);

    Double remaningAmountInWallet = moneyForEachStock - (wholeShares * currentStockPrice);

    System.out.println("ticker: " + key);
    System.out.println("money available for each stock: " + moneyForEachStock);
    System.out.println("current stock Price: " + currentStockPrice);
    System.out.println("whole shares: " + wholeShares);
    System.out.println("the remaining amount in wallet :" + remaningAmountInWallet);

    if (wallet.containsKey(portfolioName)) {
      remaningAmountInWallet = wallet.get(portfolioName).getRemainingAmount() + remaningAmountInWallet;
      wallet.put(portfolioName, new PortfolioWallet(remaningAmountInWallet));
    } else {
      wallet.put(portfolioName, new PortfolioWallet(remaningAmountInWallet));
    }



  }

  public String toString() {
    return listOfPortfolio + "\n" + wallet;
  }


}

