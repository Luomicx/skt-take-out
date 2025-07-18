/**
 * @author Wiretender
 * @version 1.0
 */
package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单接口
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户开始下单，参数：{}", ordersSubmitDTO);

        // 处理各种业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            log.warn("地址簿为空，地址ID：{}", ordersSubmitDTO.getAddressBookId());
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 查询购物车数据
        Long userId = BaseContext.getCurrentId();
        log.info("当前登录用户ID：{}", userId);
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0) {
            log.warn("购物车数据为空，用户ID：{}", userId);
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 2.向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);
        log.info("订单插入成功，订单ID：{}，订单号：{}", orders.getId(), orders.getNumber());

        // 3.向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        log.info("订单明细插入成功，共插入{}条记录", orderDetailList.size());

        // 4.清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        log.info("清空用户购物车数据，用户ID：{}", userId);

        // 5.封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        log.info("用户下单成功，返回结果：{}", orderSubmitVO);
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("开始支付订单，参数：{}", ordersPaymentDTO);

        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        JSONObject jsonObject = new JSONObject();
        paySuccess(ordersPaymentDTO.getOrderNumber());
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        log.info("订单支付成功，返回结果：{}", vo);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        log.info("支付成功，订单号：{}", outTradeNo);

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        log.info("订单状态更新成功，订单ID：{}，状态：TO_BE_CONFIRMED", ordersDB.getId());

        //
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);

        String json = JSON.toJSONString(map);
        log.info("发送Socket信息：{}", json);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 订单分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQueryUser(int pageNum, int pageSize, Integer status) {
        log.info("用户订单分页查询，页码：{}，每页条数：{}，状态：{}", pageNum, pageSize, status);

        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        // 分页查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        // 查询出订单明细，并装入OrderVO进行相应
        if (page != null && page.size() > 0) {
            for (Orders orders : page) {
                Long ordersId = orders.getId();
                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        log.info("用户订单分页查询成功，总数：{}，当前页数据条数：{}", page.getTotal(), list.size());
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        log.info("查询订单详情，订单ID：{}", id);

        // 根据ID查询订单
        Orders orders = orderMapper.getById(id);

        // 查询该订单对应的订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        // 将订单明细封装OrderVO返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        log.info("订单详情查询成功，订单信息：{}", orderVO);
        return orderVO;
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        log.info("用户取消订单，订单ID：{}", id);

        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            log.warn("订单不存在，订单ID：{}", id);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 订单状态 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (orderDB.getStatus() > 2) {
            log.warn("订单状态异常，当前状态：{}，订单ID：{}", orderDB.getStatus(), id);
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 订单处于待接单状态下取消，需要进行退款
//        if (orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            // 调用微信退款接口
//
//        }
        log.warn("跳过微信退款功能");
        // 修改订单状态为已取消
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelReason(MessageConstant.ORDER_CANCELLED_BY_USER)
                .cancelTime(LocalDateTime.now())
                .payStatus(orderDB.getPayStatus())
                .build();
        orderMapper.update(orders);
        log.info("用户取消订单成功，订单ID：{}，状态：CANCELLED", id);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    public void repetition(Long id) {
        log.info("再来一单，原订单ID：{}", id);

        //根据id查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //查询用户id
        Long userId = BaseContext.getCurrentId();
        //根据订单详情生成购物车数据
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id", "createTime");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //批量插入购物车数据
        shoppingCartMapper.insertBatch(shoppingCartList);

        log.info("再来一单成功，用户ID：{}，插入购物车数据条数：{}", userId, shoppingCartList.size());
    }

    /**
     * 条件搜索订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("条件搜索订单，参数：{}", ordersPageQueryDTO);

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        // 部分订单需要返回详细信息，转换成OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        log.info("条件搜索订单成功，总数：{}，当前页数据条数：{}", page.getTotal(), orderVOList.size());
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 订单数量处理
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        log.info("查询订单数量统计");

        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        log.info("订单数量统计成功，结果：{}", orderStatisticsVO);
        return orderStatisticsVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单，参数：{}", ordersConfirmDTO);

        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);

        log.info("接单成功，订单ID：{}，状态：{}", ordersConfirmDTO.getId(), ordersConfirmDTO.getStatus());
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单，参数：{}", ordersRejectionDTO);

        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单状态存在，且状态为2才可以拒绝单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            log.warn("订单状态异常，当前状态：{}，订单ID：{}", ordersDB != null ? ordersDB.getStatus() : "null", ordersRejectionDTO.getId());
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(ordersDB.getPayStatus())
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .build();
        orderMapper.update(orders);

        log.info("拒单成功，订单ID：{}，拒单原因：{}", ordersRejectionDTO.getId(), ordersRejectionDTO.getRejectionReason());
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单，参数：{}", ordersCancelDTO);

        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(order.getPayStatus())
                .build();
        orderMapper.update(orders);

        log.info("取消订单成功，订单ID：{}，取消原因：{}", ordersCancelDTO.getId(), ordersCancelDTO.getCancelReason());
    }

    @Override
    public void delivery(Long id) {
        log.info("开始派送订单，订单ID：{}", id);

        // 根据Id查询订单
        Orders orders = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            log.warn("订单状态异常，当前状态：{}，订单ID：{}", orders != null ? orders.getStatus() : "null", id);
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);

        log.info("订单开始派送，订单ID：{}", id);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        log.info("完成订单，订单ID：{}", id);

        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            log.warn("订单状态异常，当前状态：{}，订单ID：{}", ordersDB != null ? ordersDB.getStatus() : "null", id);
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);

        log.info("订单完成，订单ID：{}", id);
    }

    @Override
    public void reminder(Long id) {
        // 查询订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //基于WebSocket实现催单
        Map map = new HashMap();
        map.put("type", 2);//2代表用户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果

        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将公共字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
        // 将每一条订单信息拆拼接位字符串
        List<String> orderDishList = orderDetails.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        return String.join("", orderDishList);
    }
}