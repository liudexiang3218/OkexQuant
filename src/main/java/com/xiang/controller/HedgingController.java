package com.xiang.controller;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xiang.service.HedgingDataService;
import com.xiang.service.HedgingService;
import com.xiang.service.impl.HedgingConfig;
import com.xiang.spring.ErrorCodes;

/**
 * @author xiang
 *
 */
@CrossOrigin
@RestController
@RequestMapping(value="/hedging")
public class HedgingController {
	@Resource
	private HedgingService hedgingService;
	@Resource
	private HedgingDataService hedgingDataService;
	@RequestMapping(value="/config", params = "configId")
	 public Object getHedgingConfig(@RequestParam("configId") @NotNull(message = "configId不能为空") String configId){
		return hedgingService.getHedgingConfig(configId);
	 }
	@RequestMapping(value="/config/remove", params = "configId")
	 public Object removeHedgingConfig(@RequestParam("configId") @NotNull(message = "configId不能为空") String configId){
		hedgingService.removeHedgingConfig(configId);
		return ErrorCodes.OK;
	 }
	@RequestMapping(value="/config/save",method = RequestMethod.POST)
	 public Object saveHedgingConfig(@RequestBody HedgingConfig config){
		HedgingConfig hedgingConfig=hedgingService.getHedgingConfig(config.getConfigId());
		if(hedgingConfig==null)
			return addHedgingConfig(config);
		BeanUtils.copyProperties(config, hedgingConfig);
		hedgingService.addHedgingConfig(hedgingConfig);
		return config;
	 }
	@RequestMapping(value="/config/add",method = RequestMethod.POST)
	 public Object addHedgingConfig(@RequestBody HedgingConfig config){
		hedgingService.addHedgingConfig(config);
		return config;
	 }
	@RequestMapping(value="/config/list")
	 public Object getHedgingConfigs(@RequestParam("coin") @NotNull(message = "coin不能为空") String coin,@RequestParam("type") @NotNull(message = "type不能为空") String type){
		return hedgingService.getConfigs(coin,type);
	 }
	@RequestMapping(value="/config/new")
	 public Object newHedgingConfig(@RequestParam("coin") @NotNull(message = "coin不能为空") String coin,@RequestParam("type") @NotNull(message = "type不能为空") String type){
		return hedgingService.newHedgingConfig(coin, type);
	 }
	@RequestMapping(value="/list")
	 public Object getHedgings(){
		return hedgingService.getHedgings();
	 }
	@RequestMapping(value="/data")
	 public Object getHedgingData(@RequestParam("coin") @NotNull(message = "base不能为空") String coin,@RequestParam("type") @NotNull(message = "type不能为空") String type){
		LinkedList<Map<String, Object>> list=hedgingDataService.getHedgingData(coin, type);
		if(list==null)
			return new LinkedList<Map<String, Object>>();
		return list;
	 }
	@RequestMapping(value="/repair")
	 public Object repairHedging(@RequestParam("hedgingId") @NotNull(message = "hedgingId不能为空") String hedgingId) throws TimeoutException{
		return hedgingService.repairHedging(hedgingId);
	 }
	/**
	 * 强平仓
	 * @param hedgingId
	 * @return
	 */
	@RequestMapping(value="/liquid", params = "hedgingId")
	 public Object liquidHedging(@RequestParam("hedgingId") @NotNull(message = "hedgingId不能为空") String hedgingId){
		hedgingService.liquidHedging(hedgingId);
		return ErrorCodes.OK;
	 }
	/**
	 * 全部强平仓
	 * @param hedgingId
	 * @return
	 */
	@RequestMapping(value="/liquidall")
	 public Object liquidAllHedging(){
		hedgingService.liquidAllHedging();
		return ErrorCodes.OK;
	 }
}
