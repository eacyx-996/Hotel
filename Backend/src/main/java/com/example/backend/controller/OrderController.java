package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.Order;
import com.example.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/page")
    public Result<PageResult<Order>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer orderStatus) {
        return Result.success(orderService.page(pageNum, pageSize, keyword, orderStatus));
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/with-detail")
    public Result<List<Map<String, Object>>> listWithDetail() {
        return Result.success(orderService.listWithDetail());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Order order) {
        return Result.success(orderService.createOrder(order));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateOrderStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/pay")
    public Result<Void> updatePayStatus(@PathVariable Long id, @RequestParam Integer payStatus) {
        orderService.updatePayStatus(id, payStatus);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        orderService.removeById(id);
        return Result.success();
    }
}
