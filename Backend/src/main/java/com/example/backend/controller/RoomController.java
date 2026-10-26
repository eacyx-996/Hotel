package com.example.backend.controller;

import com.example.backend.common.PageResult;
import com.example.backend.common.Result;
import com.example.backend.entity.Room;
import com.example.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/page")
    public Result<PageResult<Room>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Integer status) {
        return Result.success(roomService.page(pageNum, pageSize, keyword, roomTypeId, status));
    }

    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        return Result.success(roomService.getById(id));
    }

    @GetMapping("/with-type")
    public Result<List<Map<String, Object>>> listWithTypeName() {
        return Result.success(roomService.listWithTypeName());
    }

    @GetMapping("/available")
    public Result<List<Room>> listAvailable() {
        return Result.success(roomService.listAvailable());
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(roomService.stats());
    }

    @PostMapping
    public Result<Void> save(@RequestBody Room room) {
        roomService.save(room);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Room room) {
        roomService.update(room);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roomService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        roomService.removeById(id);
        return Result.success();
    }
}
