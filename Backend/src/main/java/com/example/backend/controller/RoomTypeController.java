package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.RoomType;
import com.example.backend.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room-type")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping("/list")
    public Result<List<RoomType>> list() {
        return Result.success(roomTypeService.listAll());
    }

    @GetMapping("/page")
    public Result<PageResult<RoomType>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(roomTypeService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<RoomType> getById(@PathVariable Long id) {
        return Result.success(roomTypeService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody RoomType roomType) {
        roomTypeService.save(roomType);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody RoomType roomType) {
        roomTypeService.update(roomType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        roomTypeService.removeById(id);
        return Result.success();
    }
}
