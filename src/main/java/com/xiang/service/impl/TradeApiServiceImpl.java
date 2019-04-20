package com.xiang.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.okcoin.commons.okex.open.api.bean.futures.param.CancelOrders;
import com.okcoin.commons.okex.open.api.bean.futures.param.Order;
import com.okcoin.commons.okex.open.api.bean.futures.param.Orders;
import com.okcoin.commons.okex.open.api.bean.futures.param.OrdersItem;
import com.okcoin.commons.okex.open.api.bean.futures.result.OrderResult;
import com.okcoin.commons.okex.open.api.exception.APIException;
import com.okcoin.commons.okex.open.api.service.futures.FuturesTradeAPIService;
import com.okcoin.commons.okex.open.api.service.futures.impl.FuturesTradeAPIServiceImpl;
import com.okex.websocket.FutureOrder;
import com.xiang.service.TradeApiService;

/**
 * 期货交易okex api服务
 * @author xiang
 * @createDate 2018年12月28日 上午9:55:36
 */
@Service("tradeApiService")
@EnableRetry
public class TradeApiServiceImpl implements TradeApiService{
	FuturesTradeAPIService futurePostV3;
	@Autowired
	private SystemConfig systemConfig;
	private TradeManager tradeManager=TradeManager.getInstance();
	@PostConstruct
	public void init() {
		futurePostV3 = new FuturesTradeAPIServiceImpl(systemConfig);
	}
	/**
	 * 修复交易（修复实际已成交，但未收到订单信息的对冲交易）
	 * 
	 * @param trade
	 * @throws TimeoutException
	 */
	@Async
	@Override
	@Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 200l)) // timeout异常使用重试机制
	public void repairHedgingTrade(HedgingTrade trade) throws TimeoutException {
		if (trade != null && !Strings.isNullOrEmpty(trade.getOrderId())
				&& !Strings.isNullOrEmpty(trade.getInstrumentId())) {
			try {
				// 无需修复交易
				if (trade.getFutureOrder() != null
						&& (trade.getFutureOrder().getFilledQty() == trade.getAmount() || trade.getFutureOrder().getStatus()==7))
					return;
				JSONObject jSONObject = futurePostV3.getOrder(trade.getInstrumentId(),
						Long.parseLong(trade.getOrderId()));
				FutureOrder futuresOrder=JSON.toJavaObject(jSONObject, FutureOrder.class);
				if (futuresOrder != null) {
					tradeManager.updateFuturesOrder(futuresOrder);
					trade.setFutureOrder(futuresOrder);
				}
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/* 
	 * 撤销提交的交易
	 */
	@Async
	@Override
	@Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100l)) // timeout异常使用重试机制
	public void cancelHedgingTrade(HedgingTrade trade) throws TimeoutException {
		if (trade.getFutureOrder() != null
				&& trade.getFutureOrder().getSize() > trade.getFutureOrder().getFilledQty()
				&& (trade.getFutureOrder().getStatus() == 0 || trade.getFutureOrder().getStatus() == 1 || trade.getFutureOrder().getStatus() == 6)) {
					futurePostV3.cancelOrder(trade.getFutureOrder().getInstrumentId(),
					Long.parseLong(trade.getFutureOrder().getOrderId()));
		}
	}
	/* 
	 * 批量撤销提交的交易
	 */
	@Async
	@Override
	@Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100l)) // timeout异常使用重试机制
	public void batchCancel(List<HedgingTrade> trades) throws TimeoutException {
		List<Long> orders = new ArrayList<Long>();
		String instrumentId=null;
		for (HedgingTrade trade : trades) {
			instrumentId=trade.getInstrumentId();
			orders.add(Long.parseLong(trade.getOrderId()));
		}
		if(!orders.isEmpty())
		{
			CancelOrders cancelOrders = new CancelOrders();
			cancelOrders.setOrder_ids(orders);
			JSONObject result = futurePostV3.cancelOrders(instrumentId, cancelOrders);
		}
	}
	/* 
	 * 提交交易
	 */
	@Async
	@Override
	@Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100l)) // timeout异常使用重试机制
	public void order(HedgingTrade trade) throws TimeoutException {
		// TODO Auto-generated method stub
		if (trade.getAmount() > 0 && trade.getStatus()==0) {
			tradeManager.addHedgingTrade(trade);
			trade.setStatus(1);
			try {
				Order futureOrder = new Order();
				futureOrder.setClient_oid(trade.getHedgingTradeId());
				futureOrder.setinstrument_id(trade.getInstrumentId());
				futureOrder.setLeverage((double) trade.getLeverRate());
				futureOrder.setPrice((double) trade.getPrice());
				futureOrder.setSize(trade.getAmount());
				futureOrder.setType(Integer.parseInt(trade.getType()));
				OrderResult orderResult = futurePostV3.order(futureOrder);
				if (orderResult.isResult()) {
					trade.setOrderId(orderResult.getOrder_id());
					trade.setStatus(2);
					return;
				} else {
					trade.setStatus(3);
					System.out.println(trade.getHedgingTradeId() + "  order error  " + orderResult.getError_code()
							+ "  " + orderResult.getError_messsage());
				}
			} catch (APIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			trade.setStatus(3);
		}
	}
	/* 
	 * 批量提交交易
	 */
	@Async
	@Override
	@Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 50l)) // timeout异常使用重试机制
	public void batchOrders(List<HedgingTrade> trades) throws TimeoutException {
		Map<String,HedgingTrade> orderTrades=new HashMap<String,HedgingTrade>();
		List<OrdersItem> orders_data = new ArrayList<OrdersItem>();
		String instrument_id=null;
		int leverage=0;
		for (HedgingTrade trade : trades) {
			if (trade.getAmount() > 0 && trade.getStatus()==0) {
				tradeManager.addHedgingTrade(trade);
				instrument_id=trade.getInstrumentId();
				leverage=trade.getLeverRate();
				OrdersItem futureOrder = new OrdersItem();
				futureOrder.setClient_oid(trade.getHedgingTradeId());
				futureOrder.setPrice((double) trade.getPrice());
				futureOrder.setSize(trade.getAmount());
				futureOrder.setType(Integer.parseInt(trade.getType()));
				orders_data.add(futureOrder);
				trade.setStatus(1);
				orderTrades.put(trade.getHedgingTradeId(), trade);
			}
		}
		if(!orders_data.isEmpty())
		{
			Orders orders = new Orders();
			orders.setinstrument_id(instrument_id);
			orders.setLeverage((double)leverage);
			orders.setOrders_data(orders_data);
			JSONObject result = futurePostV3.orders(orders);
			System.out.println("batchOrders result "+result);
			if(result.getBooleanValue("result"))
			{
				JSONArray order_info=result.getJSONArray("order_info");
				for(Object obj :order_info)
				{
					JSONObject order=(JSONObject)obj;
					String client_oid=order.getString("client_oid");
					String order_id=order.getString("order_id");
					HedgingTrade trade=orderTrades.get(client_oid);
					if(!"-1".equals(order_id))
					trade.setOrderId(order_id);
					if(order.getIntValue("error_code")==0)
					trade.setStatus(2);
					else
						trade.setStatus(3);
				}
			}
		}
	}
	@Recover
    public void recover(TimeoutException e) {
		//调用api失效，重新查询更新下订单列表状态
    }
}
